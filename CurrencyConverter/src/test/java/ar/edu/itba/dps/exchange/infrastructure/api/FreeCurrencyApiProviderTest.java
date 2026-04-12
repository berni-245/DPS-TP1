package ar.edu.itba.dps.exchange.infrastructure.api;

import ar.edu.itba.dps.exchange.domain.CurrencyRateNotAvailableException;
import ar.edu.itba.dps.exchange.domain.CurrencyRateRemoteException;
import ar.edu.itba.dps.exchange.domain.CurrencyRateTransportException;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpClient;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpResponse;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpTransportException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
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

	private static final Currency EUR = Currency.getInstance("EUR");
	private static final Currency USD = Currency.getInstance("USD");

	@Test
	void http404_throwsRemoteExceptionWithStatus() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(
				new HttpResponse("{\"message\":\"Not Found\"}", 404));
		final var provider = new FreeCurrencyApiProvider(http, "https://example.com/v1/");

		final var ex = assertThrows(CurrencyRateRemoteException.class,
				() -> provider.getCurrencyRates(EUR, List.of(USD)));

		assertThat(ex.statusCode(), is(404));
		assertThat(ex.getMessage(), containsString("404"));
		assertThat(ex.responseDetail().orElse(""), containsString("Not Found"));
	}

	@Test
	void http500_throwsRemoteExceptionWithStatus() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(
				new HttpResponse("Internal Error", 500));
		final var provider = new FreeCurrencyApiProvider(http, "https://example.com/v1/");

		final var ex = assertThrows(CurrencyRateRemoteException.class,
				provider::getAvailableCurrencies);

		assertThat(ex.statusCode(), is(500));
		assertThat(ex.getMessage(), containsString("500"));
	}

	@Test
	void transportFailure_throwsTransportException() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any()))
				.thenThrow(new HttpTransportException("timeout", new RuntimeException("simulated")));
		final var provider = new FreeCurrencyApiProvider(http, "https://example.com/v1/");

		final var ex = assertThrows(CurrencyRateTransportException.class,
				() -> provider.getHistoricalCurrencyRates(EUR, List.of(USD), LocalDate.of(2022, 1, 1)));

		assertThat(ex.getMessage(), containsString("Failed to contact"));
		assertThat(ex.getCause(), instanceOf(HttpTransportException.class));
	}

	@Test
	void sanitizeResponseExcerpt_truncatesLongBody() {
		final String longBody = "x".repeat(400);
		final String excerpt = FreeCurrencyApiProvider.sanitizeResponseExcerpt(longBody);
		assertThat(excerpt.length(), lessThanOrEqualTo(258));
		assertThat(excerpt, endsWith("…"));
	}

	@Test
	void singleArgConstructor_usesDefaultBaseUrl() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(
				new HttpResponse("{\"data\":{\"USD\":1.08}}", 200));
		final var provider = new FreeCurrencyApiProvider(http);

		provider.getCurrencyRates(EUR, List.of(USD));

		verify(http).get(
				argThat(uri -> uri.toString().startsWith("https://api.freecurrencyapi.com/v1/")),
				eq(Map.of("base_currency", "EUR", "currencies", "USD")),
				any());
	}

	@Test
	void baseUrlWithoutTrailingSlash_isNormalized() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(
				new HttpResponse("{\"data\":{\"USD\":1}}", 200));
		final var provider = new FreeCurrencyApiProvider(http, "https://example.com/v1");

		provider.getCurrencyRates(EUR, List.of(USD));

		verify(http).get(
				argThat(uri -> uri.toString().startsWith("https://example.com/v1/")),
				any(),
				any());
	}

	@Test
	void invalidJson200_throwsNotAvailable() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), any(), any())).thenReturn(new HttpResponse("not-json", 200));
		final var provider = new FreeCurrencyApiProvider(http, "https://example.com/v1/");

		assertThrows(CurrencyRateNotAvailableException.class,
				() -> provider.getCurrencyRates(EUR, List.of(USD)));
	}

	@Test
	void getDailyTimeOfRateMeasurement_isEndOfServiceDay() {
		final var http = mock(HttpClient.class);
		final var provider = new FreeCurrencyApiProvider(http, "https://example.com/v1/");

		assertThat(provider.getDailyTimeOfRateMeasurement(), is(LocalTime.of(23, 59, 59)));
	}

	@Test
	void sanitizeResponseExcerpt_blankBody_returnsEmpty() {
		assertThat(FreeCurrencyApiProvider.sanitizeResponseExcerpt("   \n"), is(""));
	}

	@Test
	void sanitizeResponseExcerpt_nullBody_returnsEmpty() {
		assertThat(FreeCurrencyApiProvider.sanitizeResponseExcerpt(null), is(""));
	}

	@Test
	void latest_ok_usesCorrectQueryParams() {
		final var http = mock(HttpClient.class);
		when(http.get(any(URI.class), eq(Map.of("base_currency", "EUR", "currencies", "USD")), any()))
				.thenReturn(new HttpResponse(
						"{\"data\":{\"USD\":1.08}}", 200));
		final var provider = new FreeCurrencyApiProvider(http, "https://example.com/v1/");

		final var rate = provider.getCurrencyRates(EUR, List.of(USD)).getFirst();

		assertThat(rate.rate(), comparesEqualTo(BigDecimal.valueOf(1.08)));
	}
}
