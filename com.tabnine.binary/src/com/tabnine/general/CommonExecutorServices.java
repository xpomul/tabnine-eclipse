package com.tabnine.general;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommonExecutorServices {

	private static final ExecutorService SIMPLE_EXECUTOR = Executors.newSingleThreadExecutor();

	public static ExecutorService getAppExecutorService() {
		return SIMPLE_EXECUTOR;
	}

}
