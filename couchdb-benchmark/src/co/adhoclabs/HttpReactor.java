package co.adhoclabs;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
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
		private boolean readingChunks;

		private final BulkInsertDocuments documents;
		private final String uri;
		private int insertOperation;
		
		private BulkInsertHandler(BulkInsertDocuments documents, String uri) {
			this.documents = documents;
			this.uri = uri;
			this.insertOperation = 0;
		}

		private void writeNextBulkInsert(Channel channel) {
			HttpRequest request = new DefaultHttpRequest(
					HttpVersion.HTTP_1_1, HttpMethod.POST, uri);
			request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

			ChannelBuffer insertBuffer = documents.getBuffer(insertOperation);
			request.setContent(insertBuffer);
			insertOperation++;
			
			channel.write(request);
		}
		
		@Override
		public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
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
		private int connectionNum = 0;
		private final List<BulkInsertDocuments> allBulkInsertDocuments;
		private final String bulkInsertUri;
		
		private BulkInsertPipeline(
				List<BulkInsertDocuments> allBulkInsertDocuments, String bulkInsertUri) {
			this.allBulkInsertDocuments = allBulkInsertDocuments;
			this.bulkInsertUri = bulkInsertUri;
		}
		
		@Override
		public ChannelPipeline getPipeline() throws Exception {
			BulkInsertDocuments documents = allBulkInsertDocuments.get(connectionNum);
			connectionNum++;
			return Channels.pipeline(new BulkInsertHandler(documents, bulkInsertUri));
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
					allBulkInsertDocuments, bulkInsertUri);
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
