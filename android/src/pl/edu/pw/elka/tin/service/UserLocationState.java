package pl.edu.pw.elka.tin.service;

/**
 * Keeps current user state. Kept value depends on user's location (OK, BAD) or
 * NOT_KNOW when zones list is somehow incomplete (missing vertex etc.)
 * 
 * @author Piotr Jastrzebski & Wojciech Kaczorowski
 */
public enum UserLocationState {
	/** 
	 * User INside allowed zone.
	 */
	OK, 

	/**
	 * User OUTside allowed zone.
	 */
	BAD,

	/**
	 * List of zones is incomplete.
	 */
	NOT_KNOWN
}
