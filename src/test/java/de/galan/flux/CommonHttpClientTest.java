package de.galan.flux;

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.simpleframework.http.Request;

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
				body.print("world");
				resp.setCode(200);
			}
		}, delay);
	}


	@Test
	public void resourceConstruction() {
		CommonHttpClient client = new CommonHttpClient();
		assertEquals("ftp://horst:666/starts/with/slash", client.constructResource("ftp", "horst", 666, "/starts/with/slash"));
		assertEquals("ftp://horst:23/starts/with/noslash", client.constructResource("ftp", "horst", 23, "starts/with/noslash"));
		assertEquals("http://der.horst.de:177/somewhere", client.constructResource("http", "der.horst.de", 177, "somewhere"));
		assertEquals("http://der.horst.de/somewhere", client.constructResource("http", "der.horst.de", -1, "somewhere"));
		assertEquals("http://der.horst.de/somewhere", client.constructResource("http", "der.horst.de", 0, "somewhere"));
		assertEquals("http://der.horst.de/somewhere", client.constructResource("http", "der.horst.de", null, "somewhere"));
	}


	@Test
	public void parameterAppend() throws Exception {
		CommonHttpClient client = new CommonHttpClient();
		String resource = client.constructResource("http", "der.horst.de", 177, "somewhere");
		Map<String, List<String>> parameter = new HashMap<>();
		assertEquals("http://der.horst.de:177/somewhere", client.appendParameters(resource, parameter));
		parameter.put("k1", Arrays.asList("v1"));
		assertEquals("http://der.horst.de:177/somewhere?k1=v1", client.appendParameters(resource, parameter));
		parameter.put("k2", Arrays.asList("v2"));
		assertEquals("http://der.horst.de:177/somewhere?k1=v1&k2=v2", client.appendParameters(resource, parameter));
		assertEquals("http://der.horst.de:177/somewhere?x=y&k1=v1&k2=v2", client.appendParameters(resource + "?x=y", parameter));
	}

}
