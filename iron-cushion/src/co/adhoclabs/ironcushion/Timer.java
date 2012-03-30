package co.adhoclabs.ironcushion;

/**
 * A timer for operations.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class Timer {
	private long totalTimeMillis;
	private long startTime;
	
	public Timer() {
		totalTimeMillis = 0;
		startTime = 0;
	}

	public void start() {
		startTime = System.nanoTime();
	}
	
	public void stop() {
		long elapsedTime = System.nanoTime() - startTime;
		// Convert to milliseconds.
		totalTimeMillis += (elapsedTime / 1000000);
	}
	
	public long getTotalTimeMillis() {
		return totalTimeMillis;
	}
}
