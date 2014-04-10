package pl.edu.pw.elka.tin.others;

/**
 * Keeps Application constants, so small changes are easier.
 * 
 * @author Piotr Jastrzebski & Wojciech Kaczorowski
 */
public class AppConstants {
	/**
	 * How often notification should be updated.
	 */
	public static final long NOTIFICATION_UPDATE_INTERVAL = 3000;

	/**
	 * Minimal time, after location changes noticed. Position updated every
	 * minute.
	 */
	public static final long MIN_TIME = 15000;

	/**
	 * Minimal distance, after location changes noticed. We can trace user
	 * position, even when one is not moving.
	 */
	public static final float MIN_DIST = 0;

	/**
	 * HTTP communication timeout
	 */
	public static final int COMMUNICATION_TIMEOUT = 15000;

	/**
	 * Server ip.
	 */
	// public static final String SERVER_IP = "tomatolinksys.dyndns.org";
	// public static final String SERVER_IP = "warszawalinksys.dyndns.org";
	// public static final String SERVER_IP = "192.168.47.77";
	public static final String SERVER_IP = "192.168.46.29";

	/**
	 * Server port.
	 */
	public static final String SERVER_PORT = "8234";

	/**
	 * Phone's IMEI number.
	 */
	public static String IMEI;
}