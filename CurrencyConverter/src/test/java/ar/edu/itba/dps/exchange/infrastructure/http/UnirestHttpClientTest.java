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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnirestHttpClientTest {

	private static final int HTTP_NOT_FOUND = 404;
	private static final int HTTP_OK = 200;
	private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
	private static final int ANY_FREE_PORT = 0;
	private static final String PATH_LATEST = "/v1/latest";
	private static final String PATH_QUERY = "/q";
	private static final String PATH_FAIL = "/fail";
	private static final String JSON_KEY_ERROR = "error";
	private static final String ERROR_NOT_FOUND_VALUE = "not_found";
	private static final String BODY_NOT_FOUND_JSON =
			"{\"" + JSON_KEY_ERROR + "\":\"" + ERROR_NOT_FOUND_VALUE + "\"}";
	private static final String BODY_OK = "ok";
	private static final String BODY_BOOM = "boom";
	private static final String QUERY_PARAM_BASE_CURRENCY = "base_currency";
	private static final String EUR_CODE = "EUR";
	private static final String HTTP_SCHEME = "http://";
	private static final String LOOPBACK_ADDRESS = "127.0.0.1";
	private static final String URI_PATH_ROOT = "/";

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(options().dynamicPort())
			.build();

	@Test
	void returnsRealStatusAndBodyFor404() {
		wireMock.stubFor(get(urlPathEqualTo(PATH_LATEST))
				.willReturn(aResponse()
						.withStatus(HTTP_NOT_FOUND)
						.withBody(BODY_NOT_FOUND_JSON)));

		final var client = new UnirestHttpClient();
		final var uri = URI.create(wireMock.baseUrl() + PATH_LATEST);
		final var response = client.get(uri, Map.of(QUERY_PARAM_BASE_CURRENCY, EUR_CODE), Map.of());

		assertEquals(HTTP_NOT_FOUND, response.statusCode());
		assertTrue(response.body().contains(ERROR_NOT_FOUND_VALUE));
	}

	@Test
	void nullQueryParamsDefaultsToEmptyMap() {
		wireMock.stubFor(get(urlPathEqualTo(PATH_QUERY))
				.willReturn(aResponse().withStatus(HTTP_OK).withBody(BODY_OK)));

		final var client = new UnirestHttpClient();
		final var uri = URI.create(wireMock.baseUrl() + PATH_QUERY);
		final var response = client.get(uri, null, Map.of());

		assertEquals(HTTP_OK, response.statusCode());
		assertEquals(BODY_OK, response.body());
	}

	@Test
	void returnsRealStatusFor500() {
		wireMock.stubFor(get(urlPathEqualTo(PATH_FAIL))
				.willReturn(aResponse().withStatus(HTTP_INTERNAL_SERVER_ERROR).withBody(BODY_BOOM)));

		final var client = new UnirestHttpClient();
		final var uri = URI.create(wireMock.baseUrl() + PATH_FAIL);
		final var response = client.get(uri, Map.of(), Map.of());

		assertEquals(HTTP_INTERNAL_SERVER_ERROR, response.statusCode());
		assertEquals(BODY_BOOM, response.body());
	}

	@Test
	void connectionRefusedThrowsHttpTransportException() throws Exception {
		final int closedPort;
		try (ServerSocket ss = new ServerSocket(ANY_FREE_PORT)) {
			closedPort = ss.getLocalPort();
		}
		final var client = new UnirestHttpClient();
		final var uri = URI.create(HTTP_SCHEME + LOOPBACK_ADDRESS + ":" + closedPort + URI_PATH_ROOT);

		assertThrows(HttpClientException.class,
				() -> client.get(uri, Map.of(), Map.of()));
	}
}
