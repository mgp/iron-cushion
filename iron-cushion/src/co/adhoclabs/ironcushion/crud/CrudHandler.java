package co.adhoclabs.ironcushion.crud;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONObject;

import co.adhoclabs.ironcushion.AbstractBenchmarkHandler;
import co.adhoclabs.ironcushion.HttpReactor.ResponseHandler;
import co.adhoclabs.ironcushion.crud.CrudConnectionTimers.RunningConnectionTimer;

/**
 * The {@link SimpleChannelUpstreamHandler} implementation for use in the CRUD
 * operation {@link ChannelPipeline}.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class CrudHandler extends AbstractBenchmarkHandler {
	private final CrudConnectionTimers connectionTimers;
	private final CrudOperations crudOperations;
	private final String crudPath;
	
	private final SendCreateDataChannelFuture sendCreateDataChannelFuture;
	private final SendReadDataChannelFuture sendReadDataChannelFuture;
	private final SendUpdateDataChannelFuture sendUpdateDataChannelFuture;
	private final SendDeleteDataChannelFuture sendDeleteDataChannelFuture;
	
	private JSONObject document;
	private int crudOperationsCompleted;
	private boolean readingChunks;
	
	public CrudHandler(CrudConnectionTimers connectionTimers,
			CrudOperations crudOperations, String crudPath, ResponseHandler responseHandler,
			CountDownLatch countDownLatch) {
		super(responseHandler, countDownLatch);
		
		this.connectionTimers = connectionTimers;
		this.crudOperations = crudOperations;
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
	
	private String getDocumentPath(String documentId) {
		// TODO: Optimize this.
		StringBuilder sb = new StringBuilder();
		sb.append('/').append(crudPath);
		sb.append('/').append(documentId);
		return sb.toString();
	}
	
	private String getDocumentDeletePath(String documentId, String revision) {
		// TODO: Optimize this.
		StringBuilder sb = new StringBuilder();
		sb.append('/').append(crudPath);
		sb.append('/').append(documentId);
		sb.append("?rev=").append(revision);
		return sb.toString();
	}
	
	private void performOperation(Channel channel,
			String documentPath, HttpMethod method, ChannelBuffer contentBuffer,
			ChannelFutureListener channelFutureListener) {
		HttpRequest request = new DefaultHttpRequest(
				HttpVersion.HTTP_1_1, method, documentPath);
		// Assign the headers.
		request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		// request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
		request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
		if (contentBuffer != null) {
			request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, contentBuffer.readableBytes());
			// Assign the body if present.
			request.setContent(contentBuffer);
		}
		
		connectionTimers.startSendData();
		ChannelFuture channelFuture = channel.write(request);
		channelFuture.addListener(channelFutureListener);
	}
	
	@SuppressWarnings("unchecked")
	private void performCreateOperation(Channel channel) {
		connectionTimers.startLocalProcessing();
		
		// TODO: create the document
		document = null;
		String documentId = String.valueOf(crudOperations.getNextCreateId());
		document.put("_id", documentId);
		String documentPath = getDocumentPath(documentId);
		ChannelBuffer insertBuffer = ChannelBuffers.copiedBuffer(
				document.toString(), CharsetUtil.UTF_8);
		performOperation(channel, documentPath, HttpMethod.POST, insertBuffer, sendCreateDataChannelFuture);
	}
	
	private void performReadOperation(Channel channel) {
		connectionTimers.startLocalProcessing();
		
		document = null;
		String documentId = String.valueOf(crudOperations.getNextReadId());
		String documentPath = getDocumentPath(documentId);
		performOperation(channel, documentPath, HttpMethod.GET, null, sendReadDataChannelFuture);
	}
	
	private void performUpdateOperation(Channel channel) {
		connectionTimers.startLocalProcessing();
		
		String documentId = (String) document.get("_id");
		String documentPath = getDocumentPath(documentId);
		// TODO: update the document
		ChannelBuffer updateBuffer = ChannelBuffers.copiedBuffer(
				document.toString(), CharsetUtil.UTF_8);
		performOperation(channel, documentPath, HttpMethod.PUT, updateBuffer, sendUpdateDataChannelFuture);
	}
	
	private void performDeleteOperation(Channel channel) {
		connectionTimers.startLocalProcessing();
		
		String documentId = (String) document.get("_id");
		String revision = (String) document.get("_rev");
		String documentPath = getDocumentDeletePath(documentId, revision);
		performOperation(channel, documentPath, HttpMethod.DELETE, null, sendDeleteDataChannelFuture);
	}
	
	private void performNextOperationOrClose(Channel channel) {
		if (crudOperationsCompleted < crudOperations.size()) {
			// Perform the next CRUD operation.
			switch (crudOperations.getOperation(crudOperationsCompleted)) {
			case CREATE:
				performCreateOperation(channel);
				break;
			case READ:
				performReadOperation(channel);
				break;
			case UPDATE:
				performUpdateOperation(channel);
				break;
			case DELETE:
				performDeleteOperation(channel);
				break;
			default:
				break;
			}
		} else {
			// There are no more CRUD operations to perform.
			close(channel);
		}
	}
	
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Channel channel = ctx.getChannel();
		JSONObject json = null;
		
		switch (crudOperations.getOperation(crudOperationsCompleted)) {
		case CREATE:
			
			break;
		case READ:
			
			break;
		case UPDATE:
			
			break;
		case DELETE:
			
			break;
		default:
			break;
		}
		
		crudOperationsCompleted++;
		performNextOperationOrClose(channel);
	}
}
