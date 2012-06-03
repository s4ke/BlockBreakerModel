package de.hotware.blockbreaker.model.gamehandler;

import de.hotware.blockbreaker.model.gamehandler.DefaultGameTypeHandler.IDefaultViewControl.IDefaultGameEndCallback;
import de.hotware.blockbreaker.model.gamehandler.IBlockBreakerMessageView.IInputSeedCallback;
import de.hotware.blockbreaker.model.listeners.IGameEndListener.GameEndEvent.GameEndType;

/**
 * The DefaultGameHandler
 * @author Martin Braun
 */
public class DefaultGameTypeHandler extends BaseGameTypeHandler {
	
	protected DefaultCallback mCallback;
	protected IDefaultViewControl mDefaultViewControl;
	
	public DefaultGameTypeHandler(IDefaultViewControl pDefaultViewControl) {
		super();
		this.mCallback = new DefaultCallback();
		this.mDefaultViewControl = pDefaultViewControl;
	}

	@Override
	public void onGameEnd(final GameEndEvent pEvt) {
		this.mDefaultViewControl.showEndDialog(pEvt.getType(), this.mCallback);
	}

	@Override
	public void requestRestart() {
		this.restartLevel();
	}

	@Override
	public void requestNextLevel() {
		this.randomLevel();
	}

	@Override
	public void requestSeedInput() {
		this.mBlockBreakerMessageView.showInputSeed(this.mCallback);
	}
	
	@Override
	public void init() {
		
	}

	@Override
	public void cleanUp() {
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////					Inner Classes/Interfaces			///////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
		
	public static interface IDefaultViewControl {
		
		public void showEndDialog(GameEndType pGameEnding, IDefaultGameEndCallback pCallback);
		
		public static interface IDefaultGameEndCallback {
			
			/**
			 * @param pRestart whether the current level has to be restarted
			 */
			public void onEndDialogFinished(boolean pRestart);
			
		}
		
	}	
	
	/**
	 * the base callback implementation that is needed for all of DefaultGameTypeHandler's purposes
	 */
	private class DefaultCallback implements IInputSeedCallback, IDefaultGameEndCallback {

		@Override
		public void onSeedChosen(long pSeed) {
			DefaultGameTypeHandler.this.randomLevelFromSeed(pSeed);
		}
		
		
		@Override
		public void onEndDialogFinished(boolean pRestart) {
			if(pRestart) {
				DefaultGameTypeHandler.this.restartLevel();
			} else {
				DefaultGameTypeHandler.this.randomLevel();
			}
		}
		
	}


}