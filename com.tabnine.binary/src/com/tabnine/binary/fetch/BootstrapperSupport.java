package com.tabnine.binary.fetch;

import com.tabnine.general.StaticConfig;

import java.util.Comparator;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.osgi.framework.Version;

public class BootstrapperSupport {
	static Optional<BinaryVersion> bootstrapVersion(LocalBinaryVersions localBinaryVersions,
			BinaryRemoteSource binaryRemoteSource, BundleDownloader bundleDownloader) {
		Optional<BinaryVersion> localBootstrapVersion = locateLocalBootstrapSupportedVersion(localBinaryVersions);
		if (localBootstrapVersion.isPresent()) {
			return localBootstrapVersion;
		}
		return downloadRemoteVersion(binaryRemoteSource, bundleDownloader);
	}

	public static final String BOOTSTRAPPED_VERSION_KEY = "bootstrapped version";

	private static Preferences getPrefs() {
		return Preferences.userNodeForPackage(BootstrapperSupport.class);
	}

	private static BinaryVersion savePreferredBootstrapVersion(BinaryVersion version) {
		getPrefs().put(BOOTSTRAPPED_VERSION_KEY, version.getVersion());
		return version;
	}

	private static Optional<BinaryVersion> locateLocalBootstrapSupportedVersion(
			LocalBinaryVersions localBinaryVersions) {

		Optional<BinaryVersion> activeVersion = localBinaryVersions.activeVersion();
		if (activeVersion.isPresent())
			return activeVersion;

		return localBinaryVersions.listExisting().stream().max(Comparator.comparing(v -> new Version(v.getVersion())));
	}

	private static Optional<BinaryVersion> downloadRemoteVersion(BinaryRemoteSource binaryRemoteSource,
			BundleDownloader bundleDownloader) {
		String serverUrl = StaticConfig.getTabNineBundleVersionUrl();
		return binaryRemoteSource.fetchPreferredVersion(serverUrl).flatMap(bundleDownloader::downloadAndExtractBundle)
				.map(BootstrapperSupport::savePreferredBootstrapVersion);
	}
}
