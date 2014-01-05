package de.galan.flupi.proxy;

import static org.junit.Assert.*;

import org.junit.Test;

import de.galan.commons.test.AbstractTestParent;


/**
 * CUT CommonProxy
 * 
 * @author daniel
 */
public class CommonProxyTest extends AbstractTestParent {

	@Test
	public void name() throws Exception {
		assertProxy(new CommonProxy("1.1.1.1"), "1.1.1.1", 80, null, null, "1.1.1.1:80");
		assertProxy(new CommonProxy("1.1.1.1", 123), "1.1.1.1", 123, null, null, "1.1.1.1:123");
		assertProxy(new CommonProxy("1.1.1.1", null, "user", null), "1.1.1.1", 80, "user", null, "user@1.1.1.1:80");
		assertProxy(new CommonProxy("1.1.1.1", null, "user", "pass"), "1.1.1.1", 80, "user", "pass", "user:pass@1.1.1.1:80");
		assertProxy(new CommonProxy("1.1.1.1", null, null, "pass"), "1.1.1.1", 80, null, "pass", "1.1.1.1:80");
		assertProxy(CommonProxy.parse("1.1.1.1:666"), "1.1.1.1", 666, null, null, "1.1.1.1:666");
	}


	private void assertProxy(CommonProxy cp, String ip, int port, String username, String password, String toString) {
		assertEquals(ip, cp.getIp());
		assertEquals(port, cp.getPort());
		assertEquals(username, cp.getUsername());
		assertEquals(password, cp.getPassword());
		assertEquals(toString, cp.toString());
	}

}
