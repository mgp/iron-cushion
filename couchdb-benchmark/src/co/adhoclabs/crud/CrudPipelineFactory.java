package co.adhoclabs.crud;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

import co.adhoclabs.AbstractBenchmarkPipelineFactory;
import co.adhoclabs.ConnectionTimers;
import co.adhoclabs.HttpReactor.ResponseHandler;

/**
 * The {@link ChannelPipelineFactory} for connections that perform CRUD operations.
 */
public class CrudPipelineFactory extends AbstractBenchmarkPipelineFactory {
	private final String crudPath;

	private int connectionNum;
	
	public CrudPipelineFactory(int numConnections,
			String crudPath, ResponseHandler responseHandler) {
		super(numConnections, responseHandler);
		
		// TODO: set crud operations
		this.crudPath = crudPath;
		
		connectionNum = 0;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ConnectionTimers connectionTimers = allConnectionTimers.get(connectionNum);
		// TODO: get crud operations
		connectionNum++;
		return Channels.pipeline(
				new HttpClientCodec(),
				new CrudHandler(connectionTimers, crudPath, responseHandler, countDownLatch)
				);
	}
}
