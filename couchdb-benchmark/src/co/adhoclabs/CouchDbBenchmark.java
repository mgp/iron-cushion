package co.adhoclabs;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.adhoclabs.bulkinsert.BulkInsertConnectionTimers.BulkInsertConnectionTimes;
import co.adhoclabs.bulkinsert.BulkInsertDocuments;

/**
 * Benchmark utility for CouchDB.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class CouchDbBenchmark {
	public static void main(String[] args) throws BenchmarkException {
		ParsedArguments parsedArguments = ParsedArguments.parseArguments(args);

		// Create the document schema.
		DocumentSchema schema = null;
		if (parsedArguments.jsonDocumentSchemaFile != null) {
			schema = DocumentSchema.createSchemaFromJson(parsedArguments.jsonDocumentSchemaFile);
		} else if (parsedArguments.xmlDocumentSchemaFile != null) {
			schema = DocumentSchema.createSchemaFromXml(parsedArguments.xmlDocumentSchemaFile);
		}
		// Create the documents to bulk insert from the schema.
		ValueGenerator valueGenerator = new ValueGenerator(new Random(2012));
		List<BulkInsertDocuments> allBulkInsertDocuments = new ArrayList<BulkInsertDocuments>(
				parsedArguments.numConnections);
		for (int i = 0; i < parsedArguments.numConnections; ++i) {
			BulkInsertDocuments bulkInsertDocuments = new BulkInsertDocuments(
					schema, valueGenerator, i,
					parsedArguments.numDocumentsPerBulkInsert,
					parsedArguments.numBulkInsertOperations);
			allBulkInsertDocuments.add(bulkInsertDocuments);
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
		
		// Create the bulk insert path.
		StringBuilder sb = new StringBuilder();
		sb.append('/').append(parsedArguments.databaseName);
		sb.append('/').append("_bulk_docs");
		String bulkInsertPath = sb.toString();
		
		// Perform the bulk insert operations.
		HttpReactor httpReactor = new HttpReactor(parsedArguments.numConnections, databaseAddress);
		List<BulkInsertConnectionTimes> allConnectionTimes = httpReactor.performBulkInserts(
				allBulkInsertDocuments, bulkInsertPath);
		
		BulkInsertConnectionTimes firstConnectionTimes = allConnectionTimes.get(0);
		System.out.println("localProcessingMillis=" + firstConnectionTimes.localProcessingMillis);
		System.out.println("sendDataMillis=" + firstConnectionTimes.sendDataMillis);
		System.out.println("remoteProcessingMillis=" + firstConnectionTimes.remoteProcessingMillis);
		System.out.println("receiveDataMillis=" + firstConnectionTimes.receiveDataMillis);
		
		/*
		httpReactor.performBulkInserts();
		ScheduledOperations scheduledOperations = ScheduledOperations
				.scheduleOperations(parsedArguments);

		System.out.println("Results:");
		System.out.println("  Time taken: " + results.timeTaken);
		System.out.println("  Bytes sent: " + results.bytesSent);
		System.out.println("  Bytes received: " + results.bytesReceived);
		System.out.println("  Requests per second: "
				+ results.requestsPerSecond);
		System.out.println("  Transfer rate: " + results.transferRate);
		*/
	}
}
