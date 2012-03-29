package co.adhoclabs.ironcushion.bulkinsert;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import co.adhoclabs.ironcushion.AbstractBenchmarkHandler;
import co.adhoclabs.ironcushion.HttpReactor.ResponseHandler;
import co.adhoclabs.ironcushion.bulkinsert.BulkInsertConnectionTimers.RunningConnectionTimer;

/**
 * The {@link SimpleChannelUpstreamHandler} implementation for use in the bulk
 * insert {@link ChannelPipeline}.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class BulkInsertHandler extends AbstractBenchmarkHandler {
	private final BulkInsertConnectionTimers connectionTimers;
	private final BulkInsertDocuments documents;
	private final String bulkInsertPath;
	private final SendDataChannelFuture sendDataChannelFuture;
	
	private int insertOperationsCompleted;
	private boolean readingChunks;
	
	public BulkInsertHandler(BulkInsertConnectionTimers connectionTimers,
			BulkInsertDocuments documents, String bulkInsertPath, ResponseHandler responseHandler,
			CountDownLatch countDownLatch) {
		super(responseHandler, countDownLatch);
		
		this.connectionTimers = connectionTimers;
		this.documents = documents;
		this.bulkInsertPath = bulkInsertPath;
		this.sendDataChannelFuture = new SendDataChannelFuture();
		
		this.insertOperationsCompleted = 0;
	}

	/**
	 * The {@link ChannelFutureListener} called after a bulk insert is sent.
	 */
	private final class SendDataChannelFuture implements ChannelFutureListener {
		@Override
		public void operationComplete(ChannelFuture channelFuture) throws Exception {
			// Guard against starting RECEIVE_DATA before this listener runs. 
			if (connectionTimers.getRunningConnectionTimer() == RunningConnectionTimer.SEND_DATA) {
				connectionTimers.startRemoteProcessing();
			}
		}
	}
	
	private void writeNextBulkInsertOrClose(Channel channel) {
		if (insertOperationsCompleted < documents.size()) {
			// Perform the next bulk insert operation.
			writeNextBulkInsert(channel);
		} else {
			// There are no more bulk insert operations to perform.
			close(channel);
		}
	}
	
	private void writeNextBulkInsert(Channel channel) {
		connectionTimers.startLocalProcessing();
		HttpRequest request = new DefaultHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.POST, bulkInsertPath);
		ChannelBuffer insertBuffer = documents.getBuffer(insertOperationsCompleted);
		// Assign the headers.
		request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		// request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
		request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
		request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, insertBuffer.readableBytes());
		// Assign the body.
		request.setContent(insertBuffer);
		
		connectionTimers.startSendData();
		ChannelFuture channelFuture = channel.write(request);
		channelFuture.addListener(sendDataChannelFuture);
		insertOperationsCompleted++;
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		// Immediately perform the first bulk insert upon connecting.
		writeNextBulkInsert(e.getChannel());
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		connectionTimers.startReceiveData();
		
		Channel channel = e.getChannel();
		if (!readingChunks) {
			HttpResponse response = (HttpResponse) e.getMessage();
			responseHandler.setStatusCode(response.getStatus());
			
			if (response.isChunked()) {
				readingChunks = true;
			} else {
				ChannelBuffer content = response.getContent();
				if (content.readable()) {
					String body = content.toString(CharsetUtil.UTF_8);
					responseHandler.appendBody(body);
					writeNextBulkInsertOrClose(channel);
				}
			}
		} else {
			HttpChunk chunk = (HttpChunk) e.getMessage();
			if (chunk.isLast()) {
				readingChunks = false;
				responseHandler.endBody();
				writeNextBulkInsertOrClose(channel);
			} else {
				String body = chunk.getContent().toString(CharsetUtil.UTF_8);
				responseHandler.appendBody(body);
			}
		}
	}
}
