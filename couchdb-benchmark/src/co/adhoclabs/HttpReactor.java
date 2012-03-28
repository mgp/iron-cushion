package co.adhoclabs;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import co.adhoclabs.ConnectionTimers.ConnectionTimes;
import co.adhoclabs.ConnectionTimers.RunningConnectionTimer;

/**
 * The networking engine that asynchronously executes HTTP requests.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class HttpReactor {
	private final int numConnections;
	
	/**
	 * Interface for a consumer of HTTP responses.
	 */
	public interface ResponseHandler {
		public void setStatusCode(HttpResponseStatus statusCode);
		public void appendBody(CharSequence string);
		public void endBody();
	}
	
	/**
	 * A {@link ResponseHandler} that prints the response to {@link System#out}.
	 */
	public static final class PrintResponseHandler implements ResponseHandler {
		public static final PrintResponseHandler INSTANCE = new PrintResponseHandler();
		
		private PrintResponseHandler() {
		}
		public void setStatusCode(HttpResponseStatus statusCode) {
			System.out.print("status=");
			System.out.println(statusCode);
			System.out.flush();
		}
		public void appendBody(CharSequence string) {
			System.out.print(string);
			System.out.flush();
		}
		public void endBody() {
			System.out.print("\n\n");
			System.out.flush();
		}
	}
	
	/**
	 * A {@link ResponseHandler} that discards the response.
	 */
	public static final class NullResponseHandler implements ResponseHandler {
		public static final NullResponseHandler INSTANCE = new NullResponseHandler();
		
		private NullResponseHandler() {
		}
		public void setStatusCode(HttpResponseStatus statusCode) {
			// Do nothing.
		}
		public void appendBody(CharSequence string) {
			// Do nothing.
		}
		public void endBody() {
			// Do nothing.
		}
	}
	
	private static final class BulkInsertHandler extends SimpleChannelUpstreamHandler {
		private final ConnectionTimers connectionTimers;
		private final BulkInsertDocuments documents;
		private final String bulkInsertPath;
		private final ResponseHandler responseHandler;
		private final CountDownLatch countDownLatch;
		
		private int insertOperationsCompleted;
		private boolean readingChunks;
		
		private BulkInsertHandler(ConnectionTimers connectionTimers,
				BulkInsertDocuments documents, String bulkInsertPath, ResponseHandler responseHandler,
				CountDownLatch countDownLatch) {
			this.connectionTimers = connectionTimers;
			this.documents = documents;
			this.bulkInsertPath = bulkInsertPath;
			this.responseHandler = responseHandler;
			this.countDownLatch = countDownLatch;
			
			this.insertOperationsCompleted = 0;
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
			// Assign the headers.
			HttpRequest request = new DefaultHttpRequest(
					HttpVersion.HTTP_1_1, HttpMethod.POST, bulkInsertPath);
			connectionTimers.startLocalProcessing();
			ChannelBuffer insertBuffer = documents.getBuffer(insertOperationsCompleted);
			// Assign the headers.
			request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
			request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
			request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, insertBuffer.readableBytes());
			// Assign the body.
			request.setContent(insertBuffer);
			
			connectionTimers.startSendData();
			ChannelFuture channelFuture = channel.write(request);
			channelFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception {
					// Guard against starting RECEIVE_DATA before this listener runs. 
					if (connectionTimers.getRunningConnectionTimer() == RunningConnectionTimer.SEND_DATA) {
						connectionTimers.startRemoteProcessing();
					}
				}
			});
			insertOperationsCompleted++;
		}
		
		private void close(Channel channel) {
			connectionTimers.stop();
			
			ChannelFuture channelFuture = channel.close();
			channelFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception {
					// Allow the main thread to continue.
					countDownLatch.countDown();
				}
			});
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
		
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			e.getCause().printStackTrace();
			
			close(e.getChannel());
		}
	}
	
	private static final class BulkInsertPipeline implements ChannelPipelineFactory {
		private final List<ConnectionTimers> allConnectionTimers;
		private final List<BulkInsertDocuments> allBulkInsertDocuments;
		private final String bulkInsertPath;
		private final ResponseHandler responseHandler;
		private final CountDownLatch countDownLatch;
		
		private int connectionNum;
		
		private BulkInsertPipeline(int numConnections,
				List<BulkInsertDocuments> allBulkInsertDocuments, String bulkInsertPath,
				ResponseHandler responseHandler, CountDownLatch countDownLatch) {
			allConnectionTimers = new ArrayList<ConnectionTimers>(numConnections);
			for (int i = 0; i < numConnections; ++i) {
				allConnectionTimers.add(new ConnectionTimers());
			}
			this.allBulkInsertDocuments = allBulkInsertDocuments;
			this.bulkInsertPath = bulkInsertPath;
			this.responseHandler = responseHandler;
			this.countDownLatch = countDownLatch;
			
			connectionNum = 0;
		}
		
		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ConnectionTimers connectionTimers = allConnectionTimers.get(connectionNum);
			BulkInsertDocuments documents = allBulkInsertDocuments.get(connectionNum);
			connectionNum++;
			return Channels.pipeline(
					new HttpClientCodec(),
					// new HttpContentDecompressor(),
					new BulkInsertHandler(connectionTimers, documents, bulkInsertPath, responseHandler, countDownLatch)
					);
		}
	}
	
	public HttpReactor(int numConnections) {
		this.numConnections = numConnections;
	}
	
	public List<ConnectionTimes> performBulkInserts(List<BulkInsertDocuments> allBulkInsertDocuments,
			InetSocketAddress databaseAddress, String bulkInsertPath) throws BenchmarkException {
		try {
			CountDownLatch countDownLatch = new CountDownLatch(numConnections);

			ClientBootstrap clientBootstrap = new ClientBootstrap(
					new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
			
			BulkInsertPipeline bulkInsertPipeline = new BulkInsertPipeline(numConnections,
					allBulkInsertDocuments, bulkInsertPath, NullResponseHandler.INSTANCE, countDownLatch);
			clientBootstrap.setPipelineFactory(bulkInsertPipeline);
			
			for (int i = 0; i < numConnections; ++i) {
				clientBootstrap.connect(databaseAddress);
			}
			
			// Wait for all connections to complete their bulk insert operations.
			countDownLatch.await();
			// Shut down executor threads to exit.
			clientBootstrap.releaseExternalResources();
			
			List<ConnectionTimes> allConnectionTimes = new ArrayList<ConnectionTimes>(numConnections);
			for (ConnectionTimers connectionTimers : bulkInsertPipeline.allConnectionTimers) {
				allConnectionTimes.add(connectionTimers.getConnectionTimes());
			}
			return allConnectionTimes;
		} catch (InterruptedException e) {
			throw new BenchmarkException(e);
		}
	}
}
