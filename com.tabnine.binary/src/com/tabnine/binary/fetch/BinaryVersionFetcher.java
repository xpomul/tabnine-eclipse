package com.tabnine.binary.fetch;

import static java.lang.String.format;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.core.runtime.Platform;

import com.tabnine.Log;
import com.tabnine.binary.exceptions.NoValidBinaryToRunException;

public class BinaryVersionFetcher {
	private LocalBinaryVersions localBinaryVersions;
	private BinaryRemoteSource binaryRemoteSource;
	private BinaryDownloader binaryDownloader;
	private BundleDownloader bundleDownloader;

	public BinaryVersionFetcher(LocalBinaryVersions localBinaryVersions, BinaryRemoteSource binaryRemoteSource,
			BinaryDownloader binaryDownloader, BundleDownloader bundleDownloader) {
		this.localBinaryVersions = localBinaryVersions;
		this.binaryRemoteSource = binaryRemoteSource;
		this.binaryDownloader = binaryDownloader;
		this.bundleDownloader = bundleDownloader;
	}

	/**
	 * Fetchs TabNine's preferred version from remote server, downloads it if it is
	 * not available, and returns the path to run it.
	 *
	 * @return path of the binary to run
	 * @throws SecurityException, NoExistingBinaryException if something went wrong
	 */
	public String fetchBinary() throws NoValidBinaryToRunException {
		Optional<BinaryVersion> bootstrappedVersion = BootstrapperSupport.bootstrapVersion(localBinaryVersions,
				binaryRemoteSource, bundleDownloader);
		if (bootstrappedVersion.isPresent()) {
			Log.info(format("found local bootstrapped version %s", bootstrappedVersion.get()));
			return bootstrappedVersion.get().getVersionFullPath();
		}
		Log.warning("couldn't get bootstrapped version. fallback to legacy code!");
		List<BinaryVersion> versions = localBinaryVersions.listExisting();

		Optional<BinaryVersion> preferredBetaVersion = binaryRemoteSource.existingLocalBetaVersion(versions);

		if (preferredBetaVersion.isPresent()) {
			Platform.getLog(getClass())
					.info(format("Binary latest beta version %s was found locally, so it is being preferred.",
							preferredBetaVersion.get().getVersion()));

			return preferredBetaVersion.get().getVersionFullPath();
		}

		return binaryRemoteSource.fetchPreferredVersion().map(getLocalOrDownload(versions))
				.orElseGet(versions.stream()::findFirst).map(BinaryVersion::getVersionFullPath)
				.orElseThrow(NoValidBinaryToRunException::new);
	}

	private Function<String, Optional<BinaryVersion>> getLocalOrDownload(List<BinaryVersion> versions) {
		return preferred -> {
			Optional<BinaryVersion> localVersion = versions.stream()
					.filter(version -> preferred.equals(version.getVersion())).findAny();

			if (localVersion.isPresent()) {
				return localVersion;
			}

			Platform.getLog(getClass())
					.info(format("Current binary version %s not found locally, it is being downloaded.", preferred));

			return binaryDownloader.downloadBinary(preferred);
		};
	}
}
