package de.galan.flux.proxy;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import de.galan.commons.logging.Logr;


/**
 * Parses a given proxy in form [username[:password]@]ip[:port]
 * 
 * @author daniel
 */
public class CommonProxyParser {

	private static final Logger LOG = Logr.get();
	private static final Pattern PROXY_PATTERN = Pattern.compile("^([\\w+-]+(:[^@:]+)?@)?[0-9.]+(:[0-9]+([0-9]+)*)?$");


	public CommonProxy parse(String proxy) {
		return parse(proxy, CommonProxy.DEFAULT_PORT);
	}


	public CommonProxy parse(String proxy, Integer defaultPort) {
		CommonProxy result = null;
		String p = trimToNull(proxy);
		if (isNotBlank(p) && PROXY_PATTERN.matcher(p).matches()) {
			try {
				int indexAuth = StringUtils.indexOf(p, "@");
				String username = null;
				String password = null;
				String host = null;
				if (indexAuth > 0) {
					String auth = StringUtils.substring(p, 0, indexAuth);
					host = StringUtils.substring(p, indexAuth + 1, p.length());
					String[] authSplit = StringUtils.split(auth, ":", 2);
					username = authSplit[0];
					if (authSplit.length == 2) {
						password = authSplit[1];
					}
				}
				else {
					host = p;
				}
				String[] hostSplit = StringUtils.split(host, ":", 2);
				String ip = hostSplit[0];
				Integer port = defaultPort;
				if (hostSplit.length == 2) {
					port = Integer.valueOf(hostSplit[1]);
				}
				result = new CommonProxy(ip, port, username, password);
			}
			catch (Exception ex) {
				LOG.warn("Parsing CommonProxy failed {input}", ex, p);
			}
		}

		return result;
	}

}
