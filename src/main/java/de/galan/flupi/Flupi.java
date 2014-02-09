package de.galan.flupi;

import de.galan.commons.time.HumanTime;
import de.galan.flupi.FluentHttpClient.HttpBuilder;


/**
 * Static accessor to FluentHttpClient with option to set default configuration.
 * 
 * @author daniel
 */
public class Flupi {

	private static Long defaultTimeout;


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


	private static HttpBuilder defaults(HttpBuilder builder) {
		if (defaultTimeout != null) {
			builder.timeout(defaultTimeout);
		}
		return builder;
	}

}
