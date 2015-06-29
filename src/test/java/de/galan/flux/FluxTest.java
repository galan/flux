package de.galan.flux;

import static org.assertj.core.api.StrictAssertions.*;

import org.junit.Test;

import de.galan.commons.test.AbstractTestParent;
import de.galan.flux.FluentHttpClient.HttpBuilder;


/**
 * CUT Flux
 *
 * @author daniel
 */
public class FluxTest extends AbstractTestParent {

	private final static String URL1 = "http://www.example.com/resource";


	@Test
	public void plainUrl() throws Exception {
		HttpBuilder builder = Flux.request(URL1);
		assertThat(builder.builderClient).isInstanceOf(HttpClient.class);
		assertThat(builder.builderResource).isEqualTo(URL1);
		assertThat(builder.builderBody).isNull();
		assertThat(builder.builderHeader).isNull();
		assertThat(builder.builderFollowRedirects).isTrue();
	}

}
