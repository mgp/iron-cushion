package co.adhoclabs.bulkinsert;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

import co.adhoclabs.ConnectionTimers;
import co.adhoclabs.HttpReactor.ResponseHandler;

/**
 * The {@link ChannelPipelineFactory} for connections that perform bulk inserts.
 */
public class BulkInsertPipelineFactory implements ChannelPipelineFactory {
	private final List<ConnectionTimers> allConnectionTimers;
	private final List<BulkInsertDocuments> allBulkInsertDocuments;
	private final String bulkInsertPath;
	private final ResponseHandler responseHandler;
	private final CountDownLatch countDownLatch;
	
	private int connectionNum;
	
	public BulkInsertPipelineFactory(List<ConnectionTimers> allConnectionTimers,
			List<BulkInsertDocuments> allBulkInsertDocuments, String bulkInsertPath,
			ResponseHandler responseHandler, CountDownLatch countDownLatch) {
		this.allConnectionTimers = allConnectionTimers;
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
