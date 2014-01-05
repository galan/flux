package de.galan.flupi;

/**
 * Exception thrown while executing a remote http call.
 * 
 * @author daniel
 */
public class HttpClientException extends Exception {

	public HttpClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
