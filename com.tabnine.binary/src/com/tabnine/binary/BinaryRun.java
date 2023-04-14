package com.tabnine.binary;

import static com.tabnine.general.Utils.cmdSanitize;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.tabnine.Log;
import com.tabnine.binary.exceptions.NoValidBinaryToRunException;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.fetch.BinaryVersionFetcher;
import com.tabnine.general.PreferenceUtils;
import com.tabnine.general.StaticConfig;

public class BinaryRun {
	private final BinaryVersionFetcher binaryFetcher;

	public BinaryRun(BinaryVersionFetcher binaryFetcher) {
		this.binaryFetcher = binaryFetcher;
	}

	public List<String> generateRunCommand(Map<String, Object> additionalMetadata) throws NoValidBinaryToRunException {
		List<String> command = new ArrayList<>(singletonList(binaryFetcher.fetchBinary()));

		command.addAll(getBinaryConstantParameters(additionalMetadata));

		return command;
	}

	public Process reportUninstall(Map<String, Object> additionalMetadata)
			throws NoValidBinaryToRunException, TabNineDeadException {
		String fullLocation = binaryFetcher.fetchBinary();
		List<String> command = new ArrayList<>(asList(fullLocation, StaticConfig.UNINSTALLING_FLAG));

		command.addAll(getBinaryConstantParameters(additionalMetadata));
		Log.debug(() -> "Starting TabNine binary: " + String.join(" ", command));
		
		try {
			return new ProcessBuilder(command).start();
		} catch (IOException e) {
			throw new TabNineDeadException(e, fullLocation);
		}
	}

	private ArrayList<String> getBinaryConstantParameters(Map<String, Object> additionalMetadata) {
		ArrayList<String> constantParameters = new ArrayList<>();
		List<String> metadata = new ArrayList<>(asList("--client-metadata",
				// "pluginVersion=" + cmdSanitize(getTabNinePluginVersion()),
				// "clientChannel=" + Config.CHANNEL,
				// "pluginUserId=" + PermanentInstallationID.get(),
				"debounceValue=" + StaticConfig.getDebounceInterval()));

		metadata.addAll(Arrays.asList(PreferenceUtils.getAdditionalMetadata()));

		// constantParameters.add("--client");
		// constantParameters.add(cmdSanitize(applicationInfo.getVersionName()));
		constantParameters.add("--no-lsp");
		constantParameters.add("true");

//		metadata.add("clientVersion=" + cmdSanitize(applicationInfo.getFullVersion()));
//		metadata.add("clientApiVersion=" + cmdSanitize(applicationInfo.getApiVersion()));

		if (additionalMetadata != null) {
			additionalMetadata
					.forEach((key, value) -> metadata.add(String.format("%s=%s", key, cmdSanitize(value.toString()))));
		}
		constantParameters.add("--log-file-path");
		constantParameters.add(StaticConfig.getLogFilePath());
		constantParameters.add("--log-level");
		constantParameters.add(StaticConfig.getLogLevel());

		constantParameters.addAll(Arrays.asList(PreferenceUtils.getAdditionalArguments()));
		
		constantParameters.addAll(metadata);
		
		return constantParameters;
	}
}
