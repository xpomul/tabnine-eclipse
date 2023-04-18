package com.tabnine.binary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.ServiceCaller;

import com.tabnine.Log;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.general.StaticConfig;

public class BinaryProcessGateway {
	private Process process = null;
	private BufferedReader reader = null;

	public void init(List<String> command) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		configureProxy(processBuilder);

		Log.debug(() -> "Starting tabnine binary :\n" + String.join("\n", command) + "\n\nEnvironment"
				+ processBuilder.environment().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
						.collect(Collectors.joining("\n")));

		Process createdProcess = processBuilder.start();
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

	private void configureProxy(ProcessBuilder processBuilder) {
		List<IProxyData> proxyData = new ArrayList<>();
		boolean proxiesEnabled[] = { false };
		boolean directAccess[] = { true };

		final URI serverUri;
		try {
			serverUri = new URI(StaticConfig.getBundleServerUrl());
		} catch (URISyntaxException e) {
			return;
		}

		ServiceCaller.callOnce(getClass(), IProxyService.class, s -> {
			if (s.isProxiesEnabled()) {
				proxiesEnabled[0] = true;
				proxyData.addAll(Arrays.asList(s.getProxyData()));
				IProxyData[] tabnineHostProxy = s.select(serverUri);
				if (tabnineHostProxy.length > 0) {
					directAccess[0] = false;
				}
			}
		});

		if (!proxiesEnabled[0]) {
			return;
		}

		if (directAccess[0]) {
			processBuilder.environment().put("NO_PROXY", serverUri.getHost());
			processBuilder.environment().put("no_proxy", serverUri.getHost());
		}

		for (IProxyData proxy : proxyData) {
			String proxyString = "";
			if (proxy.isRequiresAuthentication()) {
				String encodedPassword;
				try {
					encodedPassword = URLEncoder.encode(proxy.getPassword(), StandardCharsets.UTF_8.toString());
				} catch (UnsupportedEncodingException e) {
					Log.warning("Proxy Password cannot be encoded", e);
					encodedPassword = proxy.getPassword();
				}
				proxyString = proxy.getUserId() + ":" + encodedPassword + "@";
			}

			proxyString += proxy.getHost() + ":" + proxy.getPort();
			switch (proxy.getType()) {
			case IProxyData.HTTP_PROXY_TYPE:
				processBuilder.environment().put("HTTP_PROXY", "http://" + proxyString);
				processBuilder.environment().put("http_proxy", "http://" + proxyString);
				break;
			case IProxyData.HTTPS_PROXY_TYPE:
				processBuilder.environment().put("HTTPS_PROXY", "http://" + proxyString);
				processBuilder.environment().put("https_proxy", "http://" + proxyString);
				break;
			// currently no support for SOCKS proxy!
			}
		}
	}

}
