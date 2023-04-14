package com.tabnine.binary.fetch;

import static com.tabnine.general.StaticConfig.getActiveVersionPath;
import static com.tabnine.general.StaticConfig.getBaseDirectory;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.framework.Version;

import com.tabnine.Log;

public class LocalBinaryVersions {
	public static final String BAD_VERSION = "4.0.47";
	private BinaryValidator binaryValidator;

	public LocalBinaryVersions(BinaryValidator binaryValidator) {
		this.binaryValidator = binaryValidator;
	}

	public List<BinaryVersion> listExisting() {
		File[] versionsFolders = Optional.ofNullable(getBaseDirectory().toFile().listFiles()).orElse(new File[0]);

		return Stream.of(versionsFolders).map(File::getName).map(Version::new).filter(Objects::nonNull)
				.sorted(Comparator.reverseOrder()).map(Version::toString).map(BinaryVersion::new)
				.filter(version -> !version.getVersion().equals(BAD_VERSION)
						&& binaryValidator.isWorking(version.getVersionFullPath()))
				.collect(toList());
	}

	public Optional<BinaryVersion> activeVersion() {
		List<String> lines = readActiveFile();

		if (lines.size() == 0)
			return Optional.empty();

		String version = lines.get(0);

		if (version.equals(BAD_VERSION))
			return Optional.empty();

		BinaryVersion binaryVersion = new BinaryVersion(version);

		if (!binaryValidator.isWorking(binaryVersion.getVersionFullPath())) {
			Log.warning("Version in .active file is not working");
			return Optional.empty();
		}

		return Optional.of(binaryVersion);
	}

	private List<String> readActiveFile() {
		Path activeFile = getActiveVersionPath();

		List<String> lines = new ArrayList<>();
		if (activeFile.toFile().exists()) {
			try {
				lines = Files.readAllLines(activeFile);
			} catch (IOException e) {
				Log.warning("Failed to read .active file", e);
			}
		}

		return lines;
	}
}
