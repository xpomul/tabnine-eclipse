package com.tabnine.binary;

import static com.tabnine.general.StaticConfig.exponentialBackoff;
import static com.tabnine.general.Utils.executeThread;

import java.util.Collections;
import java.util.concurrent.Future;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.tabnine.Log;
import com.tabnine.binary.exceptions.BinaryRequestTimeoutException;

public class BinaryProcessRequesterProvider {
	private final BinaryRun binaryRun;
	private final BinaryProcessGatewayProvider binaryProcessGatewayProvider;
	private final int timeoutsThresholdMillis;
	private Long firstTimeoutTimestamp = null;
	private BinaryProcessRequester binaryProcessRequester;
	private Future<?> binaryInit;
	private long lastRestartTimestamp = 0;
	private int restartAttemptCounter = 0;

	private BinaryProcessRequesterProvider(BinaryRun binaryRun,
			BinaryProcessGatewayProvider binaryProcessGatewayProvider, int timeoutsThresholdMillis) {
		this.binaryRun = binaryRun;
		this.binaryProcessGatewayProvider = binaryProcessGatewayProvider;
		this.timeoutsThresholdMillis = timeoutsThresholdMillis;
	}

	public static BinaryProcessRequesterProvider create(BinaryRun binaryRun,
			BinaryProcessGatewayProvider binaryProcessGatewayProvider, int timeoutsThreshold) {
		BinaryProcessRequesterProvider binaryProcessRequesterProvider = new BinaryProcessRequesterProvider(binaryRun,
				binaryProcessGatewayProvider, timeoutsThreshold);

		binaryProcessRequesterProvider.createNew();

		return binaryProcessRequesterProvider;
	}

	public BinaryProcessRequester get() {
		if (isStarting()) {
			Log.info("Can't get completions because Tabnine process is not started yet.");

			return VoidBinaryProcessRequester.instance();
		}

		return binaryProcessRequester;
	}

	public void onSuccessfulRequest() {
		firstTimeoutTimestamp = null;
		restartAttemptCounter = 0;
	}

	public synchronized void onDead(Throwable e) {
		long elapsedSinceLastRestart = System.currentTimeMillis() - lastRestartTimestamp;
		if (isStarting() || elapsedSinceLastRestart < exponentialBackoff(restartAttemptCounter)) {
			return;
		}

		firstTimeoutTimestamp = null;
		restartAttemptCounter++;
		lastRestartTimestamp = System.currentTimeMillis();

		Log.warning("Tabnine is in invalid state, it is being restarted.",
				new RuntimeException("restartAttemptCounter: " + restartAttemptCounter + "\n" + e.getMessage()));

		createNew();
	}

	public void onTimeout() {
		Log.info("TabNine's response timed out.");

		long now = System.currentTimeMillis();
		if (firstTimeoutTimestamp == null) {
			firstTimeoutTimestamp = now;
		}

		if (now - firstTimeoutTimestamp >= timeoutsThresholdMillis) {
			onDead(new BinaryRequestTimeoutException());
		}
	}

	private boolean isStarting() {
		return this.binaryInit == null || !this.binaryInit.isDone();
	}

	private void createNew() {
		if (binaryProcessRequester != null) {
			binaryProcessRequester.destroy();
		}
		BinaryProcessGateway binaryProcessGateway = binaryProcessGatewayProvider.generateBinaryProcessGateway();

		initProcess(binaryProcessGateway);

		this.binaryProcessRequester = new BinaryProcessRequesterImpl(new ParsedBinaryIO(
				new GsonBuilder().registerTypeAdapter(Double.class, doubleOrIntSerializer()).create(),
				binaryProcessGateway));
	}

	private static JsonSerializer<Double> doubleOrIntSerializer() {
		return (src, type, jsonSerializationContext) -> {
			if (src == src.longValue()) {
				return new JsonPrimitive(src.longValue());
			}
			return new JsonPrimitive(src);
		};
	}

	private synchronized void initProcess(BinaryProcessGateway binaryProcessGateway) {
		if (binaryInit != null)
			binaryInit.cancel(false);

		binaryInit = executeThread(() -> {
			try {
				binaryProcessGateway.init(binaryRun
						.generateRunCommand(Collections.singletonMap("ide-restart-counter", restartAttemptCounter)));
			} catch (Exception e) {
				Log.warning("Error starting TabNine.", e);
			}
		});
	}
}
