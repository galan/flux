package de.galan.flupi;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.HashMap;
import java.util.Map;

import de.galan.commons.time.HumanTime;
import de.galan.flupi.FluentHttpClient.HttpBuilder;


/**
 * Static accessor to FluentHttpClient with option to set default configuration.
 * 
 * @author daniel
 */
public class Flupi {

	private static Long defaultTimeout;
	private static Map<String, String> defaultHeader;


	/**
	 * If no timeout is passed via the builder, a default timeout can be set with this method for further requests.
	 * 
	 * @param timeout Timeout in milliseconds
	 */
	public static void setDefaultTimeout(long timeout) {
		defaultTimeout = timeout;
	}


	/**
	 * If no timeout is passed via the builder, a default timeout can be set with this method for further requests.
	 * 
	 * @param timeout Timeout in human time.
	 */
	public static void setDefaultTimeout(String timeout) {
		defaultTimeout = HumanTime.dehumanizeTime(timeout);
	}


	/**
	 * Added default headers will always be send, can still be overriden using the builder.
	 * 
	 * @param key
	 * @param value
	 */
	public static void addDefaultHeader(String key, String value) {
		if (isNotBlank(key) && isNotBlank(value)) {
			if (defaultHeader == null) {
				defaultHeader = new HashMap<String, String>();
			}
			defaultHeader.put(key, value);
		}
	}


	/**
	 * Specifies the resource to be requested.
	 * 
	 * @param resource The URL to request, including protocol.
	 * @return The HttpBuilder
	 */
	public static HttpBuilder request(String resource) {
		return defaults(new FluentHttpClient().request(resource));
	}


	/**
	 * Specifies the resource to be requested.
	 * 
	 * @param protocol Protocol of the resource
	 * @param host Host of the resource
	 * @param port Port the host is listening on
	 * @param path Path for the resource
	 * @return The HttpBuilder
	 */
	public static HttpBuilder request(String protocol, String host, Integer port, String path) {
		return defaults(new FluentHttpClient().request(protocol, host, port, path));
	}


	/** Setting the static defaults before returning the builder */
	private static HttpBuilder defaults(HttpBuilder builder) {
		if (defaultTimeout != null) {
			builder.timeout(defaultTimeout);
		}
		if (defaultHeader != null) {
			builder.headers(defaultHeader);
		}
		return builder;
	}

}
