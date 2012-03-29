package co.adhoclabs.ironcushion;

/**
 * The statistics of a connection.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class ConnectionStatistics {
	private long jsonBytesSent;
	private long jsonBytesReceived;
	
	// TODO: add timers or times somehow
	
	public ConnectionStatistics() {
		jsonBytesSent = 0;
		jsonBytesReceived = 0;
	}
	
	/**
	 * @return the number of bytes of JSON sent.
	 */
	public long getJsonBytesSent() {
		return jsonBytesSent;
	}
	
	/**
	 * Adds to the number of bytes of JSON sent.
	 */
	public void sentJsonBytes(int numBytes) {
		jsonBytesSent += numBytes;
	}
	
	/**
	 * @return the number of bytes of JSON received
	 */
	public long getJsonBytesReceived() {
		return jsonBytesReceived;
	}
	
	/**
	 * Adds to the number of bytes of JSON received.
	 */
	public void receivedJsonBytes(int numBytes) {
		jsonBytesReceived += numBytes;
	}
}
