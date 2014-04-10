package pl.edu.pw.elka.tin.others;

import java.util.HashSet;

import android.os.Process;

/**
 * Log class helps while debugging. Because of application character, explicit
 * exceptions passing to user is not possible, hence Log class and LogCat is
 * being used.
 * @author Piotr Jastrzebski & Wojciech Kaczorowski
 */
public class Log {

	private final static HashSet<String> noExceptions = new HashSet<String>();

	/**
	 * Log DEBUG method.
	 * 
	 * @param logMe
	 */
	public static void d(String logMe) {
		logD(logMe);
	}

	/**
	 * Log INFO method.
	 * 
	 * @param logMe
	 */
	public static void i(String logMe) {
		logI(logMe);
	}

	/**
	 * Log VERBOSE method.
	 * 
	 * @param logMe
	 */
	public static void v(String logMe) {
		logV(logMe);
	}

	/**
	 * Log WARN method.
	 * 
	 * @param logMe
	 */
	public static void w(String logMe) {
		logW(logMe);
	}

	/**
	 * Log ERROR method.
	 * 
	 * @param logMe
	 */
	public static void e(String logMe) {
		logE(logMe);
	}

	/**
	 * Log WARN method.
	 * 
	 * @param logMe
	 * @param ex
	 */
	public static void w(String logMe, Throwable ex) {
		if (ex == null) {
			logW(logMe);
		} else if (noExceptions.contains(ex.getClass().getName())) {
			logW(logMe + " " + ex.getMessage());
		} else {
			logW(logMe, ex);
		}
	}

	/**
	 * Log ERROR method.
	 * 
	 * @param logMe
	 * @param ex
	 */
	public static void e(String logMe, Throwable ex) {
		if (ex == null) {
			logE(logMe);
		} else if (noExceptions.contains(ex.getClass().getName())) {
			logW(logMe + " " + ex.getMessage());
		} else {
			logE(logMe, ex);
		}
	}

	/**
	 * Log WARN method.
	 * 
	 * @param ex
	 */
	public static void w(Throwable ex) {
		if (ex == null) {
			logW("");
		} else if (noExceptions.contains(ex.getClass().getName())) {
			logW(ex.getMessage());
		} else {
			logW("", ex);
		}
	}

	/**
	 * Log ERROR method.
	 * 
	 * @param ex
	 */
	public static void e(Throwable ex) {
		if (ex == null) {
			logE("");
		} else if (noExceptions.contains(ex.getClass().getName())) {
			logW(ex.getMessage());
		} else {
			logE("", ex);
		}
	}

	/**
	 * Sets as a tag in logs name of class, from which is created. 
	 * 
	 * @return Name of class.
	 */
	private static String tag() {
		StackTraceElement[] es = new Throwable().getStackTrace();
		StackTraceElement e = es[3];
		return e.getFileName();
	}

	private static String info() {
		StackTraceElement[] es = new Throwable().getStackTrace();
		StackTraceElement e = es[3];
		return "["
				+
				// SystemClock.uptimeMillis() + ", " +
				Process.getElapsedCpuTime() + ", " + Process.myTid() + ", "
				+ Thread.currentThread().getId() + "("
				+ Thread.currentThread().getName() + ")" + Thread.activeCount()
				+ ", " + "F" + Runtime.getRuntime().freeMemory() + ", " + "T"
				+ Runtime.getRuntime().totalMemory() + ", " + "M"
				+ Runtime.getRuntime().maxMemory() + ", " + "] " +
				// e.getClassName() + "." +
				e.getMethodName() + ":" +
				// e.getFileName() + ":" +
				e.getLineNumber() + " ";
	}

	public static void addNoException(String noException) {
		noExceptions.add(noException);
	}

	private static void logI(String msg) {
		android.util.Log.i(tag(), info() + msg);
	}

	private static void logD(String msg) {
		android.util.Log.d(tag(), info() + msg);
	}

	private static void logV(String msg) {
		android.util.Log.v(tag(), info() + msg);
	}

	private static void logW(String msg) {
		android.util.Log.w(tag(), info() + msg);
	}

	private static void logW(String msg, Throwable ex) {
		android.util.Log.w(tag(), info() + msg, ex);
	}

	private static void logE(String msg) {
		android.util.Log.e(tag(), info() + msg);
	}

	private static void logE(String msg, Throwable ex) {
		android.util.Log.e(tag(), info() + msg, ex);
	}

	/**
	 * Log TRACE method.
	 * 
	 * @param msg
	 */
	public static void trace(String msg) {
		try {
			throw new Exception(msg);
		} catch (Throwable e) {
			Log.w(e);
		}
	}

}