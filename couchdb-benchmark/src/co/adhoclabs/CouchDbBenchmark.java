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

		DocumentSchema schema = DocumentSchema.createSchema(
				parsedArguments.documentSchemaFile);
		ValueGenerator valueGenerator = new ValueGenerator(new Random(2012));
		List<BulkInsertDocuments> allBulkInsertDocuments = new ArrayList<BulkInsertDocuments>(
				parsedArguments.numConnections);
		for (int i = 0; i < parsedArguments.numConnections; ++i) {
			BulkInsertDocuments bulkInsertDocuments = new BulkInsertDocuments(
					schema, valueGenerator, i,
					parsedArguments.numDocumentsPerBulkInsert,
					parsedArguments.numBulkInsertOperations);
		}

		HttpReactor httpReactor = new HttpReactor(
				parsedArguments.numConnections, allBulkInsertDocuments, "");
		httpReactor.performBulkInserts();

		/*
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
