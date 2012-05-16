package de.hotware.blockbreaker.model.gamehandler;

import java.util.Random;

import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.model.generator.LevelGenerator;
import de.hotware.blockbreaker.model.listeners.IGameEndListener;

public enum GameHandlerInfo {
	INSTANCE;
	
	static final int DEFAULT_NUMBER_OF_TURNS = 16;
	static final int DEFAULT_WIN_COUNT = 10;
	static final int EASY_WIN_COUNT = 10;
	static final int MEDIUM_WIN_COUNT = 13;
	static final int HARD_WIN_COUNT = 16;
	
	static final Random sRandomSeedObject = new Random();
	
	IBlockBreakerMessageView mBlockBreakerMessageView;
	ILevelSceneHandler mLevelSceneHandler;
	private Level mLevel;
	private long mLevelSeed;
	private Level mBackupLevel;
	private int mNumberOfTurns = DEFAULT_NUMBER_OF_TURNS;
	private Difficulty mDifficulty = Difficulty.EASY;
	
	public void setBlockBreakerMessageView(IBlockBreakerMessageView pView) {
		this.mBlockBreakerMessageView = pView;
	}
	
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
	
	public void setNumberOfTurns(int pNumberOfTurns) {
		this.mNumberOfTurns = pNumberOfTurns;
	}
	
	public void setDifficulty(Difficulty pDifficulty) {
		this.mDifficulty = pDifficulty;
	}
	
	/**
	* restarts the Level by creating a deep copy of the backup level
	* and making it the current level. it also sets the GameEndListener
	* correctly and updates the LevelSceneHandlers Level
	* @param pGameEndlistener
	*/
	void restartLevel(IGameEndListener pGameEndListener) {
		this.mLevel = this.mBackupLevel.copy();
		this.mLevel.start();
		this.mLevel.setGameEndListener(pGameEndListener);
		this.mLevelSceneHandler.updateLevel(this.mLevel, this.mLevelSeed);
	}

	/**
	* changes the current Level to a completely
	* randomly generated Level
	* @param pGameEndlistener
	*/
	void randomLevel(IGameEndListener pGameEndListener) {
		long seed = GameHandlerInfo.sRandomSeedObject.nextLong();
		this.randomLevelFromSeed(pGameEndListener, seed);
	}

	/**
	* changes the current Level to a Level from seed
	* @param pGameEndListener
	* @param pSeed
	*/
	void randomLevelFromSeed(IGameEndListener pGameEndListener, long pSeed) {
		this.mBackupLevel = LevelGenerator.createRandomLevelFromSeed(pSeed, 
				this.mNumberOfTurns, 
				this.mDifficulty.getWinCount());
				this.mLevel = this.mBackupLevel.copy();
				this.mLevel.start();
				this.mLevel.setGameEndListener(pGameEndListener);
		this.mLevelSceneHandler.updateLevel(this.mLevel, pSeed);
	}	
	
	/**
	 * enum for Difficulty Settings to make preferences stuff more easy
	 * @author Martin Braun
	 */
	public static enum Difficulty {
		EASY(GameHandlerInfo.EASY_WIN_COUNT),
		MEDIUM(GameHandlerInfo.MEDIUM_WIN_COUNT),
		HARD(GameHandlerInfo.HARD_WIN_COUNT),
		DEFAULT(GameHandlerInfo.DEFAULT_WIN_COUNT);
		
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
