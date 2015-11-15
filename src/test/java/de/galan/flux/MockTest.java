package de.galan.flux;

import static org.assertj.core.api.StrictAssertions.*;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import de.galan.commons.test.AbstractTestParent;
import de.galan.flux.MockCommonHttpClient.MockResponse;


/**
 * Demonstrating the usage of the MockCommonHttpClient.
 *
 * @author daniel
 */
public class MockTest extends AbstractTestParent {

	@Test
	public void singleResponse() throws Exception {
		MockCommonHttpClient client = new MockCommonHttpClient();
		Flux.setHttpClientFactory(() -> client);
		MockResponse mockResponse = new MockResponse("hello", 200);
		client.setResponse(mockResponse);
		assertGet("hello");
	}


	@Test
	public void multipleFiniteResponses() throws Exception {
		MockCommonHttpClient client = new MockCommonHttpClient();
		Flux.setHttpClientFactory(() -> client);
		client.setResponses(false, new MockResponse("hello 1", 200), new MockResponse("hello 2", 200));
		assertGet("hello 1");
		assertGet("hello 2");
	}


	@Test
	public void multipleInfiniteResponses() throws Exception {
		MockCommonHttpClient client = new MockCommonHttpClient();
		Flux.setHttpClientFactory(() -> client);
		client.setResponses(true, new MockResponse("hello", 200));
		assertGet("hello");
		assertGet("hello");
	}


	protected void assertGet(String expectedBody) throws HttpClientException, IOException {
		try (Response response = Flux.request("http://www.example.com/xyz").get()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
			assertThat(response.getStreamAsString()).isEqualTo(expectedBody);
		}
	}


	@Test
	public void mockResponsesInfinite() throws Exception {
		MockResponse resp = new MockResponse("aaa", 200);
		resp.convertToReplayableStream();
		assertThat(resp.getStreamAsString()).isEqualTo("aaa");
		resp.close();
		assertThat(resp.getStreamAsString()).isEqualTo("aaa");
		resp.close();
		assertThat(resp.getStreamAsString()).isEqualTo("aaa");
		resp.close();
		assertThat(IOUtils.toString(resp.getStream())).isEqualTo("aaa");
		resp.close();
		assertThat(IOUtils.toString(resp.getStream())).isEqualTo("aaa");
		resp.close();
		assertThat(IOUtils.toString(resp.getStream())).isEqualTo("aaa");
		resp.close();
	}


	@Test
	public void mockResponsesFinite() throws Exception {
		MockResponse resp = new MockResponse("aaa", 200);
		assertThat(resp.getStreamAsString()).isEqualTo("aaa");
		resp.close();
		assertThat(resp.getStreamAsString()).isEmpty();
	}

}
