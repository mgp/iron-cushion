package co.adhoclabs;

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
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 * The networking engine that asynchronously executes HTTP requests.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class HttpReactor {
	ClientBootstrap clientBootstrap;
	
	private static final class BulkInsertHandler extends SimpleChannelUpstreamHandler {
		private final BulkInsertDocuments documents;
		private final String uri;
		private final CountDownLatch countDownLatch;
		
		private int insertOperationsCompleted;
		private boolean readingChunks;
		
		private BulkInsertHandler(BulkInsertDocuments documents, String uri, CountDownLatch countDownLatch) {
			this.documents = documents;
			this.uri = uri;
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
					HttpVersion.HTTP_1_1, HttpMethod.POST, uri);
			request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
			// Assign the body.
			ChannelBuffer insertBuffer = documents.getBuffer(insertOperationsCompleted);
			request.setContent(insertBuffer);
			
			channel.write(request);
			insertOperationsCompleted++;
		}
		
		void close(Channel channel) {
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
			if (!readingChunks) {
				HttpResponse response = (HttpResponse) e.getMessage();
				
				if (!response.isChunked()) {
					readingChunks = true;
				} else {
					ChannelBuffer content = response.getContent();
					if (content.readable()) {
						// TODO
					}
				}
			} else {
				HttpChunk chunk = (HttpChunk) e.getMessage();
				if (chunk.isLast()) {
					readingChunks = false;
				} else {
					
				}
			}
		}
	}
	
	private static final class BulkInsertPipeline implements ChannelPipelineFactory {
		private final List<BulkInsertDocuments> allBulkInsertDocuments;
		private final String bulkInsertUri;
		private final CountDownLatch countDownLatch;
		
		private int connectionNum;
		
		private BulkInsertPipeline(
				List<BulkInsertDocuments> allBulkInsertDocuments, String bulkInsertUri,
				CountDownLatch countDownLatch) {
			this.allBulkInsertDocuments = allBulkInsertDocuments;
			this.bulkInsertUri = bulkInsertUri;
			this.countDownLatch = countDownLatch;
			
			connectionNum = 0;
		}
		
		@Override
		public ChannelPipeline getPipeline() throws Exception {
			BulkInsertDocuments documents = allBulkInsertDocuments.get(connectionNum);
			connectionNum++;
			return Channels.pipeline(new BulkInsertHandler(documents, bulkInsertUri, countDownLatch));
		}
	}
	
	public HttpReactor(int numConnections,
			List<BulkInsertDocuments> allBulkInsertDocuments, String bulkInsertUri)
			throws BenchmarkException {
		try {
			CountDownLatch countDownLatch = new CountDownLatch(numConnections);
			
			clientBootstrap = new ClientBootstrap(
					new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
			BulkInsertPipeline bulkInsertPipeline = new BulkInsertPipeline(
					allBulkInsertDocuments, bulkInsertUri, countDownLatch);
			clientBootstrap.setPipelineFactory(bulkInsertPipeline);
			
			for (int i = 0; i < numConnections; ++i) {
				clientBootstrap.connect();
			}
			
			countDownLatch.await();
		} catch (InterruptedException e) {
			throw new BenchmarkException(e);
		}
	}
	
	public void performBulkInserts() {
		// TODO
	}
}
