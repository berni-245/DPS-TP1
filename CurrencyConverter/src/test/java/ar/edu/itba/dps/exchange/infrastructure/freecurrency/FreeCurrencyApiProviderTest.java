package ar.edu.itba.dps.exchange.infrastructure.freecurrency;

import ar.edu.itba.dps.exchange.domain.exception.CurrencyRateNotAvailableException;
import ar.edu.itba.dps.exchange.domain.exception.CurrencyRateRemoteException;
import ar.edu.itba.dps.exchange.domain.exception.CurrencyRateConnectionException;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpClient;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpResponse;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpClientException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FreeCurrencyApiProviderTest {

	private static final String EUR_CODE = "EUR";
	private static final String USD_CODE = "USD";
	private static final Currency EUR = Currency.getInstance(EUR_CODE);
	private static final Currency USD = Currency.getInstance(USD_CODE);
	private static final int HTTP_OK = 200;
	private static final int HTTP_NOT_FOUND = 404;
	private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
	private static final LocalDate HISTORICAL_DATE = LocalDate.of(2022, 1, 1);
	private static final String LATEST_USD_RATE_PLAIN = "1.08";
	private static final BigDecimal LATEST_USD_RATE = new BigDecimal(LATEST_USD_RATE_PLAIN);
	private static final String JSON_KEY_DATA = "data";
	private static final String JSON_KEY_MESSAGE = "message";
	private static final String NOT_FOUND_PHRASE = "Not Found";
	private static final String LATEST_USD_RATE_JSON_BODY =
			"{\"" + JSON_KEY_DATA + "\":{\"" + USD_CODE + "\":" + LATEST_USD_RATE + "}}";
	private static final String NOT_FOUND_JSON_BODY =
			"{\"" + JSON_KEY_MESSAGE + "\":\"" + NOT_FOUND_PHRASE + "\"}";
	private static final String EXAMPLE_V1_BASE_URL = "https://example.com/v1/";
	private static final String BASE_URL_WITHOUT_TRAILING_SLASH = "https://example.com/v1";
	private static final String INVALID_JSON_BODY = "not-json";
	private static final String INTERNAL_ERROR_BODY = "Internal Error";
	private static final String USD_RATE_ONE_JSON_BODY =
			"{\"" + JSON_KEY_DATA + "\":{\"" + USD_CODE + "\":" + BigDecimal.ONE + "}}";
	private static final String DEFAULT_FREE_CURRENCY_API_PREFIX = "https://api.freecurrencyapi.com/v1/";
	private static final String QUERY_PARAM_BASE_CURRENCY = "base_currency";
	private static final String QUERY_PARAM_CURRENCIES = "currencies";
	private static final String HTTP_CLIENT_TIMEOUT_MESSAGE = "timeout";
	private static final String SIMULATED_CAUSE_MESSAGE = "simulated";
	private static final String FAILED_TO_CONTACT_SNIPPET = "Failed to contact";

	@Test
	void getCurrencyRatesTransportFailureThrowsTransportException() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any()))
				.thenThrow(new HttpClientException(HTTP_CLIENT_TIMEOUT_MESSAGE, new RuntimeException(SIMULATED_CAUSE_MESSAGE)));
		final var provider = new FreeCurrencyApiProvider(http, EXAMPLE_V1_BASE_URL);

		final var ex = assertThrows(CurrencyRateConnectionException.class,
				() -> provider.getCurrencyRates(EUR, List.of(USD)));

		assertThat(ex.getMessage(), containsString(FAILED_TO_CONTACT_SNIPPET));
		assertThat(ex.getCause(), instanceOf(HttpClientException.class));
	}

	@Test
	void historicalHttp404ThrowsRemoteExceptionWithStatus() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(
				new HttpResponse(NOT_FOUND_JSON_BODY, HTTP_NOT_FOUND));
		final var provider = new FreeCurrencyApiProvider(http, EXAMPLE_V1_BASE_URL);

		final var ex = assertThrows(CurrencyRateRemoteException.class,
				() -> provider.getHistoricalCurrencyRates(EUR, List.of(USD), HISTORICAL_DATE));

		assertThat(ex.getMessage(), containsString(Integer.toString(HTTP_NOT_FOUND)));
	}

	@Test
	void getAvailableCurrenciesInvalidJsonThrowsNotAvailable() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(new HttpResponse(INVALID_JSON_BODY, HTTP_OK));
		final var provider = new FreeCurrencyApiProvider(http, EXAMPLE_V1_BASE_URL);

		assertThrows(CurrencyRateNotAvailableException.class, provider::getAvailableCurrencies);
	}

	@Test
	void getHistoricalCurrencyRatesInvalidJsonThrowsNotAvailable() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(new HttpResponse(INVALID_JSON_BODY, HTTP_OK));
		final var provider = new FreeCurrencyApiProvider(http, EXAMPLE_V1_BASE_URL);

		assertThrows(CurrencyRateNotAvailableException.class,
				() -> provider.getHistoricalCurrencyRates(EUR, List.of(USD), HISTORICAL_DATE));
	}

	@Test
	void http404ThrowsRemoteExceptionWithStatus() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(
				new HttpResponse(NOT_FOUND_JSON_BODY, HTTP_NOT_FOUND));
		final var provider = new FreeCurrencyApiProvider(http, EXAMPLE_V1_BASE_URL);

		final var ex = assertThrows(CurrencyRateRemoteException.class,
				() -> provider.getCurrencyRates(EUR, List.of(USD)));

		assertThat(ex.getMessage(), containsString(Integer.toString(HTTP_NOT_FOUND)));
		assertThat(ex.getMessage(), containsString(NOT_FOUND_PHRASE));
	}

	@Test
	void http500ThrowsRemoteExceptionWithStatus() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(
				new HttpResponse(INTERNAL_ERROR_BODY, HTTP_INTERNAL_SERVER_ERROR));
		final var provider = new FreeCurrencyApiProvider(http, EXAMPLE_V1_BASE_URL);

		final var ex = assertThrows(CurrencyRateRemoteException.class,
				provider::getAvailableCurrencies);

		assertThat(ex.getMessage(), containsString(Integer.toString(HTTP_INTERNAL_SERVER_ERROR)));
	}

	@Test
	void transportFailureThrowsTransportException() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any()))
				.thenThrow(new HttpClientException(HTTP_CLIENT_TIMEOUT_MESSAGE, new RuntimeException(SIMULATED_CAUSE_MESSAGE)));
		final var provider = new FreeCurrencyApiProvider(http, EXAMPLE_V1_BASE_URL);

		final var ex = assertThrows(CurrencyRateConnectionException.class,
				() -> provider.getHistoricalCurrencyRates(EUR, List.of(USD), HISTORICAL_DATE));

		assertThat(ex.getMessage(), containsString(FAILED_TO_CONTACT_SNIPPET));
		assertThat(ex.getCause(), instanceOf(HttpClientException.class));
	}

	@Test
	void singleArgConstructorUsesDefaultBaseUrl() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(
				new HttpResponse(LATEST_USD_RATE_JSON_BODY, HTTP_OK));
		final var provider = new FreeCurrencyApiProvider(http);

		provider.getCurrencyRates(EUR, List.of(USD));

		verify(http).get(
				argThat(uri -> uri.toString().startsWith(DEFAULT_FREE_CURRENCY_API_PREFIX)),
				eq(Map.of(QUERY_PARAM_BASE_CURRENCY, EUR_CODE, QUERY_PARAM_CURRENCIES, USD_CODE)),
				any());
	}

	@Test
	void baseUrlWithoutTrailingSlashIsNormalized() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(
				new HttpResponse(USD_RATE_ONE_JSON_BODY, HTTP_OK));
		final var provider = new FreeCurrencyApiProvider(http, BASE_URL_WITHOUT_TRAILING_SLASH);

		provider.getCurrencyRates(EUR, List.of(USD));

		verify(http).get(
				argThat(uri -> uri.toString().startsWith(EXAMPLE_V1_BASE_URL)),
				any(),
				any());
	}

	@Test
	void invalidJson200ThrowsNotAvailable() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(new HttpResponse(INVALID_JSON_BODY, HTTP_OK));
		final var provider = new FreeCurrencyApiProvider(http, EXAMPLE_V1_BASE_URL);

		assertThrows(CurrencyRateNotAvailableException.class,
				() -> provider.getCurrencyRates(EUR, List.of(USD)));
	}

	@Test
	void latestOkUsesCorrectQueryParams() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), eq(Map.of(QUERY_PARAM_BASE_CURRENCY, EUR_CODE, QUERY_PARAM_CURRENCIES, USD_CODE)), any()))
				.thenReturn(new HttpResponse(LATEST_USD_RATE_JSON_BODY, HTTP_OK));
		final var provider = new FreeCurrencyApiProvider(http, EXAMPLE_V1_BASE_URL);

		final var targetRate = provider.getCurrencyRates(EUR, List.of(USD)).getFirst();

		assertThat(targetRate.target(), is(USD));
		assertThat(targetRate.currencyRate().rate(), comparesEqualTo(LATEST_USD_RATE));
	}
}
