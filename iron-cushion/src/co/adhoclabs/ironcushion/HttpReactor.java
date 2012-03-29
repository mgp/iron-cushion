package co.adhoclabs.ironcushion;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import co.adhoclabs.ironcushion.bulkinsert.BulkInsertConnectionTimers;
import co.adhoclabs.ironcushion.bulkinsert.BulkInsertDocuments;
import co.adhoclabs.ironcushion.bulkinsert.BulkInsertPipelineFactory;
import co.adhoclabs.ironcushion.bulkinsert.BulkInsertConnectionTimers.BulkInsertConnectionTimes;
import co.adhoclabs.ironcushion.crud.CrudConnectionTimers;
import co.adhoclabs.ironcushion.crud.CrudOperations;
import co.adhoclabs.ironcushion.crud.CrudPipelineFactory;
import co.adhoclabs.ironcushion.crud.CrudConnectionTimers.CrudConnectionTimes;

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
	
	private void run(AbstractBenchmarkPipelineFactory channelPipelineFactory)
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
		} catch (InterruptedException e) {
			throw new BenchmarkException(e);
		}
	}
	
	public List<BulkInsertConnectionTimes> performBulkInserts(List<BulkInsertDocuments> allBulkInsertDocuments,
			String bulkInsertPath) throws BenchmarkException {
		// Run the bulk inserts.
		BulkInsertPipelineFactory bulkInsertPipelineFactory = new BulkInsertPipelineFactory(
				numConnections, allBulkInsertDocuments, bulkInsertPath,
				NullResponseHandler.INSTANCE);
		run(bulkInsertPipelineFactory);
		
		// Return the times for each connection.
		List<BulkInsertConnectionTimes> allConnectionTimes = new ArrayList<BulkInsertConnectionTimes>(numConnections);
		for (BulkInsertConnectionTimers connectionTimers : bulkInsertPipelineFactory.getAllConnectionTimers()) {
			allConnectionTimes.add(connectionTimers.getConnectionTimes());
		}
		return allConnectionTimes;
	}
	
	public List<CrudConnectionTimes> performCrudOperations(List<CrudOperations> allCrudOperations,
			String crudPath) throws BenchmarkException {
		// Run the CRUD operations.
		CrudPipelineFactory crudPipelineFactory = new CrudPipelineFactory(
				numConnections, allCrudOperations, crudPath, NullResponseHandler.INSTANCE);
		run(crudPipelineFactory);
		
		// Return the times for each connection.
		List<CrudConnectionTimes> allConnectionTimes = new ArrayList<CrudConnectionTimes>(numConnections);
		for (CrudConnectionTimers connectionTimers : crudPipelineFactory.getAllConnectionTimers()) {
			allConnectionTimes.add(connectionTimers.getConnectionTimes());
		}
		return allConnectionTimes;
	}
}
