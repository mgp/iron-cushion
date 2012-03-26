package co.adhoclabs;

/**
 * A timer for operations.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class Timer {
	private long totalTime;
	private long startTime;
	
	public Timer() {
		totalTime = 0;
		startTime = 0;
	}
	
	public void reset() {
		totalTime = 0;
		startTime = 0;
	}
	
	public void start() {
		startTime = System.nanoTime();
	}
	
	public void stop() {
		long elapsedTime = System.nanoTime() - startTime;
		// Convert to milliseconds.
		totalTime += (elapsedTime / 1000000);
	}
	
	public long getTotalTimeMillis() {
		return totalTime;
	}
}
