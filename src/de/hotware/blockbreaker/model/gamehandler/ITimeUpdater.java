package de.hotware.blockbreaker.model.gamehandler;

public interface ITimeUpdater {
	
	/**
	 * sets the Callback that is being used for all time-events
	 * @param pCallback
	 */
	public void setTimePassedCallback(ITimePassedCallback pCallback);
	
	/**
	 * sets the time after which the updater has to stop
	 * @param pTime
	 */
	public void setTime(float pTime);
	
	/**
	 * sets the period Time after which the onTimePassed(pSeconds) method
	 * is being called
	 * @param pTime
	 */
	public void setUpdateTime(float pTime);
	
	/**
	 * resets the timer to zero time passed behaviour
	 */
	public void reset();
	
	/**
	 * starts the TimeUpdater
	 * @throws IllegalStateException when setTime(float) or setUpdateTime(float) haven't
	 * been called, yet or no ITimePassedCallback has been specified, yet (== null)
	 */
	public void start();
	
	/**
	 * pauses the TimeUpdater. Just like doing pause during a song ;)
	 */
	public void pause();
	
	/**
	 * Does the same as if pause() and reset() have been called in sequence
	 */
	public void stop();
	
	public static interface ITimePassedCallback {
		
		public void onTimePassed(int pSeconds);
		
		public void onTimeEnd();
		
	}

}
