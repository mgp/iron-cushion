package co.adhoclabs.ironcushion;

import java.util.Arrays;

/**
 * The results of the benchmark.
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
	
	/* TODO
	public static final class ConnectionBenchmarkResults {
		public final ConnectionTimes times;
		public final long bytesSent;
		public final long bytesReceived;
		
		public ConnectionBenchmarkResults(ConnectionTimes times,
				long bytesSent, long bytesReceived) {
			this.times = times;
			this.bytesSent = bytesSent;
			this.bytesReceived = bytesReceived;
		}
	}
	*/
	
	/**
	 * Essential statistics about a data set.
	 */
	public static final class SampleStatistics {
		public final double min;
		public final double max;
		public final double mean;
		public final double median;
		public final double deviation;
		
		public SampleStatistics(double min, double max,
				double mean, double median, double deviation) {
			this.min = min;
			this.max = max;
			this.mean = mean;
			this.median = median;
			this.deviation = deviation;
		}
		
		private static SampleStatistics statisticsForPopulation(long[] values) {
			// Make a copy of the array before sorting as a courtesy.
			values = Arrays.copyOf(values, values.length);
			Arrays.sort(values);
			
			// Find the minimum and maximum.
			long min = values[0];
			long max = values[values.length - 1];
			// Find the median.
			double median;
			if ((values.length % 2) == 1) {
				median = values[(values.length - 1) / 2];
			} else {
				int firstMedianIndex = values.length / 2;
				int secondMedianIndex = firstMedianIndex - 1;
				median = ((double) (values[firstMedianIndex] + values[secondMedianIndex])) / 2; 
			}
			// Compute the mean.
			double sum = 0;
			for (int i = 0; i < values.length; ++i) {
				sum += values[i];
			}
			double mean = sum / values.length;
			// Compute the standard deviation.
			double numerator = 0;
			for (int i = 0; i < values.length; ++i) {
				double difference = values[i] - mean;
				numerator += (difference * difference);
			}
			double variance = numerator / values.length;
			double deviation = Math.sqrt(variance);
			
			return new SampleStatistics(min, max, mean, median, deviation);
		}
	}
}
