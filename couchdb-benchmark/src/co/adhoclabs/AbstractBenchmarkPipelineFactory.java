package co.adhoclabs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.ChannelPipelineFactory;

import co.adhoclabs.HttpReactor.ResponseHandler;

public abstract class AbstractBenchmarkPipelineFactory implements ChannelPipelineFactory {
	protected final List<ConnectionTimers> allConnectionTimers;
	protected final CountDownLatch countDownLatch;
	protected final ResponseHandler responseHandler;

	protected AbstractBenchmarkPipelineFactory(int numConnections,
			ResponseHandler responseHandler) {
		this.allConnectionTimers = new ArrayList<ConnectionTimers>();
		for (int i = 0; i < numConnections; ++i) {
			allConnectionTimers.add(new ConnectionTimers());
		}
		this.countDownLatch = new CountDownLatch(numConnections);
		this.responseHandler = responseHandler;
	}
	
	public List<ConnectionTimers> getAllConnectionTimers() {
		return allConnectionTimers;
	}
	
	public CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}
}
