package de.galan.flux.proxy;

import static org.junit.Assert.*;

import org.junit.Test;

import de.galan.commons.test.AbstractTestParent;
import de.galan.flux.proxy.CommonProxy;
import de.galan.flux.proxy.CommonProxyParser;


/**
 * CUT CommonProxyParser
 * 
 * @author daniel
 */
public class CommonProxyParserTest extends AbstractTestParent {

	CommonProxyParser pp = new CommonProxyParser();


	@Test
	public void parse() {
		assertValid("1.2.3.4", null, null, "1.2.3.4", 80);
		assertValid("1.2.3.4:8888", null, null, "1.2.3.4", 8888);
		assertValid("1.2.3.4:123", null, null, "1.2.3.4", 123);
		assertValid("uuu@1.2.3.4", "uuu", null, "1.2.3.4", 80);
		assertValid("uuu@1.2.3.4:123", "uuu", null, "1.2.3.4", 123);
		assertValid("uuu:ppp@1.2.3.4", "uuu", "ppp", "1.2.3.4", 80);
		assertValid("uuu:ppp@1.2.3.4:123", "uuu", "ppp", "1.2.3.4", 123);
		assertInvalid("@1.2.3.4");
		assertInvalid("uuu:@1.2.3.4");
		assertInvalid(":ppp@1.2.3.4");
		assertInvalid("uuu@ppp@1.2.3.4");
		assertInvalid("uuu:ppp@1.2.3.4@12323");
		assertInvalid("uuu@ppp:1.2.3.4@12323");
		assertInvalid("1.2.3.4:8888,9999");
	}


	protected void assertInvalid(String proxy) {
		assertNull(pp.parse(proxy));
	}


	protected void assertValid(String proxy, String username, String password, String ip, int port) {
		CommonProxy result = pp.parse(proxy);
		assertEquals(ip, result.getIp());
		assertEquals(port, result.getPort());
		assertEquals(username, result.getUsername());
		assertEquals(password, result.getPassword());
	}

}
