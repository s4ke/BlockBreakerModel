package de.hotware.blockbreaker.model.gamehandler;

import de.hotware.blockbreaker.model.listeners.IGameEndListener;

/**
 * The BaseGameTypeHandler class created for more easy implementation
 * of new game modes. All important Events should be handled here or 
 * at least have a requestMethod which returns a boolean.
 */
public abstract class BaseGameTypeHandler implements IGameEndListener {
	
	protected GameHandlerInfo mGameHandlerInfo;
	
	public BaseGameTypeHandler() {
		this.mGameHandlerInfo = GameHandlerInfo.INSTANCE;
	}

	/**
	 * called if Activity loses Focus
	 */
	public void onLeaveFocus() {
		this.mGameHandlerInfo.mLevelSceneHandler.setIgnoreInput(true);
	}

	public void requestSeedInput() { }

	/**
	 * called if Activity gains Focus
	 */
	public void onEnterFocus() {
		this.mGameHandlerInfo.mLevelSceneHandler.setIgnoreInput(false);
	}

	/**
	 * called if the user requests the next Level, which is the same as losing in TimeAttack
	 */
	public void requestNextLevel() {}

	/**
	 * called if the user requests to leave to the menu Activity
	 * @return true if menu will be shown, false otherwise
	 * @return default version returns true
	 */
	public boolean requestLeaveToMenu() { return true; }

	/**
	 * called if the user requests a restart of the game
	 */
	public void requestRestart() {}

	/**
	 * called upon first start of the game
	 */
	public void init() {}

	/**
	 * called when before the GameHandler is changed
	 */
	public void cleanUp() {}

	/**
	 * called if the number of turns property has changed, only used for notifying, no information
	 */
	public void onNumberOfTurnsPropertyChanged() {}
	
}