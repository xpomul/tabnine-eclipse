package com.tabnine.binary.fetch;

import static com.tabnine.general.StaticConfig.getTabNineBetaVersionUrl;
import static com.tabnine.general.Utils.readContent;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

import com.tabnine.Log;
import com.tabnine.general.StaticConfig;

public class BinaryRemoteSource {
	public Optional<String> fetchPreferredVersion() {
		String serverUrl = StaticConfig.getTabNineBundleVersionUrl();
		return fetchPreferredVersion(serverUrl);
	}

	public Optional<String> fetchPreferredVersion(String url) {
		try {
			return Optional.of(remoteVersionRequest(url));
		} catch (IOException e) {
			Log.warning("Request of current version failed. Falling back to latest local version.",
					e);
			return Optional.empty();
		}
	}

	public Optional<BinaryVersion> existingLocalBetaVersion(List<BinaryVersion> localVersions) {
		try {
			String remoteBetaVersion = remoteVersionRequest(getTabNineBetaVersionUrl());

			return localVersions.stream().filter(version -> remoteBetaVersion.equals(version.getVersion())).findAny();
		} catch (IOException e) {
			Log.warning("Request of current version failed. Falling back to latest local version.",
					e);
		}

		return Optional.empty();
	}

	private String remoteVersionRequest(String url) throws IOException {
		URLConnection connection = new URL(url).openConnection();

		connection.setConnectTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);
		connection.setReadTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);

		return readContent(connection.getInputStream());
	}
}
