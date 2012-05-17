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
import de.hotware.blockbreaker.model.gamehandler.ITimeUpdater.ITimePassedCallback;
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
	ILevelSceneHandler mLevelSceneHandler;
	int mScore;

	public TimeAttackGameTypeHandler(ILevelSceneHandler pLevelSceneHandler,
			ITimeUpdater pTimeUpdater) {
		this(pLevelSceneHandler, pTimeUpdater, DEFAULT_DURATION_IN_SECONDS, DEFAULT_NUMBER_OF_ALLOWED_LOSES);
	}

	public TimeAttackGameTypeHandler(ILevelSceneHandler pLevelSceneHandler,
			ITimeUpdater pTimeUpdater,
			int pDurationInSeconds,
			int pNumberOfAllowedLoses) {
		super();
		this.mDurationInSeconds = pDurationInSeconds;
		this.mNumberOfAllowedLoses = pNumberOfAllowedLoses;
		this.mGamesWon = 0;
		this.mGamesLost = 0;
		this.mScore = 0;
		this.mTimePassedInSeconds = 0;
		this.mLevelSceneHandler = pLevelSceneHandler;
		this.mLevelSceneHandler.setStatusActive(true);
		this.mLevelSceneHandler.setTimeLeftActive(true);
		this.mTimeUpdater = pTimeUpdater;
		this.mTimeUpdater.setTime(pDurationInSeconds);
		this.mTimeUpdater.setUpdateTime(1.0F);
		this.mTimeUpdater.setTimePassedCallback(new ITimePassedCallback() {

			@Override
			public void onTimePassed(int pSeconds) {
					int timeLeft = (int)Math.round(
							TimeAttackGameTypeHandler.this.mDurationInSeconds - 
							(++TimeAttackGameTypeHandler.this.mTimePassedInSeconds));
					TimeAttackGameTypeHandler.this.mLevelSceneHandler.setTimeLeft(timeLeft);
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
						this.mLevelSceneHandler.getLevel().
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
		this.blockBreakerActivity.mDifficulty = Difficulty.EASY;
		//and the rest
		if(this.mTimePassedInSeconds < this.mDurationInSeconds
				&& this.mGamesLost < this.mNumberOfAllowedLoses) {
			this.blockBreakerActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(this.blockBreakerActivity);
					builder.setMessage(this.blockBreakerActivity.getString(R.string.time_attack_start_text))
					.setCancelable(false)
					.setPositiveButton(this.blockBreakerActivity.getString(R.string.start), 
							new DialogInterface.OnClickListener() {
						
								@Override
								public void onClick(DialogInterface pDialog, int pId) {
									this.blockBreakerActivity.mLevelSceneHandler.setIgnoreInput(false);
									this.blockBreakerActivity.mEngine.registerUpdateHandler(this.blockBreakerActivity.mTimeUpdateHandler);
									pDialog.dismiss();
								}

							});
					builder.create().show();
				}

			});
		} else {
			this.showTimeAttackEndDialog();
		}
	}

	@Override
	public void onLeaveFocus() {
		this.blockBreakerActivity.mLevelSceneHandler.setIgnoreInput(true);
		this.blockBreakerActivity.mEngine.unregisterUpdateHandler(this.mTimeUpdateHandler);
	}

	@Override
	public boolean requestLeaveToMenu() {
		return true;
	}

	@Override
	public void requestRestart() {
		//make sure everything is set back to normal
		this.blockBreakerActivity.mEngine.unregisterUpdateHandler(this.blockBreakerActivity.mTimeUpdateHandler);
		this.reset();
		this.blockBreakerActivity.randomLevel();
		//ready, set go!
		this.blockBreakerActivity.mEngine.registerUpdateHandler(this.blockBreakerActivity.mTimeUpdateHandler);
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
		this.mTimeUpdateHandler.reset();
		this.mTimeUpdateHandler.setAutoReset(true);
		this.blockBreakerActivity.mLevelSceneHandler.setIgnoreInput(false);
		this.updateStatusText();
	}

	private void updateStatusText() {
		this.mStatusText.setText(this.blockBreakerActivity.getString(R.string.score) + ": " + this.mScore);
	}

}