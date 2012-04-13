package de.hotware.blockbreaker.model;

import java.io.Serializable;
import java.util.ArrayList;

import de.hotware.blockbreaker.model.Block.BlockColor;
import de.hotware.blockbreaker.model.listeners.IGameEndListener;
import de.hotware.blockbreaker.model.listeners.IGravityListener;
import de.hotware.blockbreaker.model.listeners.INextBlockListener;
import de.hotware.blockbreaker.model.listeners.IGameEndListener.GameEndEvent;
import de.hotware.blockbreaker.model.listeners.IGameEndListener.GameEndEvent.GameEndType;
import de.hotware.blockbreaker.model.listeners.IGravityListener.GravityEvent;
import de.hotware.blockbreaker.model.listeners.INextBlockListener.NextBlockChangedEvent;
import de.hotware.blockbreaker.util.misc.Randomizer;

/**
 * Class for the Game mechanics in BlockBreaker
 * TODO: GameEndListener
 * @author Martin Braun
 */
public class Level implements Serializable {

	////////////////////////////////////////////////////////////////////
	////							Constants						////
	////////////////////////////////////////////////////////////////////
	private static final long serialVersionUID = -1037049912770927906L;	
	protected static final int INFINITE_BLOCKS_LEFT = 999;

	////////////////////////////////////////////////////////////////////
	////							Fields							////
	////////////////////////////////////////////////////////////////////
	protected Block[][] mMatrix;
	protected Gravity mGravity;
	protected Block mNextBlock;
	protected ArrayList<Block> mReplacementList;
	protected WinCondition mWinCondition;
	protected INextBlockListener mNextBlockListener;
	protected IGameEndListener mGameEndListener;
	protected IGravityListener mGravityListener;
	protected int mSizeX;
	protected int mSizeY;
	protected boolean mStarted;
	protected boolean mIgnoreUpdates;

	////////////////////////////////////////////////////////////////////
	////							Constructors					////
	////////////////////////////////////////////////////////////////////
	public Level(Block[][] pMatrix, Gravity pGravity, 
			ArrayList<Block> pReplacementList, 
			WinCondition pWinCondition) {
		this(pMatrix, pGravity);
		this.mSizeX = pMatrix.length;
		this.mSizeY = pMatrix[0].length;
		this.mReplacementList = pReplacementList;
		this.mWinCondition = pWinCondition;
	}

	public Level(Block[][] pMatrix, Gravity pGravity) {
		this.mMatrix = pMatrix;
		this.mGravity = pGravity;
	}
	
	////////////////////////////////////////////////////////////////////
	////					Overriden Methods						////
	////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////
	////							Methods							////
	////////////////////////////////////////////////////////////////////
	
	public Level copy() {
		//deepcopy/create the ReplacementList
		ArrayList<Block> repl = new ArrayList<Block>(this.mReplacementList.size());
		Block var = null;
		for(int x = this.mReplacementList.size(); x > 0; ) {
			var = this.mReplacementList.get(--x);
			repl.add(0, new Block(var.getColor()));
		}
		//deepcopy the matrix
		Block[][] matrix = new Block[this.mSizeX][this.mSizeY];
		for(int i = 0; i < this.mSizeX; ++i) {
			for(int j = 0; j < this.mSizeY; ++j) {
				matrix[i][j] = new Block(this.mMatrix[i][j].getColor(), this.mMatrix[i][j].getX(), this.mMatrix[i][j].getY());
			}
		}
		return new Level(matrix, this.mGravity, repl, (WinCondition)this.mWinCondition.copy());
	}
	
	/**
	 * initializes the Level
	 */
	public synchronized void start() {
		if(!this.mStarted) {
			this.nextBlock();
			this.mStarted = true;
		}
	}
	
	/**
	 * removes the Block at the specified Position and inserts a
	 * new Block according to the current Gravity
	 * @param pX
	 * @param pY
	 * @param pNewBlock
	 * @return the removed Block
	 */
	public synchronized Block removeBlock(int pX, int pY, Block pNewBlock) {
		Block oldBlock = this.mMatrix[pX][pY];
		Block var;
		switch(this.mGravity) {
			case NORTH: {
				for(int i = pY; i > 0; --i) {
					var = this.mMatrix[pX][i-1];
					var.setPosition(pX,i);
					this.mMatrix[pX][i] = var;
				}
				pNewBlock.setPosition(pX,0);
				break;
			}
			case EAST: {
				for(int i = pX; i < this.mSizeX-1; ++i) {
					var = this.mMatrix[i+1][pY];
					var.setPosition(i, pY);
					this.mMatrix[i][pY] = var;
				}
				pNewBlock.setPosition(this.mSizeX-1,pY);;
				break;
			}
			case SOUTH: {
				for(int i = pY; i < this.mSizeY-1; ++i) {
					var = this.mMatrix[pX][i+1];
					var.setPosition(pX, i);
					this.mMatrix[pX][i] = var;
				}
				pNewBlock.setPosition(pX,this.mSizeY-1);
				break;
			}
			case WEST: {
				for(int i = pX; i > 0; --i) {
					var = this.mMatrix[i-1][pY];
					var.setPosition(i, pY);
					this.mMatrix[i][pY] = var;
				}
				pNewBlock.setPosition(0,pY);
				break;
			}
		}
		this.mMatrix[pNewBlock.getX()][pNewBlock.getY()] = pNewBlock;
		return oldBlock;
	}
	
	/**
	 * kills a Block from the matrix at the specified position
	 * @return the Block that was added after killing
	 */
	public synchronized Block killBlock(int pX, int pY) {
		Block newBlock = this.mNextBlock;
		if(newBlock.getColor() != BlockColor.NONE) {
			this.removeBlock(pX, pY, newBlock);
			this.nextBlock();
			if(this.mWinCondition != null && this.mGameEndListener != null) {
				if(this.checkWin()) {
					this.mGameEndListener.onGameEnd(new GameEndEvent(this, GameEndType.WIN));
				} else if (this.mReplacementList.size() == 0 && this.mNextBlock.getColor() == BlockColor.NONE) {
					this.mGameEndListener.onGameEnd(new GameEndEvent(this, GameEndType.LOSE));
				}
			}
		}	
		return newBlock;
	}

	/**
	 * sets the nextBlock (either random or according to the replacement list)
	 */
	protected synchronized void nextBlock() {
		if(this.mReplacementList != null) {
			if(this.mReplacementList.size() > 0) {
				//return the next block in the replacement list
				this.mNextBlock = this.mReplacementList.remove(0);
			} else {
				//return a dummy block
				this.mNextBlock = new Block(BlockColor.NONE);
			}			
		} else {
			this.mNextBlock = new Block(Block.BlockColor.random());
		}
		if(this.mNextBlockListener != null) {
			this.mNextBlockListener.onNextBlockChanged(new NextBlockChangedEvent(this, this.mNextBlock));
		}
	}

	/**
	 * Used for checking if player has won or lost. Only use this if WinCondition has been set!
	 */
	public boolean checkWin() {
		boolean win = true; 
		boolean help = false;
		int biggestColorNumber = BlockColor.getBiggestColorNumber();
		int winCountVar;
		BlockColor blockColorVar;
		for(int i = BlockColor.getLowestColorNumber(); i <= biggestColorNumber && win; ++i) {
			winCountVar = this.mWinCondition.getWinCount(i);
			blockColorVar = BlockColor.numberToColor(i);
			for(int j = 0; j < this.mSizeX && !help; ++j) {
				help = (help || this.checkRow(j, blockColorVar, winCountVar) || this.checkColumn(j, blockColorVar, winCountVar)) ;
			}
			win = win && help;
			help = false;
		}
		return win;
	}

	private boolean checkRow(int pX, BlockColor pColorCheck, int pWinCount) {
		int counter = 0;
		for(int i = 0; i < this.mSizeX; ++i) {
			if(this.mMatrix[pX][i].getColor() == pColorCheck) {
				++counter;
			} else {
				counter = 0;
			}
			if(counter == pWinCount) {
				return true;
			}
		}
		return false;
	}

	private boolean checkColumn(int pY, BlockColor pColorCheck, int pWinCount) {
		int counter = 0;
		for(int i = 0; i < this.mSizeY; ++i) {
			if(this.mMatrix[i][pY].getColor() == pColorCheck) {
				++counter;
			} else {
				counter = 0;
			}
			if(counter == pWinCount) {
				return true;
			}
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////
	////							Getter/Setter					////
	////////////////////////////////////////////////////////////////////
	public Block getNextBlock() {
		return this.mNextBlock;
	}

	public int getBlocksLeft() {
		if(this.mReplacementList != null) {
			if(this.mNextBlock.getColor() == BlockColor.NONE) {
				return 0;
			} else {
				return this.mReplacementList.size() + 1;
			}	
		} else {
			return INFINITE_BLOCKS_LEFT;
		}
	}
	
	public ArrayList<Block> getReplacementList() {
		return this.mReplacementList;
	}

	public String getBlocksDisplayText() {
		String turnsLeft = "00" + this.getBlocksLeft();
		int length = turnsLeft.length();
		return turnsLeft.substring(length-3, length);
	}

	public synchronized void setGravity(Gravity pGravity) {
		if(this.mGravity != pGravity) {
			this.mGravity = pGravity;
			if(this.mGravityListener != null) {
				this.mGravityListener.onGravityChanged(new GravityEvent(this, pGravity));
			}
		}
	}
	
	public void switchToNextGravity() {
		if(this.mGravity == Gravity.WEST) {
			this.setGravity(Gravity.NORTH);
		} else {
			this.setGravity(Gravity.numberToGravity(this.mGravity.toNumber()+1));
		}
	}

	public synchronized Gravity getGravity() {
		return this.mGravity;	
	}

	public Block[][] getMatrix() {
		return this.mMatrix;
	}

	public WinCondition getWinCondition() {
		return this.mWinCondition;
	}
	
	public void setIgnoreUpdates(boolean pIgnoreUpdates) {
		this.mIgnoreUpdates = pIgnoreUpdates;
	}

	public void setNextBlockListener(INextBlockListener pNextBlockListener) {
		this.mNextBlockListener = pNextBlockListener;
	}

	public void setGameEndListener(IGameEndListener pGameEndListener) {
		this.mGameEndListener = pGameEndListener;
	}

	public void setGravityListener(IGravityListener pGravityListener) {
		this.mGravityListener = pGravityListener;
	}

	////////////////////////////////////////////////////////////////////
	////							Inner Classes					////
	////////////////////////////////////////////////////////////////////
	public enum Gravity {
		NORTH(0),
		EAST(1),
		SOUTH(2),
		WEST(3);	
		
		private int mX;	
		
		private Gravity(int pX) {
			this.mX = pX;
		}
		
		public int toNumber() {
			return this.mX;
		}
		
		public static Gravity random() {
			return numberToGravity(Randomizer.nextInt(4));
		}
		
		public static Gravity numberToGravity(int pX) {
			switch(pX) {
				case 0: return NORTH;
				case 1: return EAST;
				case 2: return SOUTH;
				case 3: return WEST;
				default: return NORTH;
			}
		}
	}

}
