package ar.edu.itba.dps.exchange.infrastructure.api;

import ar.edu.itba.dps.exchange.infrastructure.http.UnirestHttpClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.LocalDate;
import java.util.Currency;

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

		final var rate = provider.getHistoricalCurrencyRate(eur, usd, date);

		Assertions.assertEquals(1.1347, rate.rate(), 1e-9);
	}

	@Test
	void testgetCurrencyRate() {
		// Given
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var eur = Currency.getInstance("EUR");
		final var usd = Currency.getInstance("USD");

		// When
		final var rate = provider.getCurrencyRate(eur, usd);

		// Then
		Assertions.assertEquals(1.0847, rate.rate(), 1e-9);
	}
}
