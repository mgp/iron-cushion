package co.adhoclabs.ironcushion;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.ChannelPipelineFactory;

import co.adhoclabs.ironcushion.HttpReactor.ResponseHandler;

/**
 * Abstract superclass for channel pipelines used in the benchmark.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public abstract class AbstractBenchmarkPipelineFactory implements ChannelPipelineFactory {
	protected final CountDownLatch countDownLatch;
	protected final ResponseHandler responseHandler;

	protected AbstractBenchmarkPipelineFactory(int numConnections,
			ResponseHandler responseHandler) {
		this.countDownLatch = new CountDownLatch(numConnections);
		this.responseHandler = responseHandler;
	}
	
	public CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}
}
