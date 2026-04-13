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

	private static final Currency EUR = Currency.getInstance("EUR");
	private static final Currency USD = Currency.getInstance("USD");
	private static final Currency CAD = Currency.getInstance("CAD");

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
		Assertions.assertTrue(currencies.contains(USD));
		Assertions.assertTrue(currencies.contains(EUR));
	}

	@Test
	void testGetHistoricalCurrencyRate() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var date = LocalDate.of(2022, 1, 1);

		final var rate = provider.getHistoricalCurrencyRates(EUR, List.of(USD), date).getFirst();

		Assertions.assertEquals(BigDecimal.valueOf(1.1347), rate.rate());
	}

	@Test
	void testGetHistoricalCurrencyRates() {
		// Given
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var date = LocalDate.of(2022, 1, 1);

		// When
		final var rates = provider.getHistoricalCurrencyRates(EUR, List.of(USD, CAD), date);

		// Then
		Assertions.assertEquals(2, rates.size());
		Assertions.assertEquals(BigDecimal.valueOf(1.1347), rates.get(0).rate());
		Assertions.assertEquals(BigDecimal.valueOf(1.5623), rates.get(1).rate());
	}

	@Test
	void testGetCurrencyRate() {
		// Given
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		// When
		final var rate = provider.getCurrencyRates(EUR, List.of(USD)).getFirst();

		// Then
		Assertions.assertEquals(BigDecimal.valueOf(1.0847), rate.rate());
	}

	@Test
	void testGetCurrencyRates() {
		// Given
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		// When
		final var rates = provider.getCurrencyRates(EUR, List.of(USD, CAD));

		// Then
		Assertions.assertEquals(2, rates.size());
		Assertions.assertEquals(BigDecimal.valueOf(1.0847), rates.get(0).rate());
		Assertions.assertEquals(BigDecimal.valueOf(1.4823), rates.get(1).rate());
	}
}
