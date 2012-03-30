package co.adhoclabs.ironcushion.crud;

import co.adhoclabs.ironcushion.AbstractConnectionStatistics;
import co.adhoclabs.ironcushion.Timer;

/**
 * Statistics for a connection performing CRUD operations.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class CrudConnectionStatistics extends AbstractConnectionStatistics {
	private final Timer remoteCreateProcessingTimer;
	private final Timer remoteReadProcessingTimer;
	private final Timer remoteUpdateProcessingTimer;
	private final Timer remoteDeleteProcessingTimer;
	
	public CrudConnectionStatistics() {
		super();
		remoteCreateProcessingTimer = new Timer();
		remoteReadProcessingTimer = new Timer();
		remoteUpdateProcessingTimer = new Timer();
		remoteDeleteProcessingTimer = new Timer();
	}
	
	/**
	 * An enumeration over all timers.
	 */
	public enum RunningConnectionTimer {
		LOCAL_PROCESSING,
		SEND_DATA,
		REMOTE_CREATE_PROCESSING,
		REMOTE_READ_PROCESSING,
		REMOTE_UPDATE_PROCESSING,
		REMOTE_DELETE_PROCESSING,
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
			case REMOTE_CREATE_PROCESSING:
				remoteCreateProcessingTimer.stop();
				return;
			case REMOTE_READ_PROCESSING:
				remoteReadProcessingTimer.stop();
				break;
			case REMOTE_UPDATE_PROCESSING:
				remoteUpdateProcessingTimer.stop();
				break;
			case REMOTE_DELETE_PROCESSING:
				remoteDeleteProcessingTimer.stop();
				break;
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
	 * Starts the timer for remote processing of create operations.
	 */
	public void startRemoteCreateProcessing() {
		if (runningTimer == RunningConnectionTimer.REMOTE_CREATE_PROCESSING) {
			return;
		}
		stop();
		remoteCreateProcessingTimer.start();
		runningTimer = RunningConnectionTimer.REMOTE_CREATE_PROCESSING;
	}
	
	/**
	 * Starts the timer for remote processing of read operations.
	 */
	public void startRemoteReadProcessing() {
		if (runningTimer == RunningConnectionTimer.REMOTE_READ_PROCESSING) {
			return;
		}
		stop();
		remoteReadProcessingTimer.start();
		runningTimer = RunningConnectionTimer.REMOTE_READ_PROCESSING;
	}
	
	/**
	 * Starts the timer for remote processing of update operations.
	 */
	public void startRemoteUpdateProcessing() {
		if (runningTimer == RunningConnectionTimer.REMOTE_UPDATE_PROCESSING) {
			return;
		}
		stop();
		remoteUpdateProcessingTimer.start();
		runningTimer = RunningConnectionTimer.REMOTE_UPDATE_PROCESSING;
	}
	
	/**
	 * Starts the timer for remote processing of delete operations.
	 */
	public void startRemoteDeleteProcessing() {
		if (runningTimer == RunningConnectionTimer.REMOTE_DELETE_PROCESSING) {
			return;
		}
		stop();
		remoteDeleteProcessingTimer.start();
		runningTimer = RunningConnectionTimer.REMOTE_DELETE_PROCESSING;
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
