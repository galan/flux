package de.galan.flux;

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.simpleframework.http.Request;

import de.galan.commons.logging.Say;
import de.galan.commons.test.DummyContainer;
import de.galan.commons.test.SimpleWebserverTestParent;


/**
 * CUT CommonHttpClientTest.
 *
 * @author daniel
 */
public class CommonHttpClientTest extends SimpleWebserverTestParent {

	@Test
	public void body() throws Exception {
		startServer(new DummyContainer() {

			@Override
			public void serve(Request req, org.simpleframework.http.Response resp) throws Exception {
				PrintStream body = resp.getPrintStream();
				body.print("world");
				resp.setCode(200);
			}

		});

		CommonHttpClient client = new CommonHttpClient();
		try (Response response = client.request("http://localhost:12345")) {
			assertEquals(200, response.getStatusCode());
			assertEquals("world", response.getStreamAsString());
		}
	}


	@Test
	public void statusCode() throws Exception {
		startServer(new DummyContainer() {

			@Override
			public void serve(Request req, org.simpleframework.http.Response resp) throws Exception {
				resp.setCode(404);
			}
		});

		CommonHttpClient client = new CommonHttpClient();
		try (Response response = client.request("http://localhost:12345")) {
			assertEquals(404, response.getStatusCode());
		}
	}


	@Test
	public void header() throws Exception {
		startServer(new DummyContainer() {

			@Override
			public void serve(Request req, org.simpleframework.http.Response resp) throws Exception {
				assertEquals("text/html; charset=UTF-8", req.getValue("Content-Type"));
			}
		});

		CommonHttpClient client = new CommonHttpClient();
		Map<String, String> header = new HashMap<String, String>();
		header.put("Content-Type", "text/html; charset=UTF-8");
		try (Response response = client.request("http://localhost:12345", Method.GET, header, null, null, null)) {
			assertEquals(200, response.getStatusCode());
		}
	}


	@Test
	public void resourceOnly() throws HttpClientException {
		startServer(new DummyContainer() {

			@Override
			public void serve(Request req, org.simpleframework.http.Response resp) throws Exception {
				assertEquals("value", req.getParameter("key"));
			}
		});

		CommonHttpClient client = new CommonHttpClient();
		try (Response response = client.request("http://localhost:12345?key=value")) {
			assertEquals(200, response.getStatusCode());
		}
	}


	@Test
	public void retryOnce() throws Exception {
		startServerDelayed("300ms");
		HttpClient client = new CommonHttpClient();
		HttpOptions options = new HttpOptions();
		options.enableRetries(1L, "400ms");
		try (Response response = client.request("http://localhost:12345", Method.GET, null, null, null, options)) {
			assertEquals(200, response.getStatusCode());
			assertEquals("world", response.getStreamAsString());
		}
	}


	@Test
	public void retryTwice() throws Exception {
		startServerDelayed("500ms");
		CommonHttpClient client = new CommonHttpClient();
		HttpOptions options = new HttpOptions();
		options.enableRetries(2L, "300ms");
		try (Response response = client.request("http://localhost:12345", Method.GET, null, null, null, options)) {
			assertEquals(200, response.getStatusCode());
			assertEquals("world", response.getStreamAsString());
		}
	}


	@Test(expected = HttpClientException.class)
	public void noRetry() throws Exception {
		startServerDelayed("300s");
		CommonHttpClient client = new CommonHttpClient();
		client.request("http://localhost:12345");
		fail();
	}


	private void startServerDelayed(final String delay) {
		startServerDelayed(new DummyContainer() {

			@Override
			public void serve(Request req, org.simpleframework.http.Response resp) throws Exception {
				PrintStream body = resp.getPrintStream();
				resp.setCode(200);
				body.print("world");
			}
		}, delay);
	}


	@Test
	public void bodyError() throws Exception {
		startServer(new DummyContainer() {

			@Override
			public void serve(Request req, org.simpleframework.http.Response resp) throws Exception {
				PrintStream body = resp.getPrintStream();
				resp.setCode(404);
				body.print("world");
				Say.info(resp);
			}

		});

		CommonHttpClient client = new CommonHttpClient();
		try (Response response = client.request("http://localhost:12345")) {
			assertEquals(404, response.getStatusCode());
			assertEquals("world", response.getStreamAsString());
		}
	}

}
