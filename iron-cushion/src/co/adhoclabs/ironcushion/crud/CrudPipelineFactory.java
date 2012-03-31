package co.adhoclabs.ironcushion.crud;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

import co.adhoclabs.ironcushion.AbstractBenchmarkPipelineFactory;

/**
 * The {@link ChannelPipelineFactory} for connections that perform CRUD operations.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class CrudPipelineFactory extends AbstractBenchmarkPipelineFactory {
	private final List<CrudConnectionStatistics> allConnectionStatistics;
	private final List<CrudOperations> allCrudOperations;
	private final String crudPath;

	private int connectionNum;
	
	public CrudPipelineFactory(int numConnections,
			List<CrudOperations> allCrudOperations, String crudPath) {
		super(numConnections);
		
		this.allConnectionStatistics = new ArrayList<CrudConnectionStatistics>(numConnections);
		for (int i = 0; i < numConnections; ++i) {
			this.allConnectionStatistics.add(new CrudConnectionStatistics());
		}
		this.allCrudOperations = allCrudOperations;
		this.crudPath = crudPath;
		
		connectionNum = 0;
	}
	
	/**
	 * @return the {@link CrudConnectionStatistics} used by connections
	 */
	public List<CrudConnectionStatistics> getAllConnectionStatistics() {
		return allConnectionStatistics;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		CrudConnectionStatistics connectionStatistics = allConnectionStatistics.get(connectionNum);
		CrudOperations crudOperations = allCrudOperations.get(connectionNum);
		connectionNum++;
		return Channels.pipeline(
				new HttpClientCodec(),
				// new HttpContentDecompressor(),
				new CrudHandler(connectionStatistics, crudOperations, crudPath, countDownLatch)
				);
	}
}
