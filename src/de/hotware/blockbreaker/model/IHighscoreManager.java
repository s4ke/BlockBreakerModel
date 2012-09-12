package de.hotware.blockbreaker.model;


public interface IHighscoreManager {

	/**
	 * inserts a new score for the TimeAtttackGameHandler into the database
	 * @param pName
	 * @param pNumberOfWins
	 * @param pNumberOfLosses
	 * @param pScore
	 */
	public void createTimeAttackEntry(String pName, int pNumberOfWins, int pNumberOfLosses, int pScore);
	
	/**
	 * ensures that the given name is existent in the database
	 * and if not creates an DB entry for the name in the names
	 * table
	 */
	public void ensureNameExistsInDB(String pName);
	
}
