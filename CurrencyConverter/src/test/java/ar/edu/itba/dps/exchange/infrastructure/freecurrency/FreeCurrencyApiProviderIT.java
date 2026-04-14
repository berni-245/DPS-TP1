package ar.edu.itba.dps.exchange.infrastructure.freecurrency;

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
	void getAvailableCurrencies() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");

		final var currencies = provider.getAvailableCurrencies();

		Assertions.assertEquals(2, currencies.size());
		Assertions.assertTrue(currencies.contains(USD));
		Assertions.assertTrue(currencies.contains(EUR));
	}

	@Test
	void getHistoricalCurrencyRate() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var date = LocalDate.of(2022, 1, 1);

		final var targetRate = provider.getHistoricalCurrencyRates(EUR, List.of(USD), date).getFirst();

		Assertions.assertEquals(USD, targetRate.target());
		Assertions.assertEquals(BigDecimal.valueOf(1.1347), targetRate.currencyRate().rate());
	}

	@Test
	void getHistoricalCurrencyRates() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var date = LocalDate.of(2022, 1, 1);

		final var rates = provider.getHistoricalCurrencyRates(EUR, List.of(USD, CAD), date);

		Assertions.assertEquals(2, rates.size());
		Assertions.assertEquals(USD, rates.get(0).target());
		Assertions.assertEquals(CAD, rates.get(1).target());
		Assertions.assertEquals(BigDecimal.valueOf(1.1347), rates.get(0).currencyRate().rate());
		Assertions.assertEquals(BigDecimal.valueOf(1.5623), rates.get(1).currencyRate().rate());
	}

	@Test
	void getCurrencyRate() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");

		final var targetRate = provider.getCurrencyRates(EUR, List.of(USD)).getFirst();

		Assertions.assertEquals(USD, targetRate.target());
		Assertions.assertEquals(BigDecimal.valueOf(1.0847), targetRate.currencyRate().rate());
	}

	@Test
	void getCurrencyRates() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");

		final var rates = provider.getCurrencyRates(EUR, List.of(USD, CAD));

		Assertions.assertEquals(2, rates.size());
		Assertions.assertEquals(USD, rates.get(0).target());
		Assertions.assertEquals(CAD, rates.get(1).target());
		Assertions.assertEquals(BigDecimal.valueOf(1.0847), rates.get(0).currencyRate().rate());
		Assertions.assertEquals(BigDecimal.valueOf(1.4823), rates.get(1).currencyRate().rate());
	}
}
