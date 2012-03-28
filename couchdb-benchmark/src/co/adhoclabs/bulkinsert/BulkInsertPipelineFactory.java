package co.adhoclabs.bulkinsert;

import java.util.List;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

import co.adhoclabs.AbstractBenchmarkPipelineFactory;
import co.adhoclabs.ConnectionTimers;
import co.adhoclabs.HttpReactor.ResponseHandler;

/**
 * The {@link ChannelPipelineFactory} for connections that perform bulk inserts.
 */
public class BulkInsertPipelineFactory extends AbstractBenchmarkPipelineFactory {
	private final List<BulkInsertDocuments> allBulkInsertDocuments;
	private final String bulkInsertPath;
	
	private int connectionNum;
	
	public BulkInsertPipelineFactory(int numConnections,
			List<BulkInsertDocuments> allBulkInsertDocuments, String bulkInsertPath,
			ResponseHandler responseHandler) {
		super(numConnections, responseHandler);
		
		this.allBulkInsertDocuments = allBulkInsertDocuments;
		this.bulkInsertPath = bulkInsertPath;
		
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
