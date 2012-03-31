package co.adhoclabs.ironcushion;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import co.adhoclabs.ironcushion.bulkinsert.BulkInsertConnectionStatistics;
import co.adhoclabs.ironcushion.bulkinsert.BulkInsertDocumentGenerator;
import co.adhoclabs.ironcushion.bulkinsert.BulkInsertPipelineFactory;
import co.adhoclabs.ironcushion.crud.CrudConnectionStatistics;
import co.adhoclabs.ironcushion.crud.CrudOperations;
import co.adhoclabs.ironcushion.crud.CrudPipelineFactory;

/**
 * The networking engine that asynchronously executes HTTP requests.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class HttpReactor {
	private final int numConnections;
	private final InetSocketAddress databaseAddress;
	
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
	
	public List<BulkInsertConnectionStatistics> performBulkInserts(
			List<BulkInsertDocumentGenerator> allBulkInsertDocumentGenerators,
			String bulkInsertPath) throws BenchmarkException {
		// Run the bulk inserts.
		BulkInsertPipelineFactory bulkInsertPipelineFactory = new BulkInsertPipelineFactory(
				numConnections, allBulkInsertDocumentGenerators, bulkInsertPath);
		run(bulkInsertPipelineFactory);
		
		// Return the times for each connection.
		return bulkInsertPipelineFactory.getAllConnectionStatistics();
	}
	
	public List<CrudConnectionStatistics> performCrudOperations(List<CrudOperations> allCrudOperations,
			String crudPath) throws BenchmarkException {
		// Run the CRUD operations.
		CrudPipelineFactory crudPipelineFactory = new CrudPipelineFactory(
				numConnections, allCrudOperations, crudPath);
		run(crudPipelineFactory);
		
		// Return the times for each connection.
		return crudPipelineFactory.getAllConnectionStatistics();
	}
}
