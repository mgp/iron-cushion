package co.adhoclabs;
/**
 * The results of the bemchmark.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class BenchmarkResults {
	public final double timeTaken;
	public final long bytesSent;
	public final long bytesReceived;
	public final double requestsPerSecond;
	public final double transferRate;
	
	public BenchmarkResults(double timeTaken,
			long bytesSent,
			long bytesReceived,
			double requestsPerSecond,
			double transferRate) {
		this.timeTaken = timeTaken;
		this.bytesSent = bytesSent;
		this.bytesReceived = bytesReceived;
		this.requestsPerSecond = requestsPerSecond;
		this.transferRate = transferRate;
	}
}
