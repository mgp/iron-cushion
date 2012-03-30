package co.adhoclabs.ironcushion.bulkinsert;

import co.adhoclabs.ironcushion.AbstractConnectionStatistics;
import co.adhoclabs.ironcushion.Timer;

/**
 * Statistics for a connection performing bulk inserts.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class BulkInsertConnectionStatistics extends AbstractConnectionStatistics {
	private final Timer remoteProcessingTimer;

	public BulkInsertConnectionStatistics() {
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
	 * Stops whichever timer is running.
	 */
	@Override
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
	
	/**
	 * @return the number of milliseconds spent on remote processing
	 */
	public long getRemoteProcessingTimeMillis() {
		return remoteProcessingTimer.getTotalTimeMillis();
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
}
