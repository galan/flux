package de.galan.flupi;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.Charsets;

import de.galan.commons.time.HumanTime;
import de.galan.flupi.proxy.CommonProxy;


/**
 * Simplified fluent interface for HttpClient.
 * 
 * @author daniel
 */
public class FluentHttpClient {

	private HttpClient client;


	public FluentHttpClient() {
		// nada
	}


	public FluentHttpClient(HttpClient client) {
		setClient(client);
	}


	public void setClient(HttpClient client) {
		this.client = client;
	}


	HttpClient getClient(boolean create) {
		if (client == null && create) {
			client = new CommonHttpClient();
		}
		return client;
	}


	public HttpBuilder request(String protocol, String host, Integer port, String path) {
		String resource = new CommonHttpClient().constructResource(protocol, host, port, path);
		return request(resource);
	}


	public HttpBuilder request(String resource) {
		return new HttpBuilder(getClient(true), resource);
	}

	/** Builder */
	public static class HttpBuilder {

		String builderResource;
		byte[] builderBody;
		Long builderTimeout;
		CommonProxy builderProxy;
		String builderUsername;
		String builderPassword;
		boolean builderFollowRedirects = true;
		Long builderRetries;
		String builderRetriesTimebetween;
		boolean builderTimeoutThread = false;
		Map<String, String> builderHeader;
		Map<String, List<String>> builderParameter;
		private HttpClient builderClient;


		protected HttpBuilder(HttpClient client, String resource) {
			builderClient = client;
			builderResource = resource;
		}


		public HttpBuilder proxy(String proxy) {
			if (isNotBlank(proxy)) {
				builderProxy = CommonProxy.parse(proxy);
			}
			return this;
		}


		public HttpBuilder proxy(CommonProxy proxy) {
			if (proxy != null) {
				builderProxy = proxy;
			}
			return this;
		}


		public HttpBuilder proxy(String ip, int port) {
			if (isNotBlank(ip)) {
				builderProxy = new CommonProxy(ip, port);
			}
			return this;
		}


		/** Has to be called after setting a proxy via ip/port */
		public HttpBuilder proxyAuthentication(String username, String password) {
			builderProxy = new CommonProxy(builderProxy.getIp(), builderProxy.getPort(), username, password);
			return this;
		}


		public HttpBuilder authentication(String username, String password) {
			builderUsername = username;
			builderPassword = password;
			return this;
		}


		public HttpBuilder timeout(String timeout) {
			return timeout(HumanTime.dehumanizeTime(timeout).intValue());
		}


		public HttpBuilder timeout(Integer timeout) {
			return timeout((timeout == null) ? null : timeout.longValue());
		}


		public HttpBuilder timeout(Long timeout) {
			builderTimeout = timeout;
			return this;
		}


		public HttpBuilder followRedirects() {
			builderFollowRedirects = true;
			return this;
		}


		public HttpBuilder unfollowRedirects() {
			builderFollowRedirects = false;
			return this;
		}


		public HttpBuilder retries(Long retries, String timeBetween) {
			builderRetries = retries;
			builderRetriesTimebetween = timeBetween;
			return this;
		}


		public HttpBuilder timeoutThread() {
			builderTimeoutThread = true;
			return this;
		}


		public HttpBuilder body(String body) {
			builderBody = (body == null) ? null : body.getBytes(Charsets.UTF_8);
			return this;
		}


		public HttpBuilder body(byte[] body) {
			builderBody = body;
			return this;
		}


		public HttpBuilder header(String key, String value) {
			if (builderHeader == null) {
				builderHeader = new HashMap<String, String>();
			}
			builderHeader.put(key, value);
			return this;
		}


		public HttpBuilder headers(Map<String, String> headers) {
			if (builderHeader == null) {
				builderHeader = new HashMap<String, String>();
			}
			builderHeader.putAll(headers);
			return this;
		}


		public HttpBuilder parameter(String key, String... values) {
			List<String> list = getParameterList(key);
			for (String value: values) {
				list.add(value);
			}
			return this;
		}


		public HttpBuilder parameterMap(Map<String, String> parameters) {
			if (parameters != null) {
				for (Entry<String, String> entry: parameters.entrySet()) {
					getParameterList(entry.getKey()).add(entry.getValue());
				}
			}
			return this;
		}


		public HttpBuilder parameterList(Map<String, List<String>> parameters) {
			if (parameters != null) {
				for (Entry<String, List<String>> entry: parameters.entrySet()) {
					List<String> list = getParameterList(entry.getKey());
					for (String value: entry.getValue()) {
						list.add(value);
					}
				}
			}
			return this;
		}


		private List<String> getParameterList(String key) {
			if (builderParameter == null) {
				builderParameter = new HashMap<>();
			}
			List<String> values = builderParameter.get(key);
			if (values == null) {
				values = new ArrayList<>();
				builderParameter.put(key, values);
			}
			return values;
		}


		public Response get() throws HttpClientException {
			return method(Method.GET);
		}


		public Response put() throws HttpClientException {
			return method(Method.PUT);
		}


		public Response post() throws HttpClientException {
			return method(Method.POST);
		}


		public Response delete() throws HttpClientException {
			return method(Method.DELETE);
		}


		public Response head() throws HttpClientException {
			return method(Method.HEAD);
		}


		public Response trace() throws HttpClientException {
			return method(Method.TRACE);
		}


		public Response options() throws HttpClientException {
			return method(Method.OPTIONS);
		}


		public Response method(Method method) throws HttpClientException {
			HttpClient client = (builderClient == null) ? new CommonHttpClient() : builderClient;
			HttpOptions options = new HttpOptions();
			options.setTimeout(builderTimeout);
			options.enableAuthorization(builderUsername, builderPassword);
			options.enableProxy(builderProxy);
			options.enableRetries(builderRetries, builderRetriesTimebetween);
			options.enableFollowRedirects(builderFollowRedirects);
			options.enableTimeoutThread(builderTimeoutThread);
			return client.request(builderResource, method, builderHeader, builderParameter, builderBody, options);
		}
	}

}
