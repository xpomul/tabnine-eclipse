package com.tabnine;

import java.util.function.Supplier;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

/**
 * Simple log facility to make it possible to log messages without using Eclipse
 * API everywhere.
 * 
 * The Eclipse API does not provide a debug log level, so we use info instead.
 * 
 * @author Stefan Winkler
 */
public final class Log {

	/**
	 * Logging facility.
	 */
	private static ILog log = Platform.getLog(Log.class);

	private Log() {
	}

	public static void error(String s) {
		log.error(s);
	}

	public static void error(String s, Throwable t) {
		log.error(s, t);
	}

	public static void warning(String s) {
		Log.warning(s);
	}

	public static void warning(String s, Throwable t) {
		Log.warning(s, t);
	}

	public static void info(String s) {
		log.info(s);
	}

	public static void info(String s, Throwable t) {
		log.info(s, t);
	}

	public static void debug(String s) {
		if (Platform.inDebugMode()) {
			log.info("DBG: " + s);
		}
	}

	public static void debug(Supplier<String> s) {
		if (Platform.inDebugMode()) {
			log.info("DBG: " + s.get());
		}
	}
}
