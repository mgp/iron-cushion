package co.adhoclabs.bulkinsert;

import co.adhoclabs.AbstractConnectionTimers;
import co.adhoclabs.Timer;

/**
 * Timers that breakdown how a connection performing bulk inserts spends its time.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class BulkInsertConnectionTimers extends AbstractConnectionTimers {
	private final Timer remoteProcessingTimer;

	public BulkInsertConnectionTimers() {
		super();
		remoteProcessingTimer = new Timer();
	}
	
	/**
	 * An enumeration over all timers.
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
	 * A breakdown of how a connection performing bulk inserts spent its time.
	 */
	public static final class BulkInsertConnectionTimes extends AbstractConnectionTimes {
		/**
		 * Milliseconds devoted to remote processing.
		 */
		public final long remoteProcessingMillis;
		
		public BulkInsertConnectionTimes(long localProcessingMillis,
				long sendDataMillis,
				long remoteProcessingMillis,
				long receiveDataMillis) {
			super(localProcessingMillis,
					sendDataMillis,
					receiveDataMillis);
			this.remoteProcessingMillis = remoteProcessingMillis;
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
	
	@Override
	public void startLocalProcessing() {
		if (runningTimer == RunningConnectionTimer.LOCAL_PROCESSING) {
			return;
		}
		stop();
		localProcessingTimer.start();
		runningTimer = RunningConnectionTimer.LOCAL_PROCESSING;
	}
	
	@Override
	public void startSendData() {
		if (runningTimer == RunningConnectionTimer.SEND_DATA) {
			return;
		}
		stop();
		sendDataTimer.start();
		runningTimer = RunningConnectionTimer.SEND_DATA;
	}
	
	/**
	 * Starts the timer for remote processing.
	 */
	public void startRemoteProcessing() {
		if (runningTimer == RunningConnectionTimer.REMOTE_PROCESSING) {
			return;
		}
		stop();
		remoteProcessingTimer.start();
		runningTimer = RunningConnectionTimer.REMOTE_PROCESSING;
	}
	
	@Override
	public void startReceiveData() {
		if (runningTimer == RunningConnectionTimer.RECEIVE_DATA) {
			return;
		}
		stop();
		receiveDataTimer.start();
		runningTimer = RunningConnectionTimer.RECEIVE_DATA;
	}
	
	/**
	 * @return a {@link BulkInsertConnectionTimes} instance
	 */
	public BulkInsertConnectionTimes getConnectionTimes() {
		return new BulkInsertConnectionTimes(localProcessingTimer.getTotalTimeMillis(),
				sendDataTimer.getTotalTimeMillis(),
				remoteProcessingTimer.getTotalTimeMillis(),
				receiveDataTimer.getTotalTimeMillis());
	}
	
	@Override
	public void reset() {
		super.reset();
		remoteProcessingTimer.reset();
	}
}
