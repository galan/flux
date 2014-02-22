package de.galan.flux;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;


/**
 * Test helper for collecting requests and responding to http clients.
 * 
 * @author daniel
 */
public class MockCommonHttpClient implements HttpClient {

	private Iterator<Response> responses;
	private List<Request> requests = new ArrayList<Request>();


	public void setResponse(Response response) {
		setResponses(true, response);
	}


	public void setResponses(boolean repeat, Response... response) {
		responses = repeat ? Iterators.cycle(response) : Iterators.forArray(response);
	}


	public List<Request> getRequests() {
		return requests;
	}


	public void reset() {
		requests.clear();
	}


	protected Response getNextResponse() {
		if (responses == null || !responses.hasNext()) {
			return null;
		}
		return responses.next();
	}


	@Override
	public Response request(String resource, Method method, Map<String, String> extraHeader, Map<String, List<String>> parameters, byte[] body) throws HttpClientException {
		return request(resource, method, extraHeader, parameters, body, null);
	}


	@Override
	public Response request(String resource, Method method, Map<String, String> extraHeader, Map<String, List<String>> parameters, byte[] body, HttpOptions options) throws HttpClientException {
		requests.add(new Request(method, extraHeader, body, resource));
		// add request to list
		return getNextResponse();
	}

	/** Collected request. */
	public static class Request {

		public Method method;
		public Map<String, String> extraHeader;
		public byte[] body;
		public String resource;


		public Request(Method method, Map<String, String> extraHeader, byte[] body, String resource) {
			super();
			this.method = method;
			this.extraHeader = extraHeader;
			this.body = body;
			this.resource = resource;
		}


		public String getBody() {
			return new String(body, Charsets.UTF_8);
		}

	}

	/** Mocks the body and metadata of a http response. */
	public static class MockResponse extends Response {

		public MockResponse(String body, int statusCode) {
			this(body, statusCode, "text/html;charset=UTF-8");
		}


		public MockResponse(byte[] body, int statusCode, String contentType) {
			super(null, new ByteArrayInputStream(body), statusCode, null, contentType, null);
		}


		public MockResponse(String body, int statusCode, String contentType) {
			super(null, new ByteArrayInputStream(body.getBytes(Charsets.UTF_8)), statusCode, Charsets.UTF_8.toString(), contentType, null);
		}


		public MockResponse(HttpURLConnection connection, InputStream dataStream, int statusCode, String contentEncoding, String contentType, Map<String, String> headerFields) {
			super(connection, dataStream, statusCode, contentEncoding, contentType, headerFields);
		}

	}

}
