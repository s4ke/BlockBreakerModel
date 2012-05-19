package de.hotware.blockbreaker.model.gamehandler;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.text.Text;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import de.hotware.blockbreaker.android.BlockBreakerActivity;
import de.hotware.blockbreaker.android.R;
import de.hotware.blockbreaker.android.R.string;
import de.hotware.blockbreaker.android.view.LevelSceneHandler;
import de.hotware.blockbreaker.model.gamehandler.GameHandlerInfo.Difficulty;
import de.hotware.blockbreaker.model.gamehandler.ITimeUpdater.ITimePassedCallback;
import de.hotware.blockbreaker.model.gamehandler.TimeAttackGameTypeHandler.ITimeAttackMessenger.ITimeAttackEndDialogCallback;
import de.hotware.blockbreaker.model.gamehandler.TimeAttackGameTypeHandler.ITimeAttackMessenger.ITimeAttackStartDialogCallback;
import de.hotware.blockbreaker.model.listeners.IGameEndListener.GameEndEvent;

/**
 * The TimeAttackGameHandler
 * @author Martin Braun
 */
class TimeAttackGameTypeHandler extends BaseGameTypeHandler {

	//Time Constants
	private static final int DEFAULT_DURATION_IN_SECONDS = 120;
	private static final int GAME_WIN_TIME_BONUS_IN_SECONDS = 15;
	
	//Game specific Constants
	private static final int DEFAULT_NUMBER_OF_ALLOWED_LOSES = 2;
	private static final int GAME_WIN_POINT_BONUS = 100;
	private static final int BLOCK_LEFT_POINT_BONUS = 10;
	private static final int GAME_LOSE_POINT_BONUS = -50;

	int mDurationInSeconds;
	int mTimePassedInSeconds;
	int mNumberOfAllowedLoses;
	int mGamesLost;
	int mGamesWon;
	ITimeUpdater mTimeUpdater;
	int mScore;
	ITimeAttackMessenger mTimeAttackMessenger;

	public TimeAttackGameTypeHandler(ITimeUpdater pTimeUpdater, ITimeAttackMessenger pTimeAttackMessenger) {
		this(pTimeUpdater,
				pTimeAttackMessenger,
				DEFAULT_DURATION_IN_SECONDS,
				DEFAULT_NUMBER_OF_ALLOWED_LOSES);
	}

	public TimeAttackGameTypeHandler(ITimeUpdater pTimeUpdater,
			ITimeAttackMessenger pTimeAttackMessenger,
			int pDurationInSeconds,
			int pNumberOfAllowedLoses) {
		super();
		this.mDurationInSeconds = pDurationInSeconds;
		this.mNumberOfAllowedLoses = pNumberOfAllowedLoses;
		this.mGamesWon = 0;
		this.mGamesLost = 0;
		this.mScore = 0;
		this.mTimePassedInSeconds = 0;
		this.mGameHandlerInfo.mLevelSceneHandler.setStatusActive(true);
		this.mGameHandlerInfo.mLevelSceneHandler.setTimeLeftActive(true);
		this.mTimeAttackMessenger = pTimeAttackMessenger;
		this.mTimeUpdater = pTimeUpdater;
		this.mTimeUpdater.setTime(pDurationInSeconds);
		this.mTimeUpdater.setUpdateTime(1.0F);
		this.mTimeUpdater.setTimePassedCallback(new ITimePassedCallback() {

			@Override
			public void onTimePassed(int pSeconds) {
					int timeLeft = (int)Math.round(
							TimeAttackGameTypeHandler.this.mDurationInSeconds - 
							(++TimeAttackGameTypeHandler.this.mTimePassedInSeconds));
					TimeAttackGameTypeHandler.this.mGameHandlerInfo.mLevelSceneHandler.setTimeLeft(timeLeft);
			}

			@Override
			public void onTimeEnd() {
				TimeAttackGameTypeHandler.this.onTimeAttackEnd();
			}
			
		});
	}

	@Override
	public void onGameEnd(GameEndEvent pEvt) {
		switch(pEvt.getType()) {
			case WIN: {
				this.mScore = this.mScore + GAME_WIN_POINT_BONUS + 
						this.mGameHandlerInfo.mLevelSceneHandler.getLevel().
							getBlocksLeft() * BLOCK_LEFT_POINT_BONUS;
				synchronized(this) {
					this.mTimePassedInSeconds -= GAME_WIN_TIME_BONUS_IN_SECONDS;
				}
				++this.mGamesWon;
				this.mGameHandlerInfo.randomLevel(this);
				this.updateStatusText();
				break;
			}
			case LOSE: {
				this.requestNextLevel();
				break;
			}
		}
	}

	@Override
	public void onEnterFocus() {
		//assure that some settings are at default just for this gamemode
		this.mGameHandlerInfo.setDifficulty(Difficulty.EASY);
		//and the rest
		if(this.mTimePassedInSeconds < this.mDurationInSeconds
				&& this.mGamesLost < this.mNumberOfAllowedLoses) {
			this.mTimeAttackMessenger.showTimeAttackStartDialog(TimeAttackMessengerCallback.INSTANCE);
			
//			this.blockBreakerActivity.runOnUiThread(new Runnable() {
//
//				@Override
//				public void run() {
//					AlertDialog.Builder builder = new AlertDialog.Builder(this.blockBreakerActivity);
//					builder.setMessage(this.blockBreakerActivity.getString(R.string.time_attack_start_text))
//					.setCancelable(false)
//					.setPositiveButton(this.blockBreakerActivity.getString(R.string.start), 
//							new DialogInterface.OnClickListener() {
//						
//								@Override
//								public void onClick(DialogInterface pDialog, int pId) {
//									this.blockBreakerActivity.mLevelSceneHandler.setIgnoreInput(false);
//									this.blockBreakerActivity.mEngine.registerUpdateHandler(this.blockBreakerActivity.mTimeUpdateHandler);
//									pDialog.dismiss();
//								}
//
//							});
//					builder.create().show();
//				}
//
//			});
		} else {
			this.mTimeAttackMessenger.showTimeAttackEndDialog(TimeAttackMessengerCallback.INSTANCE);
		}
	}

	@Override
	public void onLeaveFocus() {
		this.mGameHandlerInfo.mLevelSceneHandler.setIgnoreInput(true);
		this.mTimeUpdater.pause();
	}

	@Override
	public boolean requestLeaveToMenu() {
		return true;
	}

	@Override
	public void requestRestart() {
		//make sure everything is set back to normal
		this.mTimeUpdater.stop();
		this.reset();
		this.mGameHandlerInfo.randomLevel(this);
		//ready, set go!
		this.mTimeUpdater.start();
	}

	@Override
	public void requestNextLevel() {
		this.mScore = this.mScore + GAME_LOSE_POINT_BONUS;
		this.updateStatusText();
		++this.mGamesLost;
		if(this.mGamesLost <= this.mNumberOfAllowedLoses) {
			this.blockBreakerActivity.randomLevel();
		} else {
			this.onTimeAttackEnd();
		}
	}

	@Override
	public void init() {
		this.mTimeLeftText = this.blockBreakerActivity.mLevelSceneHandler.getTimeLeftText();
		this.mTimeLeftText.setVisible(true);
		this.mTimeLeftText.setIgnoreUpdate(false);
		this.mTimeLeftText.setText(Integer.toString(this.mDurationInSeconds));
		this.mTimeText = this.blockBreakerActivity.mLevelSceneHandler.getTimeText();
		this.mTimeText.setVisible(true);
		VertexBufferObjectManager vbo = this.blockBreakerActivity.mEngine.getVertexBufferObjectManager();
		this.mStatusText = new Text(
				5,
				3,
				this.blockBreakerActivity.mMiscFont,
				"",
				15,
				vbo);
		this.updateStatusText();
		this.blockBreakerActivity.mCamera.getHUD().attachChild(this.mStatusText);
	}

	@Override
	public void cleanUp() {
		this.blockBreakerActivity.mEngine.unregisterUpdateHandler(this.mTimeUpdateHandler);
		this.blockBreakerActivity.mLevelSceneHandler.setIgnoreInput(false);
		this.blockBreakerActivity.randomLevel();
		this.mTimeLeftText.setVisible(false);
		this.mTimeLeftText.setIgnoreUpdate(true);
		this.mTimeLeftText.setText("");
		this.mTimeText.setVisible(false);
		this.mStatusText.detachSelf();
	}

	@Override
	public void onNumberOfTurnsPropertyChanged() {
		this.reset();
	}

	public void onTimeAttackEnd() {
		this.blockBreakerActivity.mLevelSceneHandler.setIgnoreInput(true);
		this.mTimeUpdateHandler.setAutoReset(false);
		this.blockBreakerActivity.mHighscoreManager.
			createTimeAttackEntry(this.blockBreakerActivity.mPlayerName,
				this.mGamesWon, this.mGamesLost, this.mScore);
		this.showTimeAttackEndDialog();
	}

	private void showTimeAttackEndDialog() {
		this.blockBreakerActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(this.blockBreakerActivity);
				builder.setMessage(
						this.blockBreakerActivity.getString(R.string.game_over_text)
						+ "\n" + this.blockBreakerActivity.getString(R.string.score_text)
						+ ":\n" + this.blockBreakerActivity.mScore
						+ "\n" + this.blockBreakerActivity.getString(R.string.completed_levels_text)
						+ ":\n" + this.blockBreakerActivity.mGamesWon
						+ "\n" + this.blockBreakerActivity.getString(R.string.lost_levels_text)
						+ ":\n" + this.blockBreakerActivity.mGamesLost)
						.setCancelable(true)
						.setPositiveButton(this.blockBreakerActivity.getString(R.string.restart), 
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface pDialog, int pId) {
										//a restart has been requested
										this.blockBreakerActivity.requestRestart();
										pDialog.dismiss();
									}
						});
				builder.create().show();
			}

		});
	}

	private void reset() {
		this.mScore = 0;
		this.mGamesWon = 0;
		this.mGamesLost = 0;
		this.mTimePassedInSeconds = 0;
		this.mGameHandlerInfo.mLevelSceneHandler.setIgnoreInput(false);
		this.updateStatusText();
	}

	private void updateStatusText() {
		this.mStatusText.setText(this.blockBreakerActivity.getString(R.string.score) + ": " + this.mScore);
	}
	
	public static interface ITimeAttackMessenger {
		
		public void showTimeAttackEndDialog(ITimeAttackEndDialogCallback pCallback);
		
		public void showTimeAttackStartDialog(ITimeAttackStartDialogCallback pCallback);
		
		public static interface ITimeAttackEndDialogCallback {
			
			/**
			 * has to be called when the TimeAttackEndDialog has been dismissed
			 */
			public void onTimeAttackEndDialogEnd();
			
		}
		
		public static interface ITimeAttackStartDialogCallback {
			
			
			/**
			 * has to be called when the TimeAttackStartDialog has been dismissed
			 */
			public void onTimeAttackStartDialogEnd();
			
		}
		
	}
	
	private enum TimeAttackMessengerCallback implements ITimeAttackEndDialogCallback, ITimeAttackStartDialogCallback {
		INSTANCE;

		@Override
		public void onTimeAttackStartDialogEnd() {
			
		}

		@Override
		public void onTimeAttackEndDialogEnd() {
			
		}
		
	}

}