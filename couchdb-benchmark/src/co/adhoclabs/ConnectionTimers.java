package co.adhoclabs;

/**
 * Timers that breakdown how a connection spends its time.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class ConnectionTimers {
	private final Timer localProcessingTimer;
	private final Timer sendDataTimer;
	private final Timer remoteProcessingTimer;
	private final Timer receiveDataTimer;

	public ConnectionTimers() {
		localProcessingTimer = new Timer();
		sendDataTimer = new Timer();
		remoteProcessingTimer = new Timer();
		receiveDataTimer = new Timer();
	}
	
	/**
	 * An enumeration over all 
	 */
	public enum RunningConnectionTimer {
		LOCAL_PROCESSING,
		SEND_DATA,
		REMOTE_PROCESSING,
		RECEIVE_DATA,
	}
	
	private RunningConnectionTimer runningTimer;
	
	/**
	 * @return the currently running timer
	 */
	public RunningConnectionTimer getRunningConnectionTimer() {
		return runningTimer;
	}
	
	/**
	 * A breakdown of how a connection spent its time.
	 */
	public static final class ConnectionTimes {
		/**
		 * Milliseconds devoted to local processing.
		 */
		public final long localProcessingMillis;
		/**
		 * Milliseconds devoted to sending data.
		 */
		public final long sendDataMillis;
		/**
		 * Milliseconds devoted to remote processing.
		 */
		public final long remoteProcessingMillis;
		/**
		 * Milliseconds devoted to receiving data.
		 */
		public final long receiveDataMillis;
		
		public ConnectionTimes(long localProcessingMillis,
				long sendDataMillis,
				long remoteProcessingMillis,
				long receiveDataMillis) {
			this.localProcessingMillis = localProcessingMillis;
			this.sendDataMillis = sendDataMillis;
			this.remoteProcessingMillis = remoteProcessingMillis;
			this.receiveDataMillis = receiveDataMillis;
		}
	}
	
	/**
	 * Stops whichever timer is running.
	 */
	public void stop() {
		if (runningTimer != null) {
			switch (runningTimer) {
			case LOCAL_PROCESSING:
				localProcessingTimer.stop();
				return;
			case SEND_DATA:
				sendDataTimer.stop();
				return;
			case REMOTE_PROCESSING:
				remoteProcessingTimer.stop();
				return;
			case RECEIVE_DATA:
				receiveDataTimer.stop();
				return;
			default:
				break;
			}
		}
	}
	
	/**
	 * Starts the timer for local processing.
	 */
	public void startLocalProcessing() {
		stop();
		localProcessingTimer.start();
		runningTimer = RunningConnectionTimer.LOCAL_PROCESSING;
	}
	
	/**
	 * Starts the timer for sending data.
	 */
	public void startSendData() {
		stop();
		sendDataTimer.start();
		runningTimer = RunningConnectionTimer.SEND_DATA;
	}
	
	/**
	 * Starts the timer for remote processing.
	 */
	public void startRemoteProcessing() {
		stop();
		remoteProcessingTimer.start();
		runningTimer = RunningConnectionTimer.REMOTE_PROCESSING;
	}
	
	/**
	 * Starts the timer for receiving data.
	 */
	public void startReceiveData() {
		stop();
		receiveDataTimer.start();
		runningTimer = RunningConnectionTimer.RECEIVE_DATA;
	}
	
	/**
	 * @return a {@link ConnectionTimes} instance
	 */
	public ConnectionTimes getConnectionTimes() {
		return new ConnectionTimes(localProcessingTimer.getTotalTimeMillis(),
				sendDataTimer.getTotalTimeMillis(),
				remoteProcessingTimer.getTotalTimeMillis(),
				receiveDataTimer.getTotalTimeMillis());
	}
	
	/**
	 * Resets all timers.
	 */
	public void reset() {
		localProcessingTimer.reset();
		sendDataTimer.reset();
		remoteProcessingTimer.reset();
		receiveDataTimer.reset();
	}
}
