package ar.edu.itba.dps.exchange.infrastructure.http;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.ServerSocket;
import java.net.URI;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnirestHttpClientTest {

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(options().dynamicPort())
			.build();

	@Test
	void returnsRealStatusAndBodyFor404() {
		wireMock.stubFor(get(urlPathEqualTo("/v1/latest"))
				.willReturn(aResponse()
						.withStatus(404)
						.withBody("{\"error\":\"not_found\"}")));

		final var client = new UnirestHttpClient();
		final var uri = URI.create(wireMock.baseUrl() + "/v1/latest");
		final var response = client.get(uri, Map.of("base_currency", "EUR"), Map.of());

		assertThat(response.statusCode(), is(404));
		assertThat(response.body(), containsString("not_found"));
	}

	@Test
	void nullQueryParams_defaultsToEmptyMap() {
		wireMock.stubFor(get(urlPathEqualTo("/q"))
				.willReturn(aResponse().withStatus(200).withBody("ok")));

		final var client = new UnirestHttpClient();
		final var uri = URI.create(wireMock.baseUrl() + "/q");
		final var response = client.get(uri, null, Map.of());

		assertThat(response.statusCode(), is(200));
		assertThat(response.body(), is("ok"));
	}

	@Test
	void returnsRealStatusFor500() {
		wireMock.stubFor(get(urlPathEqualTo("/fail"))
				.willReturn(aResponse().withStatus(500).withBody("boom")));

		final var client = new UnirestHttpClient();
		final var uri = URI.create(wireMock.baseUrl() + "/fail");
		final var response = client.get(uri, Map.of(), Map.of());

		assertThat(response.statusCode(), is(500));
		assertThat(response.body(), is("boom"));
	}

	@Test
	void connectionRefused_throwsHttpTransportException() throws Exception {
		final int closedPort;
		try (ServerSocket ss = new ServerSocket(0)) {
			closedPort = ss.getLocalPort();
		}
		final var client = new UnirestHttpClient();
		final var uri = URI.create("http://127.0.0.1:" + closedPort + "/");

		assertThrows(HttpTransportException.class,
				() -> client.get(uri, Map.of(), Map.of()));
	}
}
