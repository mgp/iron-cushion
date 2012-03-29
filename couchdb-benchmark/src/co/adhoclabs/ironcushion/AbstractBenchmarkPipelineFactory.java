package co.adhoclabs.ironcushion;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.ChannelPipelineFactory;

import co.adhoclabs.ironcushion.HttpReactor.ResponseHandler;

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
