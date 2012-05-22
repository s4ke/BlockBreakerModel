package de.hotware.blockbreaker.model.gamehandler;

import de.hotware.blockbreaker.model.gamehandler.IBlockBreakerMessageView.IInputSeedCallback;

/**
 * The DefaultGameHandler
 * @author Martin Braun
 */
public class DefaultGameTypeHandler extends BaseGameTypeHandler {
	
	private DefaultInputSeedCallback mCallback;
	
	public DefaultGameTypeHandler() {
		super();
		this.mCallback = new DefaultInputSeedCallback();
	}

	@Override
	public void onGameEnd(final GameEndEvent pEvt) {
//		this.blockBreakerActivity.runOnUiThread(new Runnable() {
//			public void run() {
//				this.blockBreakerActivity.showEndDialog(pEvt.getType());
//			}
//		});
	}

	@Override
	public void requestRestart() {
		this.mEngineBindings.restartLevel(this);
	}

	@Override
	public void requestNextLevel() {
		this.mEngineBindings.randomLevel(this);
	}

	@Override
	public void requestSeedInput() {
		this.mEngineBindings.mBlockBreakerMessageView.showInputSeed(this.mCallback);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////						Inner Classes					///////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	
	private class DefaultInputSeedCallback implements IInputSeedCallback {

		@Override
		public void onSeedChosen(long pSeed) {
			DefaultGameTypeHandler.this.mEngineBindings.randomLevelFromSeed(
					DefaultGameTypeHandler.this, pSeed);
		}
		
	}
	
//	private void showEndDialog(final GameEndType pResult) {
//		String resString;
//
//		switch(pResult) {
//			case WIN: {
//				resString = this.blockBreakerActivity.getString(R.string.win_text);
//				break;
//			}
//			case LOSE: {
//				resString = this.blockBreakerActivity.getString(R.string.lose_text);
//				break;
//			}
//			default: {
//				resString = "WTF?";
//				break;
//			}
//		}
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(this.blockBreakerActivity);
//		builder.setMessage(resString + " " + this.blockBreakerActivity.getString(R.string.restart_question))
//				.setCancelable(true)
//				.setPositiveButton(this.blockBreakerActivity.getString(R.string.yes),
//						new DialogInterface.OnClickListener() {
//							
//							@Override
//							public void onClick(DialogInterface pDialog, int pId) {
//								pDialog.dismiss();
//								this.blockBreakerActivity.restartLevel();
//							}
//							
//				})
//				.setNegativeButton(this.blockBreakerActivity.getString(R.string.no),
//						new DialogInterface.OnClickListener() {
//							
//							@Override
//							public void onClick(DialogInterface pDialog, int pId) {
//								pDialog.dismiss();
//								this.blockBreakerActivity.randomLevel();
//							}
//							
//				});
//		builder.create().show();
//	}

}