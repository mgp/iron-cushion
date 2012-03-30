package co.adhoclabs.ironcushion;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.adhoclabs.ironcushion.bulkinsert.BulkInsertConnectionStatistics;
import co.adhoclabs.ironcushion.bulkinsert.BulkInsertDocumentGenerator;
import co.adhoclabs.ironcushion.crud.CrudConnectionStatistics;
import co.adhoclabs.ironcushion.crud.CrudOperations;
import co.adhoclabs.ironcushion.crud.CrudOperations.CrudOperationCounts;

/**
 * Benchmark utility for CouchDB.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class CouchDbBenchmark {
	private static void performCrudOperations(ParsedArguments parsedArguments,
			DocumentSchema schema, HttpReactor httpReactor, Random rng) throws BenchmarkException {
		// Create the CRUD operation path.
		StringBuilder sb = new StringBuilder();
		sb.append('/').append(parsedArguments.databaseName);
		String crudPath = sb.toString();
		
		// Create the CRUD operations to perform.
		List<CrudOperations> allCrudOperations = new ArrayList<CrudOperations>(
				parsedArguments.numConnections);
		CrudOperationCounts crudOperationCounts = CrudOperations.createOperationCounts(
				parsedArguments);
		for (int i = 0; i < parsedArguments.numConnections; ++i) {
			CrudOperations crudOperations = CrudOperations.createCrudOperations(
					i, schema, new ValueGenerator(rng), parsedArguments, crudOperationCounts);
			allCrudOperations.add(crudOperations);
		}

		// Perform the CRUD operations.
		List<CrudConnectionStatistics> allCrudConnectionTimes = httpReactor.performCrudOperations(
				allCrudOperations, crudPath);
	}
	
	private static void performBulkInserts(ParsedArguments parsedArguments,
			DocumentSchema schema, HttpReactor httpReactor, Random rng) throws BenchmarkException {
		
		// Create the bulk insert path.
		StringBuilder sb = new StringBuilder();
		sb.append('/').append(parsedArguments.databaseName);
		sb.append('/').append("_bulk_docs");
		String bulkInsertPath = sb.toString();

		List<ValueGenerator> valueGenerators = new ArrayList<ValueGenerator>(
				parsedArguments.numConnections);
		for (int i = 0; i < parsedArguments.numConnections; ++i) {
			valueGenerators.add(new ValueGenerator(rng));
		}
		List<BulkInsertDocumentGenerator> allBulkInsertDocumentGenerators = new ArrayList<BulkInsertDocumentGenerator>(
				parsedArguments.numConnections);
		for (int i = 0; i < parsedArguments.numConnections; ++i) {
			ValueGenerator valueGenerator = valueGenerators.get(i);
			BulkInsertDocumentGenerator bulkInsertDocumentGenerator = BulkInsertDocumentGenerator.preComputed(
					schema, valueGenerator, i,
					parsedArguments.numDocumentsPerBulkInsert,
					parsedArguments.numBulkInsertOperations);
			allBulkInsertDocumentGenerators.add(bulkInsertDocumentGenerator);
		}
		
		// Perform the bulk insert operations.
		List<BulkInsertConnectionStatistics> allBulkInsertConnectionTimes = httpReactor.performBulkInserts(
				allBulkInsertDocumentGenerators, bulkInsertPath);
		
		// Print the results.
		BulkInsertConnectionStatistics firstConnectionTimes = allBulkInsertConnectionTimes.get(0);
		System.out.println("jsonBytesSent=" + firstConnectionTimes.getJsonBytesSent());
		System.out.println("jsonBytesReceived=" + firstConnectionTimes.getJsonBytesReceived());
		System.out.println("localProcessingMillis=" + firstConnectionTimes.getLocalProcessingTimeMillis());
		System.out.println("sendDataMillis=" + firstConnectionTimes.getSendDataTimeMillis());
		System.out.println("remoteProcessingMillis=" + firstConnectionTimes.getRemoteProcessingTimeMillis());
		System.out.println("receiveDataMillis=" + firstConnectionTimes.getReceivedDataTimeMillis());		
	}
	
	public static void main(String[] args) throws BenchmarkException {
		ParsedArguments parsedArguments = ParsedArguments.parseArguments(args);
		Random rng = new Random(2012);

		// Create the document schema.
		DocumentSchema schema = null;
		if (parsedArguments.jsonDocumentSchemaFile != null) {
			schema = DocumentSchema.createSchemaFromJson(parsedArguments.jsonDocumentSchemaFile);
		} else if (parsedArguments.xmlDocumentSchemaFile != null) {
			schema = DocumentSchema.createSchemaFromXml(parsedArguments.xmlDocumentSchemaFile);
		}

		// Create the address of the server.
		URI databaseUri;
		try {
			databaseUri = new URI(parsedArguments.databaseAddress);
		} catch (URISyntaxException e) {
			throw new BenchmarkException(e);
		}
		InetSocketAddress databaseAddress = new InetSocketAddress(
				databaseUri.getHost(), databaseUri.getPort());
		HttpReactor httpReactor = new HttpReactor(parsedArguments.numConnections, databaseAddress);
		
		// Perform the bulk inserts.
		performBulkInserts(parsedArguments, schema, httpReactor, rng);
		// Perform the CRUD operations.
		performCrudOperations(parsedArguments, schema, httpReactor, rng);
	}
}
