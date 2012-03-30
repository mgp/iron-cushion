package co.adhoclabs.ironcushion.bulkinsert;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

import co.adhoclabs.ironcushion.AbstractBenchmarkPipelineFactory;
import co.adhoclabs.ironcushion.HttpReactor.ResponseHandler;

/**
 * The {@link ChannelPipelineFactory} for connections that perform bulk inserts.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class BulkInsertPipelineFactory extends AbstractBenchmarkPipelineFactory {
	private final List<BulkInsertConnectionStatistics> allConnectionStatistics;
	private final List<BulkInsertDocuments> allBulkInsertDocuments;
	private final String bulkInsertPath;
	
	private int connectionNum;
	
	public BulkInsertPipelineFactory(int numConnections,
			List<BulkInsertDocuments> allBulkInsertDocuments, String bulkInsertPath,
			ResponseHandler responseHandler) {
		super(numConnections, responseHandler);
		
		this.allConnectionStatistics = new ArrayList<BulkInsertConnectionStatistics>();
		for (int i = 0; i < numConnections; ++i) {
			allConnectionStatistics.add(new BulkInsertConnectionStatistics());
		}
		this.allBulkInsertDocuments = allBulkInsertDocuments;
		this.bulkInsertPath = bulkInsertPath;
		
		connectionNum = 0;
	}

	/**
	 * @return the {@link BulkInsertConnectionStatistics} used by connections
	 */
	public List<BulkInsertConnectionStatistics> getAllConnectionStatistics() {
		return allConnectionStatistics;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		BulkInsertConnectionStatistics connectionStatistics = allConnectionStatistics.get(connectionNum);
		BulkInsertDocuments documents = allBulkInsertDocuments.get(connectionNum);
		connectionNum++;
		return Channels.pipeline(
				new HttpClientCodec(),
				// new HttpContentDecompressor(),
				new BulkInsertHandler(connectionStatistics, documents, bulkInsertPath, responseHandler, countDownLatch)
				);
	}
}
