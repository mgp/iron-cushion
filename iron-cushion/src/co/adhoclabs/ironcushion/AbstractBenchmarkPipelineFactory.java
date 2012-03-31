package co.adhoclabs.ironcushion;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.ChannelPipelineFactory;

/**
 * Abstract superclass for channel pipelines used in the benchmark.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public abstract class AbstractBenchmarkPipelineFactory implements ChannelPipelineFactory {
	protected final CountDownLatch countDownLatch;

	protected AbstractBenchmarkPipelineFactory(int numConnections) {
		this.countDownLatch = new CountDownLatch(numConnections);
	}
	
	public CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}
}
