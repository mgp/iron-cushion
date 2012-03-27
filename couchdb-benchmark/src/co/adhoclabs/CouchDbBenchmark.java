package co.adhoclabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Benchmark utility for CouchDB.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class CouchDbBenchmark {
	public static void main(String[] args) throws BenchmarkException {
		ParsedArguments parsedArguments = ParsedArguments.parseArguments(args);

		// Create the document schema.
		DocumentSchema schema = DocumentSchema.createSchema(
				parsedArguments.documentSchemaFile);
		// Create the documents to bulk insert from the schema.
		ValueGenerator valueGenerator = new ValueGenerator(new Random(2012));
		List<BulkInsertDocuments> allBulkInsertDocuments = new ArrayList<BulkInsertDocuments>(
				parsedArguments.numConnections);
		for (int i = 0; i < parsedArguments.numConnections; ++i) {
			BulkInsertDocuments bulkInsertDocuments = new BulkInsertDocuments(
					schema, valueGenerator, i,
					parsedArguments.numDocumentsPerBulkInsert,
					parsedArguments.numBulkInsertOperations);
		}
		// Create the bulk insert URL.
		StringBuilder sb = new StringBuilder(parsedArguments.databaseUrl);
		if (!parsedArguments.databaseUrl.endsWith("/")) {
			sb.append('/');
		}
		sb.append("_bulk_docs");
		String bulkInsertUri = sb.toString();
		
		// Perform the bulk insert operations.
		HttpReactor httpReactor = new HttpReactor(
				parsedArguments.numConnections, allBulkInsertDocuments, bulkInsertUri);

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
