package ar.edu.itba.dps.exchange.infrastructure.api;

import ar.edu.itba.dps.exchange.infrastructure.http.UnirestHttpClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

class FreeCurrencyApiProviderIT {

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(options().dynamicPort().usingFilesUnderClasspath("wiremock"))
			.build();

	@Test
	void testGetAvailableCurrencies() {
		// Given
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");

		// When
		final var currencies = provider.getAvailableCurrencies();

		// Then
		Assertions.assertEquals(2, currencies.size());
		Assertions.assertTrue(currencies.contains(Currency.getInstance("USD")));
		Assertions.assertTrue(currencies.contains(Currency.getInstance("EUR")));
	}

	@Test
	void testGetHistoricalCurrencyRate() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var eur = Currency.getInstance("EUR");
		final var usd = Currency.getInstance("USD");
		final var date = LocalDate.of(2022, 1, 1);

		final var quote = provider.getHistoricalCurrencyRates(eur, List.of(usd), date).getFirst();

		Assertions.assertEquals(usd, quote.target());
		Assertions.assertEquals(BigDecimal.valueOf(1.1347), quote.quotedRate().rate());
	}

	@Test
	void testGetHistoricalCurrencyRates() {
		// Given
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var eur = Currency.getInstance("EUR");
		final var usd = Currency.getInstance("USD");
		final var cad = Currency.getInstance("CAD");
		final var date = LocalDate.of(2022, 1, 1);

		// When
		final var rates = provider.getHistoricalCurrencyRates(eur, List.of(usd, cad), date);

		// Then
		Assertions.assertEquals(2, rates.size());
		Assertions.assertEquals(usd, rates.get(0).target());
		Assertions.assertEquals(cad, rates.get(1).target());
		Assertions.assertEquals(BigDecimal.valueOf(1.1347), rates.get(0).quotedRate().rate());
		Assertions.assertEquals(BigDecimal.valueOf(1.5623), rates.get(1).quotedRate().rate());
	}

	@Test
	void testGetCurrencyRate() {
		// Given
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var eur = Currency.getInstance("EUR");
		final var usd = Currency.getInstance("USD");

		// When
		final var quote = provider.getCurrencyRates(eur, List.of(usd)).getFirst();

		// Then
		Assertions.assertEquals(usd, quote.target());
		Assertions.assertEquals(BigDecimal.valueOf(1.0847), quote.quotedRate().rate());
	}

	@Test
	void testGetCurrencyRates() {
		// Given
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var eur = Currency.getInstance("EUR");
		final var usd = Currency.getInstance("USD");
		final var cad = Currency.getInstance("CAD");

		// When
		final var rates = provider.getCurrencyRates(eur, List.of(usd, cad));

		// Then
		Assertions.assertEquals(2, rates.size());
		Assertions.assertEquals(usd, rates.get(0).target());
		Assertions.assertEquals(cad, rates.get(1).target());
		Assertions.assertEquals(BigDecimal.valueOf(1.0847), rates.get(0).quotedRate().rate());
		Assertions.assertEquals(BigDecimal.valueOf(1.4823), rates.get(1).quotedRate().rate());
	}
}
