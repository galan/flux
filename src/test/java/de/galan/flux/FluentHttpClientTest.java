package de.galan.flux;

import static org.assertj.core.api.Assertions.*;

import java.io.PrintStream;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.simpleframework.http.Request;

import com.google.common.base.Stopwatch;

import de.galan.commons.test.DummyContainer;
import de.galan.commons.test.SimpleWebserverTestParent;
import de.galan.commons.test.Tests;
import de.galan.commons.time.Sleeper;


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
			assertThat(response.getStatusCode()).isEqualTo(200);
			assertThat(response.getStreamAsString()).isEqualTo("world");
		}
	}


	@Test
	public void asyncGet1() throws Exception {
		startServer(new DummyContainer() {

			@Override
			public void serve(Request req, org.simpleframework.http.Response resp) throws Exception {
				PrintStream body = resp.getPrintStream();
				body.print("world");
				Sleeper.sleep("2s");
				resp.setCode(200);
			}

		});

		Future<Response> future = new FluentHttpClient().request(new URL("http://localhost:12345")).getAsync();

		try {
			future.get(1000L, TimeUnit.MILLISECONDS);
			fail("throws exception");
		}
		catch (TimeoutException tex) {
			// expected
		}
		Response response = future.get(2200L, TimeUnit.MILLISECONDS);
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(200);
		assertThat(response.getStreamAsString()).isEqualTo("world");
		response.close();
	}


	@Test
	public void asyncGet2() throws Exception {
		startServer(new DummyContainer() {

			@Override
			public void serve(Request req, org.simpleframework.http.Response resp) throws Exception {
				PrintStream body = resp.getPrintStream();
				body.print("world");
				Sleeper.sleep("2s");
				resp.setCode(200);
			}

		});

		Stopwatch watch = Stopwatch.createStarted();
		Future<Response> future = new FluentHttpClient().request(new URL("http://localhost:12345")).getAsync();
		Response response = future.get();
		long timeTaken = watch.stop().elapsed(TimeUnit.MILLISECONDS);
		Tests.assertBetween(1999L, 2100L, timeTaken);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(200);
		assertThat(response.getStreamAsString()).isEqualTo("world");
		response.close();
	}

}
