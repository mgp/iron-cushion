package co.adhoclabs.crud;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

import co.adhoclabs.AbstractBenchmarkPipelineFactory;
import co.adhoclabs.HttpReactor.ResponseHandler;

/**
 * The {@link ChannelPipelineFactory} for connections that perform CRUD operations.
 */
public class CrudPipelineFactory extends AbstractBenchmarkPipelineFactory {
	private final List<CrudConnectionTimers> allConnectionTimers;
	private final String crudPath;

	private int connectionNum;
	
	public CrudPipelineFactory(int numConnections,
			String crudPath, ResponseHandler responseHandler) {
		super(numConnections, responseHandler);
		
		this.allConnectionTimers = new ArrayList<CrudConnectionTimers>(numConnections);
		for (int i = 0; i < numConnections; ++i) {
			this.allConnectionTimers.add(new CrudConnectionTimers());
		}
		// TODO: set crud operations
		this.crudPath = crudPath;
		
		connectionNum = 0;
	}
	
	/**
	 * @return the {@link CrudConnectionTimers} used by connections
	 */
	public List<CrudConnectionTimers> getAllConnectionTimers() {
		return allConnectionTimers;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		CrudConnectionTimers connectionTimers = allConnectionTimers.get(connectionNum);
		// TODO: get crud operations
		connectionNum++;
		return Channels.pipeline(
				new HttpClientCodec(),
				new CrudHandler(connectionTimers, crudPath, responseHandler, countDownLatch)
				);
	}
}
