package co.adhoclabs;
/**
 * The networking engine that asynchronously executes HTTP requests.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class HttpReactor {
	private final int numConnections;
	
	public HttpReactor(int numConnections) {
		this.numConnections = numConnections;
		
		start();
	}
	
	public void start() {
		// TODO: start up the async engine
	}
	
	public void performBulkInserts() {
		// TODO
	}
	
	public void performCrudOperations(ScheduledOperations scheduledOperations) {
		// TODO
	}
	
	public BenchmarkResults end() {
		// TODO: stop the async engine
		return null;
	}
}
