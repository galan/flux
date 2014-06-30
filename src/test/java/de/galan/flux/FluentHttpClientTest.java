package de.galan.flux;

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.net.URL;

import org.junit.Test;
import org.simpleframework.http.Request;

import de.galan.commons.test.DummyContainer;
import de.galan.commons.test.SimpleWebserverTestParent;


/**
 * CUT FLuentHttpClientTest
 *
 * @author daniel
 */
public class FluentHttpClientTest extends SimpleWebserverTestParent {

	@Test
	public void bodyUrl() throws Exception {
		startServer(new DummyContainer() {

			@Override
			public void serve(Request req, org.simpleframework.http.Response resp) throws Exception {
				PrintStream body = resp.getPrintStream();
				body.print("world");
				resp.setCode(200);
			}

		});

		try (Response response = new FluentHttpClient().request(new URL("http://localhost:12345")).get()) {
			assertEquals(200, response.getStatusCode());
			assertEquals("world", response.getStreamAsString());
		}
	}

}
