package de.hotware.blockbreaker.model.gamehandler;

import de.hotware.blockbreaker.model.Level;

public interface ILevelSceneHandler {
	
	public void setIgnoreInput(boolean pIgnoreInput);

	public void updateLevel(Level pLevel, long pSeed);
	
	/**
	 * @throws IllegalStateException if Level has already been initialized
	 * @param pLevel
	 */
	public void initLevelScene(Level pLevel);
	
	/**
	 * @return whether the LevelSceneHandler has already been initialized
	 */
	public boolean isStarted();
	
	public Level getLevel();

}
