package com.tabnine.general;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

public final class Utils {
	public static String readContent(InputStream inputStream) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;

		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}

		return result.toString(StandardCharsets.UTF_8.name()).trim();
	}

	public static String cmdSanitize(String text) {
		return text.replace(" ", "");
	}

	public static Future<?> executeThread(Runnable runnable) {
		return CommonExecutorServices.getAppExecutorService().submit(runnable);
	}
}
