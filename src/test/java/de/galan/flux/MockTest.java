package de.galan.flux;

import static org.assertj.core.api.Assertions.*;

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
	public void testName() throws Exception {
		MockCommonHttpClient client = new MockCommonHttpClient();
		Flux.setHttpClientFactory(() -> client);
		MockResponse mockResponse = new MockResponse("hello", 200);
		client.setResponse(mockResponse);
		try (Response response = Flux.request("http://www.example.com/xyz").get()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
			assertThat(response.getStreamAsString()).isEqualTo("hello");
		}
	}

}
