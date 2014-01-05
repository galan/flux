package de.galan.flupi;

import de.galan.commons.time.HumanTime;
import de.galan.flupi.FLuentHttpClient.HttpBuilder;


/**
 * Static accessor to FluentHttpClient with option to set default configuration.
 * 
 * @author daniel
 */
public class Flupi {

	private static Long defaultTimeout;


	public static void setDefaultTimeout(long timeout) {
		defaultTimeout = timeout;
	}


	public static void setDefaultTimeout(String timeout) {
		defaultTimeout = HumanTime.dehumanizeTime(timeout);
	}


	public static HttpBuilder request(String resource) {
		return defaults(new FLuentHttpClient().request(resource));
	}


	public static HttpBuilder request(String protocol, String host, Integer port, String path) {
		return defaults(new FLuentHttpClient().request(protocol, host, port, path));
	}


	private static HttpBuilder defaults(HttpBuilder builder) {
		if (defaultTimeout != null) {
			builder.timeout(defaultTimeout);
		}
		return builder;
	}

}
