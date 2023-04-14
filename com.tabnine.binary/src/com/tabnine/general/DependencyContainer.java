package com.tabnine.general;

import com.tabnine.binary.BinaryProcessGatewayProvider;
import com.tabnine.binary.BinaryProcessRequesterProvider;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.BinaryRun;
import com.tabnine.binary.fetch.BinaryDownloader;
import com.tabnine.binary.fetch.BinaryRemoteSource;
import com.tabnine.binary.fetch.BinaryValidator;
import com.tabnine.binary.fetch.BinaryVersionFetcher;
import com.tabnine.binary.fetch.BundleDownloader;
import com.tabnine.binary.fetch.GeneralDownloader;
import com.tabnine.binary.fetch.LocalBinaryVersions;
import com.tabnine.binary.fetch.TempBinaryValidator;
import com.tabnine.binary.fetch.TempBundleValidator;

public class DependencyContainer {

	public static int binaryRequestsTimeoutsThresholdMillis = StaticConfig.BINARY_TIMEOUTS_THRESHOLD_MILLIS;

	private static BinaryProcessRequesterProvider BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE = null;

	public static BinaryRequestFacade instanceOfBinaryRequestFacade() {
		return new BinaryRequestFacade(singletonOfBinaryProcessRequesterProvider());
	}

	private static BinaryProcessRequesterProvider singletonOfBinaryProcessRequesterProvider() {
		if (BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE == null) {
			BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE = BinaryProcessRequesterProvider.create(instanceOfBinaryRun(),
					instanceOfBinaryProcessGatewayProvider(), binaryRequestsTimeoutsThresholdMillis);
		}

		return BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE;
	}

	private static BinaryProcessGatewayProvider instanceOfBinaryProcessGatewayProvider() {
		return new BinaryProcessGatewayProvider();
	}

	private static BinaryRun instanceOfBinaryRun() {
		return new BinaryRun(instanceOfBinaryFetcher());
	}

	private static BinaryVersionFetcher instanceOfBinaryFetcher() {
		return new BinaryVersionFetcher(instanceOfLocalBinaryVersions(), instanceOfBinaryRemoteSource(),
				instanceOfBinaryDownloader(), instanceOfBundleDownloader());
	}

	private static BinaryDownloader instanceOfBinaryDownloader() {
		return new BinaryDownloader(instanceOfBinaryPropositionValidator(), instanceOfDownloader());
	}

	private static BundleDownloader instanceOfBundleDownloader() {
		return new BundleDownloader(instanceOfBundlePropositionValidator(), instanceOfDownloader());
	}

	private static GeneralDownloader instanceOfDownloader() {
		return new GeneralDownloader();
	}

	private static TempBinaryValidator instanceOfBinaryPropositionValidator() {
		return new TempBinaryValidator(instanceOfBinaryValidator());
	}

	private static TempBundleValidator instanceOfBundlePropositionValidator() {
		return new TempBundleValidator();
	}

	private static BinaryRemoteSource instanceOfBinaryRemoteSource() {
		return new BinaryRemoteSource();
	}

	private static LocalBinaryVersions instanceOfLocalBinaryVersions() {
		return new LocalBinaryVersions(instanceOfBinaryValidator());
	}

	private static BinaryValidator instanceOfBinaryValidator() {
		return new BinaryValidator();
	}
}
