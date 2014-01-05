package de.galan.flupi;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import de.galan.commons.logging.Logr;
import de.galan.commons.net.UrlUtil;
import de.galan.commons.time.Sleeper;


/**
 * Provides a simple HTTP client for most use-cases, reusable.
 * 
 * @author daniel
 */
public class CommonHttpClient implements HttpClient {

	private static final Logger LOG = Logr.get();


	public CommonHttpClient() {
		// nada
	}


	String appendParameters(String resource, Map<String, List<String>> parameters) {
		StringBuilder builder = new StringBuilder(resource);
		if (parameters != null && !parameters.isEmpty()) {
			boolean first = true;
			for (Entry<String, List<String>> entry: parameters.entrySet()) {
				for (String value: entry.getValue()) {
					if (entry.getValue() != null) {
						if (first) {
							builder.append(contains(resource, "?") ? "&" : "?");
							first = false;
						}
						else {
							builder.append("&");
						}
						builder.append(entry.getKey());
						if (value != null) {
							builder.append("=");
							builder.append(UrlUtil.encode(value.toString(), Charsets.UTF_8));
						}
					}
				}
			}
		}
		return builder.toString();
	}


	String constructResource(String protocol, String host, Integer port, String path) {
		StringBuilder builder = new StringBuilder();
		builder.append(defaultIfBlank(protocol, "http"));
		builder.append("://");
		builder.append(host);
		if ((port != null) && (port > 0)) {
			builder.append(":");
			builder.append(port);
		}
		builder.append(startsWith(path, "/") ? path : "/" + path);
		return builder.toString();
	}


	public Response request(String resource) throws HttpClientException {
		return request(resource, null, null, null, null);
	}


	public Response request(String protocol, String host, Integer port, String path, Method method, Map<String, String> extraHeader, Map<String, List<String>> parameters, byte[] body) throws HttpClientException {
		String buildUrl = constructResource(protocol, host, port, path);
		return request(buildUrl, method, extraHeader, parameters, body);
	}


	@Override
	public Response request(String resource, Method method, Map<String, String> extraHeader, Map<String, List<String>> parameters, byte[] body) throws HttpClientException {
		return request(resource, method, extraHeader, parameters, body, null);
	}


	@Override
	public Response request(String resource, Method method, Map<String, String> extraHeader, Map<String, List<String>> parameters, byte[] body, HttpOptions options) throws HttpClientException {
		HttpOptions opts = (options != null) ? options : new HttpOptions();
		URL url = null;
		try {
			url = new URL(appendParameters(resource, parameters));
		}
		catch (MalformedURLException muex) {
			throw new HttpClientException("URL invalid", muex);
		}

		Response response = null;
		for (int i = 0; i <= opts.getRetriesCount(); i++) {
			try {
				response = request(method, extraHeader, body, url, opts);
				break;
			}
			catch (HttpClientException ex) {
				if (i < opts.getRetriesCount()) {
					LOG.info("Unable to connect to {resource}, retrying", resource);
					Sleeper.sleep(opts.getTimeBetweenRetries());
				}
				else {
					throw ex;
				}
			}
		}
		return response;
	}


	protected Response request(Method method, Map<String, String> extraHeader, byte[] body, URL url, HttpOptions options) throws HttpClientException {
		Method scopeMethod = method;
		Map<String, String> header = extraHeader;
		int statusCode = 0;
		String contentEncoding = null;
		String contentType = null;
		InputStream is = null;
		Map<String, String> headerFields = new HashMap<String, String>();

		// Bug, HttpURLConnection does not allow DELETE with body
		if (body != null && Method.DELETE.equals(scopeMethod)) {
			scopeMethod = Method.POST;
			if (header == null) {
				header = new HashMap<String, String>();
			}
			header.put("X-HTTP-Method-Override", Method.DELETE.name());
		}
		LOG.debug("Requesting url {}: {} - headers {} - body {}", method, url, header, (body != null && body.length > 0));
		HttpURLConnection connection = openConnection(url, options);
		initConnection(header, connection, options);
		try {
			connection.setRequestMethod((scopeMethod == null) ? Method.GET.name() : scopeMethod.name());
			if (body != null) {
				IOUtils.write(body, connection.getOutputStream());
			}
			// Retrieve response
			statusCode = connection.getResponseCode();
			contentEncoding = connection.getContentEncoding();
			contentType = connection.getContentType();
			for (String key: connection.getHeaderFields().keySet()) {
				headerFields.put(key, connection.getHeaderField(key));
			}

			try {
				is = connection.getInputStream();
			}
			catch (Exception ex) {
				// check response-statusCode instead
			}
		}
		catch (Exception ex) {
			throw new HttpClientException("Data could not be queried (" + ex.getClass().getSimpleName() + ": " + ex.getMessage() + ")", ex);
		}

		return new Response(connection, is, statusCode, contentEncoding, contentType, headerFields);
	}


	private void initConnection(Map<String, String> extraHeader, HttpURLConnection connection, HttpOptions options) {
		putAuthorization(connection, "Authorization", options.getAuthorizationUsername(), options.getAuthorizationPassword());
		connection.setConnectTimeout(options.getTimeout().intValue());
		connection.setReadTimeout(options.getTimeout().intValue());
		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(options.getFollowRedirects());
		//extraHeader
		if (extraHeader != null) {
			for (String key: extraHeader.keySet()) {
				connection.setRequestProperty(key, extraHeader.get(key));
			}
		}

		if (options.getTimeoutThread()) {
			startTimeoutThread(connection, options);
		}
	}


	private HttpURLConnection openConnection(URL url, HttpOptions options) throws HttpClientException {
		HttpURLConnection connection = null;
		try {
			if (options.isProxyEnabled()) {
				Proxy javaProxy = new Proxy(Type.HTTP, new InetSocketAddress(options.getProxy().getIp(), options.getProxy().getPort()));
				connection = (HttpURLConnection)url.openConnection(javaProxy);
				if (options.getProxy().hasAuthentication()) {
					if (HttpsURLConnection.class.isAssignableFrom(connection.getClass())) {
						Authenticator.setDefault(createAuthenticator(options));
					}
					else {
						putAuthorization(connection, "Proxy-Authorization", options.getProxy().getUsername(), options.getProxy().getPassword());
					}
				}
			}
			else {
				connection = (HttpURLConnection)url.openConnection();
			}
		}
		catch (IOException ex) {
			throw new HttpClientException("connection could not be opened", ex);
		}
		return connection;
	}


	protected Authenticator createAuthenticator(final HttpOptions options) {
		return new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				if (equalsIgnoreCase(getRequestingScheme(), "basic") && equalsIgnoreCase(getRequestingURL().getProtocol(), "https")
						&& getRequestorType().equals(RequestorType.PROXY)) {
					return new PasswordAuthentication(options.getProxy().getUsername(), options.getProxy().getPassword().toCharArray());
				}
				return super.getPasswordAuthentication();
			}
		};
	}


	protected void putAuthorization(URLConnection connection, String header, String username, String password) {
		if (isNotBlank(username) && isNotBlank(password)) {
			String pair = username + ":" + password;
			String encodedAuthorization = trim(Base64.encodeBase64String(pair.getBytes(Charsets.UTF_8)));
			connection.setRequestProperty(header, "Basic " + encodedAuthorization);
		}
	}


	protected void startTimeoutThread(HttpURLConnection connection, HttpOptions options) {
		TimeoutThread tt = new TimeoutThread(connection, options.getTimeout().intValue());
		tt.start();
	}

	/**
	 * Some sites may have timeouts, but the connection timeout does not seem to work reliably. Therefore this Thread
	 * can be used to force quiting the blocking thread. (The initial problematic site was http://www.qionghaifc.com/)
	 */
	public static class TimeoutThread extends Thread {

		private final HttpURLConnection connection;
		private final Integer timeout;


		public TimeoutThread(HttpURLConnection connection, Integer timeout) {
			this.connection = connection;
			this.timeout = timeout;
		}


		@Override
		public void run() {
			Sleeper.sleep(timeout + 1000);
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

}
