package com.tabnine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.ServiceCaller;

import com.tabnine.general.StaticConfig;

public class ProxyUtil {

	@FunctionalInterface
	public static interface ThrowingCallable<T> {
		T call(URLConnection c) throws IOException;
	}

	public static <T> T runWithUrlConnection(String urlString, ThrowingCallable<T> callable) throws IOException {
		URI uriObject;
		try {
			uriObject = new URI(urlString);
		} catch (URISyntaxException e) {
			Log.error("Not a valid URI: " + urlString, e);
			return null;
		}

		List<IProxyData> proxyData = new ArrayList<>();
		ServiceCaller.callOnce(ProxyUtil.class, IProxyService.class,
				proxyService -> proxyData.addAll(Arrays.asList(proxyService.select(uriObject))));

		for (IProxyData data : proxyData) {
			var proxy = new Proxy(Type.HTTP, new InetSocketAddress(data.getHost(), data.getPort()));

			URLConnection connection = uriObject.toURL().openConnection(proxy);

			connection.setConnectTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);
			connection.setReadTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);
			if (data.isRequiresAuthentication()) {
				String authString = data.getUserId() + ":" + data.getPassword();
				String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());
				connection.setRequestProperty("Proxy-Authorization", "Basic " + encodedAuthString);
			}

			try {
				return callable.call(connection);
			} catch (IOException e) {
				Log.warning("Error accessing " + urlString + " via proxy " + data.getHost());
			}
		}

		URLConnection connection = uriObject.toURL().openConnection();

		connection.setConnectTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);
		connection.setReadTimeout(StaticConfig.REMOTE_CONNECTION_TIMEOUT);

		return callable.call(connection);
	}
}
