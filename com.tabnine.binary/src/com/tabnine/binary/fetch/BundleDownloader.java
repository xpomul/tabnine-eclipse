package com.tabnine.binary.fetch;

import static com.tabnine.general.StaticConfig.TARGET_NAME;
import static com.tabnine.general.StaticConfig.bundleFullPath;
import static com.tabnine.general.StaticConfig.getBundleServerUrl;
import static com.tabnine.general.StaticConfig.versionFullPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.tabnine.Log;

public class BundleDownloader {

	private final TempBundleValidator validator;
	private final GeneralDownloader downloader;

	public BundleDownloader(TempBundleValidator validator, GeneralDownloader downloader) {
		this.validator = validator;
		this.downloader = downloader;
	}

	public Optional<BinaryVersion> downloadAndExtractBundle(String version) {
		String bundlesServerUrl = getBundleServerUrl();
		String urlString = String.join("/", bundlesServerUrl, version, TARGET_NAME, "TabNine.zip");
		String destination = bundleFullPath(version);
		if (this.downloader.download(urlString, destination, validator)) {
			try {
				unzipFile(destination);
				java.nio.file.Paths.get(destination).toFile().delete();
				return Optional.of(new BinaryVersion(versionFullPath(version), version));
			} catch (IOException e) {
				Log.warning("error unzipping file", e);
			}
		}
		return Optional.empty();
	}

	private void unzipFile(String fileZip) throws IOException {
		File destDir = new File(fileZip).getParentFile();
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = newFile(destDir, zipEntry);
			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			zipEntry = zis.getNextEntry();
			newFile.setExecutable(true);
		}
		zis.closeEntry();
		zis.close();
	}

	private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}
}
