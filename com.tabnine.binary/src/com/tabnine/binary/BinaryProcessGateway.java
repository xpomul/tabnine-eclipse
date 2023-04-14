package com.tabnine.binary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.tabnine.binary.exceptions.TabNineDeadException;

public class BinaryProcessGateway {
	private Process process = null;
	private BufferedReader reader = null;

	public void init(List<String> command) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process createdProcess = processBuilder.start();
		// TODO Proxy Support
		process = createdProcess;
		reader = new BufferedReader(new InputStreamReader(createdProcess.getInputStream(), StandardCharsets.UTF_8));
	}

	public String readRawResponse() throws IOException, TabNineDeadException {
		String line = reader.readLine();
		if (line == null) {
			throw new TabNineDeadException("End of stream reached");
		}
		return line;
	}

	public void writeRequest(String request) throws IOException {
		process.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
		process.getOutputStream().flush();
	}

	public boolean isDead() {
		return process == null || !process.isAlive();
	}

	public void destroy() {
		process.destroy();
	}

	public Long pid() {
		return process != null ? process.pid() : null;
	}
}
