package co.adhoclabs.ironcushion;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import co.adhoclabs.ironcushion.HttpReactor.ResponseHandler;

/**
 * Abstract superclass for handlers used in the benchmark.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public abstract class AbstractBenchmarkHandler extends SimpleChannelUpstreamHandler {
	protected final ResponseHandler responseHandler;
	protected final CountDownLatch countDownLatch;

	protected AbstractBenchmarkHandler(
			ResponseHandler responseHandler, CountDownLatch countDownLatch) {
		this.responseHandler = responseHandler;
		this.countDownLatch = countDownLatch;
	}
	
	protected void close(Channel channel) {
		ChannelFuture channelFuture = channel.close();
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
				// Allow the main thread to continue.
				countDownLatch.countDown();
			}
		});
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		
		close(e.getChannel());
	}
}
