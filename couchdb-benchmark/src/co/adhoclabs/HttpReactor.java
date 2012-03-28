package co.adhoclabs;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import co.adhoclabs.ConnectionTimers.ConnectionTimes;
import co.adhoclabs.bulkinsert.BulkInsertDocuments;
import co.adhoclabs.bulkinsert.BulkInsertPipelineFactory;
import co.adhoclabs.crud.CrudPipelineFactory;

/**
 * The networking engine that asynchronously executes HTTP requests.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class HttpReactor {
	private final int numConnections;
	private final InetSocketAddress databaseAddress;
	
	/**
	 * Interface for a consumer of HTTP responses.
	 */
	public interface ResponseHandler {
		public void setStatusCode(HttpResponseStatus statusCode);
		public void appendBody(CharSequence string);
		public void endBody();
	}
	
	/**
	 * A {@link ResponseHandler} that prints the response to {@link System#out}.
	 */
	public static final class PrintResponseHandler implements ResponseHandler {
		public static final PrintResponseHandler INSTANCE = new PrintResponseHandler();
		
		private PrintResponseHandler() {
		}
		public void setStatusCode(HttpResponseStatus statusCode) {
			System.out.print("status=");
			System.out.println(statusCode);
			System.out.flush();
		}
		public void appendBody(CharSequence string) {
			System.out.print(string);
			System.out.flush();
		}
		public void endBody() {
			System.out.print("\n\n");
			System.out.flush();
		}
	}
	
	/**
	 * A {@link ResponseHandler} that discards the response.
	 */
	public static final class NullResponseHandler implements ResponseHandler {
		public static final NullResponseHandler INSTANCE = new NullResponseHandler();
		
		private NullResponseHandler() {
		}
		public void setStatusCode(HttpResponseStatus statusCode) {
			// Do nothing.
		}
		public void appendBody(CharSequence string) {
			// Do nothing.
		}
		public void endBody() {
			// Do nothing.
		}
	}
	
	public HttpReactor(int numConnections, InetSocketAddress databaseAddress) {
		this.numConnections = numConnections;
		this.databaseAddress = databaseAddress;
	}
	
	private List<ConnectionTimes> run(AbstractBenchmarkPipelineFactory channelPipelineFactory)
			throws BenchmarkException {
		try {
			// Create the connections to the server.
			ClientBootstrap clientBootstrap = new ClientBootstrap(
					new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
			clientBootstrap.setPipelineFactory(channelPipelineFactory);
			for (int i = 0; i < numConnections; ++i) {
				clientBootstrap.connect(databaseAddress);
			}
			
			// Wait for all connections to complete their tasks.
			channelPipelineFactory.getCountDownLatch().await();
			// Shut down executor threads to exit.
			clientBootstrap.releaseExternalResources();
			
			// Return the elapsed time of each connection.
			List<ConnectionTimes> allConnectionTimes = new ArrayList<ConnectionTimes>(numConnections);
			for (ConnectionTimers connectionTimers : channelPipelineFactory.getAllConnectionTimers()) {
				allConnectionTimes.add(connectionTimers.getConnectionTimes());
			}
			return allConnectionTimes;
		} catch (InterruptedException e) {
			throw new BenchmarkException(e);
		}
	}
	
	public List<ConnectionTimes> performBulkInserts(List<BulkInsertDocuments> allBulkInsertDocuments,
			String bulkInsertPath) throws BenchmarkException {
		BulkInsertPipelineFactory bulkInsertPipelineFactory = new BulkInsertPipelineFactory(
				numConnections, allBulkInsertDocuments, bulkInsertPath,
				NullResponseHandler.INSTANCE);
		return run(bulkInsertPipelineFactory);
	}
	
	public List<ConnectionTimes> performCrudOperations(String crudPath) throws Exception {
		CrudPipelineFactory crudPipelineFactory = new CrudPipelineFactory(
				numConnections, crudPath, NullResponseHandler.INSTANCE);
		return run(crudPipelineFactory);
	}
}
