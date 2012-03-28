package co.adhoclabs.crud;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import co.adhoclabs.AbstractBenchmarkHandler;
import co.adhoclabs.ConnectionTimers;
import co.adhoclabs.HttpReactor.ResponseHandler;

/**
 * The {@link SimpleChannelUpstreamHandler} implementation for use in the CRUD
 * operation {@link ChannelPipeline}.
 */
public class CrudHandler extends AbstractBenchmarkHandler {
	private final String crudPath;
	
	private int crudOperationsCompleted;
	private boolean readingChunks;
	
	public CrudHandler(ConnectionTimers connectionTimers,
			String crudPath, ResponseHandler responseHandler,
			CountDownLatch countDownLatch) {
		super(connectionTimers, responseHandler, countDownLatch);
		
		this.crudPath = crudPath;
		
		this.crudOperationsCompleted = 0;
	}
	
	private void performNextOperationOrClose(Channel channel) {
		if (true) {
			// Perform the next CRUD operation.
			
		} else {
			// There are no more CRUD operations to perform.
			close(channel);
		}
	}
}
