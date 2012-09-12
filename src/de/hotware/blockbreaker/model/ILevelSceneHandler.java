package de.hotware.blockbreaker.model;


public interface ILevelSceneHandler {
	
	public void setIgnoreInput(boolean pIgnoreInput);
	
	public void setGravityChanger(IGravityChanger pGravityChanger);

	public void updateLevel(Level pLevel, long pSeed);
	
	/**
	 * @throws IllegalStateException if Level has already been initialized
	 * @param pLevel
	 */
	public void initLevelScene(Level pLevel, long pSeed);
	
	/**
	 * @return whether the LevelSceneHandler has already been initialized
	 */
	public boolean isStarted();

}
