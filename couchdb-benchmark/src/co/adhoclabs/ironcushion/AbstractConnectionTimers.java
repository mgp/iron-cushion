package co.adhoclabs.ironcushion;

/**
 * Superclass for connection timers that record how a connection spends its time.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public abstract class AbstractConnectionTimers {
	/**
	 * Superclass for breakdowns of how a connection spent its time.
	 */
	public static abstract class AbstractConnectionTimes {
		/**
		 * Milliseconds devoted to local processing.
		 */
		public final long localProcessingMillis;
		/**
		 * Milliseconds devoted to sending data.
		 */
		public final long sendDataMillis;
		/**
		 * Milliseconds devoted to receiving data.
		 */
		public final long receiveDataMillis;

		protected AbstractConnectionTimes(long localProcessingMillis,
				long sendDataMillis,
				long receiveDataMillis) {
			this.localProcessingMillis = localProcessingMillis;
			this.sendDataMillis = sendDataMillis;
			this.receiveDataMillis = receiveDataMillis;
		}
	}
	
	protected final Timer localProcessingTimer;
	protected final Timer sendDataTimer;
	protected final Timer receiveDataTimer;

	protected AbstractConnectionTimers() {
		localProcessingTimer = new Timer();
		sendDataTimer = new Timer();
		receiveDataTimer = new Timer();
	}

	/**
	 * Starts the timer for local processing.
	 */
	public abstract void startLocalProcessing();
	
	/**
	 * Starts the timer for sending data.
	 */
	public abstract void startSendData();
	
	/**
	 * Starts the timer for remote processing.
	 */
	public abstract void startReceiveData();
	
	/**
	 * Reset all timers.
	 */
	public void reset() {
		localProcessingTimer.reset();
		sendDataTimer.reset();
		receiveDataTimer.reset();
	}
}
