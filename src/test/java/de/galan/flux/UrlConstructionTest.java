package de.galan.flux;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.galan.commons.test.AbstractTestParent;


/**
 * CUT UrlConstruction
 *
 * @author daniel
 */
public class UrlConstructionTest extends AbstractTestParent {

	@Test
	public void resourceConstruction() {
		assertEquals("ftp://horst:666/starts/with/slash", UrlConstruction.constructResource("ftp", "horst", 666, "/starts/with/slash"));
		assertEquals("ftp://horst:23/starts/with/noslash", UrlConstruction.constructResource("ftp", "horst", 23, "starts/with/noslash"));
		assertEquals("http://der.horst.de:177/somewhere", UrlConstruction.constructResource("http", "der.horst.de", 177, "somewhere"));
		assertEquals("http://der.horst.de/somewhere", UrlConstruction.constructResource("http", "der.horst.de", -1, "somewhere"));
		assertEquals("http://der.horst.de/somewhere", UrlConstruction.constructResource("http", "der.horst.de", 0, "somewhere"));
		assertEquals("http://der.horst.de/somewhere", UrlConstruction.constructResource("http", "der.horst.de", null, "somewhere"));
	}


	@Test
	public void parameterAppend() throws Exception {
		String resource = UrlConstruction.constructResource("http", "der.horst.de", 177, "somewhere");
		Map<String, List<String>> parameter = new HashMap<>();
		assertEquals("http://der.horst.de:177/somewhere", UrlConstruction.appendParameters(resource, parameter));
		parameter.put("k1", Arrays.asList("v1"));
		assertEquals("http://der.horst.de:177/somewhere?k1=v1", UrlConstruction.appendParameters(resource, parameter));
		parameter.put("k2", Arrays.asList("v2"));
		assertEquals("http://der.horst.de:177/somewhere?k1=v1&k2=v2", UrlConstruction.appendParameters(resource, parameter));
		assertEquals("http://der.horst.de:177/somewhere?x=y&k1=v1&k2=v2", UrlConstruction.appendParameters(resource + "?x=y", parameter));
	}

}
