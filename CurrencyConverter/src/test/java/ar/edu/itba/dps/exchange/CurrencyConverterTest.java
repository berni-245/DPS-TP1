package ar.edu.itba.dps.exchange;

import ar.edu.itba.dps.exchange.domain.CurrencyConverter;
import ar.edu.itba.dps.exchange.domain.CurrencyRate;
import ar.edu.itba.dps.exchange.domain.CurrencyRateProvider;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrencyConverterTest {

	public static final Currency USD = Currency.getInstance("USD");
	public static final Currency ARS = Currency.getInstance("ARS");

	@Test
	void testConvert() {
		// Given
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRate(ARS, USD)).thenReturn(new CurrencyRate(1));
		final var fixedInstant = java.time.Instant.parse("2026-04-01T10:00:00Z");
		final var clock = java.time.Clock.fixed(fixedInstant, java.time.ZoneId.of("UTC"));
		final var converter = new CurrencyConverter(provider, clock);

		// When
		final var result = converter.convert(ARS, USD, 100);

		// Then
		assertThat(result.convertedAmount(), is(100.0));
		assertThat(result.timestamp(), is(fixedInstant));
	}

	// @Test
	// void testConvert() {
	// // Given
	// final var httpClient = mock(UnirestHttpClient.class);
	//
	// final var mock = mock(HttpResponse.class);
	//
	// when(httpClient.get(any(), any(), any())).thenReturn(mock);
	// when(mock.statusCode()).thenReturn(200);
	// when(mock.body()).thenReturn(new
	// JsonNode("{\"data\":{\"USD\":1.05}}").toString());
	//
	// final var converter = new CurrencyConverter(httpClient);
	//
	// // When
	// final var result = converter.convert("EUR", "USD", 100);
	//
	// // Then
	// assertThat(result, closeTo(105, 0.01));
	// }
}