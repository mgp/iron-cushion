package co.adhoclabs;
/**
 * Benchmark utility for CouchDB.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class CouchDbBenchmark {
	public static void main(String[] args) {
		ParsedArguments parsedArguments = ParsedArguments.parseArguments(args);
		ScheduledOperations scheduledOperations = ScheduledOperations.scheduleOperations(
				parsedArguments);
		
		HttpReactor httpReactor = new HttpReactor(parsedArguments.numConnections);
		httpReactor.start();
		httpReactor.performBulkInserts();
		httpReactor.performCrudOperations(scheduledOperations);
		BenchmarkResults results = httpReactor.end();
		
		System.out.println("Results:");
		System.out.println("  Time taken: " + results.timeTaken);
		System.out.println("  Bytes sent: " + results.bytesSent);
		System.out.println("  Bytes received: " + results.bytesReceived);
		System.out.println("  Requests per second: " + results.requestsPerSecond);
		System.out.println("  Transfer rate: " + results.transferRate);
	}
}
