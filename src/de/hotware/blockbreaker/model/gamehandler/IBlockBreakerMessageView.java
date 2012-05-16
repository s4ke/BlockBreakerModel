package de.hotware.blockbreaker.model.gamehandler;

/**
 * class being used to abstract system specific stuff (view) from the GameHandlers
 * @author Martin Braun
 *
 */
public interface IBlockBreakerMessageView {
	
	/**
	 * shows an Exit Dialog
	 */
	public void showExitDialog();
	
	/**
	 * Quits the game with an error Message
	 * @param pMessage
	 */
	public void showFailDialogAndQuit(String pMessage);
	
	/**
	 * shows an input menu where the player can choose a seed
	 * The callback has to be called when the player has chosen the seed.
	 * @param pCallback
	 */
	public void showInputSeed(IInputSeedCallback pCallback);
	
	public static interface IInputSeedCallback {
		
		public void onSeedChosen(long pSeed);
		
	}

}
