package com.tabnine.binary.fetch;

import static com.tabnine.general.StaticConfig.BINARY_READ_TIMEOUT;
import static com.tabnine.general.StaticConfig.REMOTE_CONNECTION_TIMEOUT;
import static java.lang.String.format;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.eclipse.core.runtime.Platform;

import com.tabnine.binary.exceptions.FailedToDownloadException;

public class GeneralDownloader {
	boolean download(String urlString, String destination, DownloadValidator validator) {
		Path tempDestination = Paths.get(format("%s.download.%s", destination, UUID.randomUUID()));

		try {
			if (!tempDestination.getParent().toFile().exists()) {
				if (!tempDestination.getParent().toFile().mkdirs()) {
					Platform.getLog(GeneralDownloader.class)
							.warn(format("Could not create the required directories for %s", tempDestination));
					return false;
				}
			}
			URLConnection connection = new URL(urlString).openConnection();
			connection.setConnectTimeout(REMOTE_CONNECTION_TIMEOUT);
			connection.setReadTimeout(BINARY_READ_TIMEOUT);
			Files.copy(connection.getInputStream(), tempDestination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Platform.getLog(GeneralDownloader.class).warn("Unexpected Exception", e);
			return false;
		}
		try {
			validator.validateAndRename(tempDestination, Paths.get(destination));
		} catch (FailedToDownloadException e) {
			Platform.getLog(GeneralDownloader.class).warn("Unexpected Exception", e);
			tempDestination.toFile().delete();
			return false;
		}

		return true;
	}
}
