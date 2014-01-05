package de.galan.flupi;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.galan.commons.test.AbstractTestParent;
import de.galan.flupi.FLuentHttpClient.HttpBuilder;


/**
 * CUT HttpDsl
 * 
 * @author daniel
 */
public class HttpDslParameterTest extends AbstractTestParent {

	private FLuentHttpClient http;
	private HttpBuilder builder;


	@Before
	public void before() {
		http = new FLuentHttpClient();
		builder = http.request("http://www.example.com");
		assertNull(builder.builderParameter);
	}


	private void assertParams(String key, String... items) {
		assertNotNull(builder.builderParameter);
		assertThat(builder.builderParameter, hasKey(key));
		List<String> params = new ArrayList<>(builder.builderParameter.get(key));
		for (String item: items) {
			if (params.contains(item)) {
				params.remove(item);
			}
			else {
				fail("Element '" + item + "' not in expected values ");
			}
		}
		if (!params.isEmpty()) {
			fail("Elements " + params + " not matched");
		}
		assertThat(builder.builderParameter.get(key).size(), equalTo(items.length));
		//assertThat(builder.builderParameter.get(key), hasItems(items));
	}


	@Test
	public void paramDirect1() throws Exception {
		builder.parameter("a", "b");
		assertParams("a", "b");
		builder.parameter("a", "c");
		assertParams("a", "b", "c");
		builder.parameter("x", "y");
		assertParams("a", "b", "c");
		assertParams("x", "y");
	}


	@Test
	public void paramsDirect2() throws Exception {
		builder.parameter("a", "b", "c", "d");
		assertParams("a", "b", "c", "d");
		builder.parameter("a", "b", "f");
		assertParams("a", "b", "c", "d", "b", "f");
	}


	@Test
	public void paramsMap() throws Exception {
		Map<String, String> input = new HashMap<>();
		input.put("a", "b");
		input.put("c", "d");
		builder.parameterMap(input);
		assertParams("a", "b");
		assertParams("c", "d");

		Map<String, String> input2 = new HashMap<>();
		input2.put("a", "c");
		input2.put("e", "f");
		builder.parameterMap(input2);
		assertParams("a", "b", "c");
		assertParams("c", "d");
		assertParams("e", "f");
	}


	@Test
	public void paramsList() throws Exception {
		Map<String, List<String>> input = new HashMap<>();
		input.put("a", Arrays.asList("b", "c"));
		input.put("x", Arrays.asList("y"));
		builder.parameterList(input);
		assertParams("a", "b", "c");
		assertParams("x", "y");
	}


	@Test
	public void paramsEmpty() throws Exception {
		builder.parameter("a");
		assertTrue(builder.builderParameter.containsKey("a"));
		assertTrue(builder.builderParameter.get("a").isEmpty());
	}

}
