package co.adhoclabs.crud;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.json.simple.JSONObject;

import co.adhoclabs.AbstractBenchmarkHandler;
import co.adhoclabs.HttpReactor.ResponseHandler;
import co.adhoclabs.crud.CrudConnectionTimers.RunningConnectionTimer;

/**
 * The {@link SimpleChannelUpstreamHandler} implementation for use in the CRUD
 * operation {@link ChannelPipeline}.
 */
public class CrudHandler extends AbstractBenchmarkHandler {
	private final CrudConnectionTimers connectionTimers;
	private final String crudPath;
	private final SendCreateDataChannelFuture sendCreateDataChannelFuture;
	private final SendReadDataChannelFuture sendReadDataChannelFuture;
	private final SendUpdateDataChannelFuture sendUpdateDataChannelFuture;
	private final SendDeleteDataChannelFuture sendDeleteDataChannelFuture;
	
	private CrudOperations crudOperations;
	private int crudOperationsCompleted;
	private JSONObject document;
	private boolean readingChunks;
	
	public CrudHandler(CrudConnectionTimers connectionTimers,
			String crudPath, ResponseHandler responseHandler,
			CountDownLatch countDownLatch) {
		super(responseHandler, countDownLatch);
		
		this.connectionTimers = connectionTimers;
		this.crudPath = crudPath;
		this.sendCreateDataChannelFuture = new SendCreateDataChannelFuture();
		this.sendReadDataChannelFuture = new SendReadDataChannelFuture();
		this.sendUpdateDataChannelFuture = new SendUpdateDataChannelFuture();
		this.sendDeleteDataChannelFuture = new SendDeleteDataChannelFuture();
		
		this.crudOperationsCompleted = 0;
	}
	
	/**
	 * The The {@link ChannelFutureListener} called after a create operation is sent.
	 */
	private final class SendCreateDataChannelFuture implements ChannelFutureListener {
		@Override
		public void operationComplete(ChannelFuture channelFuture) throws Exception {
			// Guard against starting RECEIVE_DATA before this listener runs. 
			if (connectionTimers.getRunningConnectionTimer() == RunningConnectionTimer.SEND_DATA) {
				connectionTimers.startRemoteCreateProcessing();
			}
		}
	}
	
	/**
	 * The The {@link ChannelFutureListener} called after a read operation is sent.
	 */
	private final class SendReadDataChannelFuture implements ChannelFutureListener {
		@Override
		public void operationComplete(ChannelFuture channelFuture) throws Exception {
			// Guard against starting RECEIVE_DATA before this listener runs. 
			if (connectionTimers.getRunningConnectionTimer() == RunningConnectionTimer.SEND_DATA) {
				connectionTimers.startRemoteReadProcessing();
			}
		}
	}
	
	/**
	 * The The {@link ChannelFutureListener} called after an update operation is sent.
	 */
	private final class SendUpdateDataChannelFuture implements ChannelFutureListener {
		@Override
		public void operationComplete(ChannelFuture channelFuture) throws Exception {
			// Guard against starting RECEIVE_DATA before this listener runs. 
			if (connectionTimers.getRunningConnectionTimer() == RunningConnectionTimer.SEND_DATA) {
				connectionTimers.startRemoteUpdateProcessing();
			}
		}
	}
	
	/**
	 * The The {@link ChannelFutureListener} called after a delete operation is sent.
	 */
	private final class SendDeleteDataChannelFuture implements ChannelFutureListener {
		@Override
		public void operationComplete(ChannelFuture channelFuture) throws Exception {
			// Guard against starting RECEIVE_DATA before this listener runs. 
			if (connectionTimers.getRunningConnectionTimer() == RunningConnectionTimer.SEND_DATA) {
				connectionTimers.startRemoteDeleteProcessing();
			}
		}
	}
	
	private void writeOperation() {
		// TODO
	}
	
	private String pathForDocument(String documentId) {
		// TODO
		return null;
	}
	
	private void writeCreateOperation(Channel channel) {
		connectionTimers.startLocalProcessing();
		String documentPath = null;
		HttpRequest request = new DefaultHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.POST, documentPath);
		// Assign the headers.
		
		connectionTimers.startSendData();
		ChannelFuture channelFuture = channel.write(request);
		channelFuture.addListener(sendCreateDataChannelFuture);
	}
	
	private void writeReadOperation(Channel channel) {
		connectionTimers.startLocalProcessing();
	}
	
	private void writeUpdateOperation(Channel channel) {
		connectionTimers.startLocalProcessing();
	}
	
	private void readDeleteOperation(Channel channel) {
		connectionTimers.startLocalProcessing();
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
