package com.tabnine.general;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.FrameworkUtil;

import com.tabnine.Activator;

public class StaticConfig {
	// Must be identical to what is written under <id>com.tabnine.TabNine</id> in
	// plugin.xml !!!
	public static final String TABNINE_PLUGIN_ID_RAW = "com.tabnine.TabNine";
	public static final int MAX_COMPLETIONS = 5;
	public static final String BINARY_PROTOCOL_VERSION = "4.4.223";
	public static final int COMPLETION_TIME_THRESHOLD = 3000;
	public static final int NEWLINE_COMPLETION_TIME_THRESHOLD = 10000;
	public static final int ILLEGAL_RESPONSE_THRESHOLD = 5;
	public static final int ADVERTISEMENT_MAX_LENGTH = 100;
	public static final int MAX_OFFSET = 100000; // 100 KB
	public static final int SLEEP_TIME_BETWEEN_FAILURES = 1000;
	public static final int BINARY_MINIMUM_REASONABLE_SIZE = 1000 * 1000; // roughly 1MB
	public static final String SET_STATE_RESPONSE_RESULT_STRING = "Done";
	public static final String UNINSTALLING_FLAG = "--uninstalling";
	public static final int BINARY_TIMEOUTS_THRESHOLD_MILLIS = 60_000;
	public static final String BRAND_NAME = "tabnine";
	public static final String TARGET_NAME = getDistributionName();
	public static final String EXECUTABLE_NAME = getExeName();
	public static final String TABNINE_FOLDER_NAME = ".tabnine";
	public static final int BINARY_READ_TIMEOUT = 5 * 60 * 1000; // 5 minutes
	public static final int REMOTE_CONNECTION_TIMEOUT = 5_000; // 5 seconds
	public static final long BINARY_NOTIFICATION_POLLING_INTERVAL = 10_000L; // 10 seconds
	public static final String USER_HOME_PATH_PROPERTY = "user.home";
	public static final String REMOTE_BASE_URL_PROPERTY = "TABNINE_REMOTE_BASE_URL";
	public static final String REMOTE_VERSION_URL_PROPERTY = "TABNINE_REMOTE_VERSION_URL";
	public static final String TABNINE_ENTERPRISE_HOST = "TABNINE_ENTERPRISE_HOST";
	public static final String REMOTE_BETA_VERSION_URL_PROPERTY = "TABNINE_REMOTE_BETA_VERSION_URL";
	public static final String LOG_FILE_PATH_PROPERTY = "TABNINE_LOG_FILE_PATH";
	public static final String LIMITATION_SYMBOL = "🔒";
	private static final int MAX_SLEEP_TIME_BETWEEN_FAILURES = 1_000 * 60 * 60; // 1 hour
	public static final long BINARY_PROMOTION_POLLING_INTERVAL = 2 * 60 * 1_000L; // 2 minutes
	public static final long BINARY_PROMOTION_POLLING_DELAY = 10_000L; // 10 seconds

	public static final String OPEN_HUB_ACTION = "OpenHub";

	public static String getLogFilePath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toPath().resolve("tabnine.log")
				.toString();
	}

	public static String getLogLevel() {
		return System.getProperty("com.tabnine.loglevel", "Error");
	}

	public static String getServerUrl() {
		return Optional.ofNullable(System.getProperty(REMOTE_BASE_URL_PROPERTY)).orElse("https://update.tabnine.com");
	}

	public static String getBundleServerUrl() {
		return Optional.ofNullable(System.getProperty(REMOTE_BASE_URL_PROPERTY))
				.orElse("https://update.tabnine.com/bundles");
	}

	public static String getTabNineBundleVersionUrl() {
		return Optional.ofNullable(System.getProperty(REMOTE_VERSION_URL_PROPERTY))
				.orElse(StaticConfig.getBundleServerUrl() + "/version");
	}

	public static String getTabNineBetaVersionUrl() {
		return Optional.ofNullable(System.getProperty(REMOTE_BETA_VERSION_URL_PROPERTY))
				.orElse(getServerUrl() + "/beta_version");
	}

	public static void sleepUponFailure(int attempt) throws InterruptedException {
		Thread.sleep(Math.min(exponentialBackoff(attempt), MAX_SLEEP_TIME_BETWEEN_FAILURES));
	}

	public static int exponentialBackoff(int attempt) {
		return SLEEP_TIME_BETWEEN_FAILURES * (int) Math.pow(2, Math.min(attempt, 30));
	}

	private static String getDistributionName() {
		String arch = System.getProperty("os.arch");
		String tabnineArch;
		if ("amd64".equals(arch) || "x86_64".equals(arch)) {
			tabnineArch = "x86_64";
		} else if ("aarch64".equals(arch)) {
			tabnineArch = "aarch64";
		} else {
			tabnineArch = "i686";
		}

		String platform;
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			platform = "pc-windows-gnu";
		} else if (os.contains("mac")) {
			platform = "apple-darwin";
		} else if (os.contains("linux")) {
			platform = "unknown-linux-musl";
		} else if (os.contains("freebsd")) {
			platform = "unknown-freebsd";
		} else {
			throw new RuntimeException("Platform was not recognized as any of Windows, macOS, Linux, FreeBSD");
		}

		return tabnineArch + "-" + platform;
	}

	public static Path getBaseDirectory() {
		var stateLoc = Platform.getStateLocation(FrameworkUtil.getBundle(StaticConfig.class));
		return stateLoc.toFile().toPath();
	}

	public static Path getActiveVersionPath() {
		return getBaseDirectory().resolve(".active");
	}

	private static String getExeName() {
		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
		return isWindows ? "TabNine.exe" : "TabNine";
	}

	public static String versionFullPath(String version) {
		return Paths.get(getBaseDirectory().toString(), version, TARGET_NAME, EXECUTABLE_NAME).toString();
	}

	public static String bundleFullPath(String version) {
		return Paths.get(getBaseDirectory().toString(), version, TARGET_NAME, "TabNine.zip").toString();
	}

	public static Map<String, Object> wrapWithBinaryRequest(Object value) {
		Map<String, Object> jsonObject = new HashMap<>();

		jsonObject.put("version", BINARY_PROTOCOL_VERSION);
		jsonObject.put("request", value);

		return jsonObject;
	}

	public static String getDebounceInterval() {
		return "500";
	}
}
