package de.hotware.blockbreaker.model.gamehandler;

import java.util.Random;

import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.model.generator.LevelGenerator;
import de.hotware.blockbreaker.model.listeners.IGameEndListener;

/**
 * The BaseGameTypeHandler class created for more easy implementation
 * of new game modes. All important Events should be handled here or 
 * at least have a requestMethod which returns a boolean.
 */
public abstract class BaseGameTypeHandler implements IGameEndListener {
	
	protected static final int DEFAULT_NUMBER_OF_TURNS = 16;
	protected static final int DEFAULT_WIN_COUNT = 10;
	protected static final int EASY_WIN_COUNT = 10;
	protected static final int MEDIUM_WIN_COUNT = 13;
	protected static final int HARD_WIN_COUNT = 16;
	
	protected static final Random sRandomSeedObject = new Random();
	
	protected IBlockBreakerMessageView mBlockBreakerMessageView;
	protected String mPlayerName;
	protected ILevelSceneHandler mLevelSceneHandler;
	protected Level mLevel;
	protected long mLevelSeed;
	protected Level mBackupLevel;
	protected int mNumberOfTurns = DEFAULT_NUMBER_OF_TURNS;
	protected Difficulty mDifficulty = Difficulty.EASY;
	
	public BaseGameTypeHandler() {
		
	}

	/**
	 * called if Activity loses Focus
	 */
	public void onLeaveFocus() {
		this.mLevelSceneHandler.setIgnoreInput(true);
	}

	public void requestSeedInput() { }

	/**
	 * called if Activity gains Focus
	 */
	public void onEnterFocus() {
		this.mLevelSceneHandler.setIgnoreInput(false);
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
	public abstract void init();

	/**
	 * called when before the GameHandler is changed
	 */
	protected abstract void cleanUp();

	/**
	 * called if the number of turns property has changed, only used for notifying, no information
	 */
	public void onNumberOfTurnsPropertyChanged() {}
	
	/**
	 * <b>DO-CALL</b>
	 */
	public void setBlockBreakerMessageView(IBlockBreakerMessageView pView) {
		this.mBlockBreakerMessageView = pView;
	}
	
	/**
	 * <b>DO-CALL</b>
	 * @param pLevelSceneHandler
	 * @param pGameEndListener
	 */
	public void setLevelSceneHandlerAndInitialize(ILevelSceneHandler pLevelSceneHandler,
			IGameEndListener pGameEndListener) {
		this.mLevelSceneHandler = pLevelSceneHandler;
		long seed = sRandomSeedObject.nextLong();
		this.mBackupLevel = LevelGenerator.createRandomLevelFromSeed(seed, 
				this.mNumberOfTurns, 
				this.mDifficulty.getWinCount());
		this.mLevel = this.mBackupLevel.copy();
		this.mLevel.start();
		this.mLevel.setGameEndListener(pGameEndListener);
		
		//ignore input, gamehandlers will have to handle starting on their own
		this.mLevelSceneHandler.setIgnoreInput(true);		
		this.mLevelSceneHandler.initLevelScene(this.mLevel);
	}
	
	/**
	 * sets the Difficulty of future
	 * @param pDifficulty
	 */
	public void setDifficulty(Difficulty pDifficulty) {
		this.mDifficulty = pDifficulty;
	}
	
	/**
	* restarts the Level by creating a deep copy of the backup level
	* and making it the current level. it also sets the GameEndListener
	* correctly and updates the LevelSceneHandlers Level
	* @param pGameEndlistener
	*/
	protected void restartLevel() {
		this.mLevel = this.mBackupLevel.copy();
		this.mLevel.start();
		this.mLevel.setGameEndListener(this);
		this.mLevelSceneHandler.updateLevel(this.mLevel, this.mLevelSeed);
	}

	/**
	* changes the current Level to a completely
	* randomly generated Level
	* @param pGameEndlistener
	*/
	protected void randomLevel() {
		long seed = sRandomSeedObject.nextLong();
		this.randomLevelFromSeed(seed);
	}

	/**
	* changes the current Level to a Level from seed
	* @param pGameEndListener
	* @param pSeed
	*/
	protected void randomLevelFromSeed(long pSeed) {
		this.mBackupLevel = LevelGenerator.createRandomLevelFromSeed(pSeed, 
				this.mNumberOfTurns, 
				this.mDifficulty.getWinCount());
				this.mLevel = this.mBackupLevel.copy();
				this.mLevel.start();
				this.mLevel.setGameEndListener(this);
		this.mLevelSceneHandler.updateLevel(this.mLevel, pSeed);
	}
	
	/**
	 * enum for Difficulty Settings to make preferences stuff more easy
	 * @author Martin Braun
	 */
	public static enum Difficulty {
		EASY(BaseGameTypeHandler.EASY_WIN_COUNT),
		MEDIUM(BaseGameTypeHandler.MEDIUM_WIN_COUNT),
		HARD(BaseGameTypeHandler.HARD_WIN_COUNT),
		DEFAULT(BaseGameTypeHandler.DEFAULT_WIN_COUNT);
		
		private int mWinCount;
		
		private Difficulty(int pWinCount) {
			this.mWinCount = pWinCount;
		}
		
		public int getWinCount() {
			return this.mWinCount;
		}
		
		public static Difficulty numberToDifficulty(int pNumber) {
			switch(pNumber) {
				case 0: {
					return EASY;
				}
				case 1: {
					return MEDIUM;
				}
				case 2: {
					return HARD;
				}
				default: {
					return DEFAULT;
				}
			}
		}
		
	}
	
}