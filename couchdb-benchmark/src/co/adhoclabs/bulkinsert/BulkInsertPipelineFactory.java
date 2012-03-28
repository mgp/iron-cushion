package co.adhoclabs.bulkinsert;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

import co.adhoclabs.AbstractBenchmarkPipelineFactory;
import co.adhoclabs.HttpReactor.ResponseHandler;

/**
 * The {@link ChannelPipelineFactory} for connections that perform bulk inserts.
 */
public class BulkInsertPipelineFactory extends AbstractBenchmarkPipelineFactory {
	private final List<BulkInsertConnectionTimers> allConnectionTimers;
	private final List<BulkInsertDocuments> allBulkInsertDocuments;
	private final String bulkInsertPath;
	
	private int connectionNum;
	
	public BulkInsertPipelineFactory(int numConnections,
			List<BulkInsertDocuments> allBulkInsertDocuments, String bulkInsertPath,
			ResponseHandler responseHandler) {
		super(numConnections, responseHandler);
		
		this.allConnectionTimers = new ArrayList<BulkInsertConnectionTimers>();
		for (int i = 0; i < numConnections; ++i) {
			allConnectionTimers.add(new BulkInsertConnectionTimers());
		}
		this.allBulkInsertDocuments = allBulkInsertDocuments;
		this.bulkInsertPath = bulkInsertPath;
		
		connectionNum = 0;
	}

	/**
	 * @return the {@link BulkInsertConnectionTimers} used by connections
	 */
	public List<BulkInsertConnectionTimers> getAllConnectionTimers() {
		return allConnectionTimers;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		BulkInsertConnectionTimers connectionTimers = allConnectionTimers.get(connectionNum);
		BulkInsertDocuments documents = allBulkInsertDocuments.get(connectionNum);
		connectionNum++;
		return Channels.pipeline(
				new HttpClientCodec(),
				// new HttpContentDecompressor(),
				new BulkInsertHandler(connectionTimers, documents, bulkInsertPath, responseHandler, countDownLatch)
				);
	}
}
