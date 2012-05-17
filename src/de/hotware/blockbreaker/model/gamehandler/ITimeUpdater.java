package de.hotware.blockbreaker.model.gamehandler;

public interface ITimeUpdater {
	
	public void setTimePassedCallback(ITimePassedCallback pCallback);
	public void setTime(float pTime);
	public void setUpdateTime(float pTime);
	public void reset();
	public void start();
	
	public static interface ITimePassedCallback {
		
		public void onTimePassed(int pSeconds);
		public void onTimeEnd();
		
	}

}
