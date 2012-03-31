package co.adhoclabs.ironcushion;

/**
 * Superclass for class recording statistics about a connection.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public abstract class AbstractConnectionStatistics {
	private long jsonBytesSent;
	private long jsonBytesReceived;
	
	protected final Timer localProcessingTimer;
	protected final Timer sendDataTimer;

	protected AbstractConnectionStatistics() {
		jsonBytesSent = 0;
		jsonBytesReceived = 0;
		
		localProcessingTimer = new Timer();
		sendDataTimer = new Timer();
	}

	/**
	 * Adds the given number to the total number of bytes of JSON sent.
	 * 
	 * @param numBytes the number of bytes to add
	 */
	public void sentJsonBytes(long numBytes) {
		jsonBytesSent += numBytes;
	}
	
	/**
	 * @return the total number of bytes of JSON sent
	 */
	public long getJsonBytesSent() {
		return jsonBytesSent;
	}
	
	/**
	 * Adds the given number to the total number of bytes of JSON received.
	 * 
	 * @param numBytes the number of bytes to add
	 */
	public void receivedJsonBytes(long numBytes) {
		jsonBytesReceived += numBytes;
	}
	
	/**
	 * @return the total number of bytes of JSON received
	 */
	public long getJsonBytesReceived() {
		return jsonBytesReceived;
	}
	
	/**
	 * Stops whichever timer is running.
	 */
	public abstract void stop();
	
	/**
	 * Starts the timer for local processing.
	 */
	public abstract void startLocalProcessing();
	
	/**
	 * @return the number of milliseconds spent on local processing
	 */
	public long getLocalProcessingTimeMillis() {
		return localProcessingTimer.getTotalTimeMillis();
	}
	
	/**
	 * Starts the timer for sending data.
	 */
	public abstract void startSendData();
	
	/**
	 * @return the number of milliseconds spent on sending data
	 */
	public long getSendDataTimeMillis() {
		return sendDataTimer.getTotalTimeMillis();
	}
	
	/**
	 * @return the total number of milliseconds spent
	 */
	public abstract long getTotalTimeMillis();
}
