package ar.edu.itba.dps.exchange.integration;

import ar.edu.itba.dps.exchange.infrastructure.freecurrency.FreeCurrencyApiProvider;
import ar.edu.itba.dps.exchange.infrastructure.http.UnirestHttpClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FreeCurrencyApiProviderIT {

	private static final String EUR_CODE = "EUR";
	private static final String USD_CODE = "USD";
	private static final String CAD_CODE = "CAD";
	private static final String API_V1_PATH_SUFFIX = "/v1/";
	private static final String WIREMOCK_FILES_CLASSPATH = "wiremock";

	private static final Currency EUR = Currency.getInstance(EUR_CODE);
	private static final Currency USD = Currency.getInstance(USD_CODE);
	private static final Currency CAD = Currency.getInstance(CAD_CODE);

	private static final LocalDate HISTORICAL_DATE = LocalDate.of(2022, 1, 1);

	private static final int MULTI_TARGET_COUNT = 2;
	private static final int FIRST_RESULT_INDEX = 0;
	private static final int SECOND_RESULT_INDEX = 1;

	private static final String HISTORICAL_USD_RATE_PLAIN = "1.1347";
	private static final String HISTORICAL_CAD_RATE_PLAIN = "1.5623";
	private static final String LATEST_USD_RATE_PLAIN = "1.0847";
	private static final String LATEST_CAD_RATE_PLAIN = "1.4823";
	private static final BigDecimal HISTORICAL_USD_RATE = new BigDecimal(HISTORICAL_USD_RATE_PLAIN);
	private static final BigDecimal HISTORICAL_CAD_RATE = new BigDecimal(HISTORICAL_CAD_RATE_PLAIN);
	private static final BigDecimal LATEST_USD_RATE = new BigDecimal(LATEST_USD_RATE_PLAIN);
	private static final BigDecimal LATEST_CAD_RATE = new BigDecimal(LATEST_CAD_RATE_PLAIN);

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(options().dynamicPort().usingFilesUnderClasspath(WIREMOCK_FILES_CLASSPATH))
			.build();

	private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
		assertEquals(0, expected.compareTo(actual));
	}

	private String wiremockV1BaseUrl() {
		return wireMock.baseUrl() + API_V1_PATH_SUFFIX;
	}

	@Test
	void getAvailableCurrencies() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wiremockV1BaseUrl());

		final var currencies = provider.getAvailableCurrencies();

		assertEquals(MULTI_TARGET_COUNT, currencies.size());
		assertTrue(currencies.contains(USD));
		assertTrue(currencies.contains(EUR));
	}

	@Test
	void getHistoricalCurrencyRate() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wiremockV1BaseUrl());

		final var targetRate = provider.getHistoricalCurrencyRates(EUR, List.of(USD), HISTORICAL_DATE).getFirst();

		assertEquals(USD, targetRate.target());
		assertBigDecimalEquals(HISTORICAL_USD_RATE, targetRate.currencyRate().rate());
	}

	@Test
	void getHistoricalCurrencyRates() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wiremockV1BaseUrl());

		final var rates = provider.getHistoricalCurrencyRates(EUR, List.of(USD, CAD), HISTORICAL_DATE);

		assertEquals(MULTI_TARGET_COUNT, rates.size());
		assertEquals(USD, rates.get(FIRST_RESULT_INDEX).target());
		assertEquals(CAD, rates.get(SECOND_RESULT_INDEX).target());
		assertBigDecimalEquals(HISTORICAL_USD_RATE, rates.get(FIRST_RESULT_INDEX).currencyRate().rate());
		assertBigDecimalEquals(HISTORICAL_CAD_RATE, rates.get(SECOND_RESULT_INDEX).currencyRate().rate());
	}

	@Test
	void getCurrencyRate() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wiremockV1BaseUrl());

		final var targetRate = provider.getCurrencyRates(EUR, List.of(USD)).getFirst();

		assertEquals(USD, targetRate.target());
		assertBigDecimalEquals(LATEST_USD_RATE, targetRate.currencyRate().rate());
	}

	@Test
	void getCurrencyRates() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wiremockV1BaseUrl());

		final var rates = provider.getCurrencyRates(EUR, List.of(USD, CAD));

		assertEquals(MULTI_TARGET_COUNT, rates.size());
		assertEquals(USD, rates.get(FIRST_RESULT_INDEX).target());
		assertEquals(CAD, rates.get(SECOND_RESULT_INDEX).target());
		assertBigDecimalEquals(LATEST_USD_RATE, rates.get(FIRST_RESULT_INDEX).currencyRate().rate());
		assertBigDecimalEquals(LATEST_CAD_RATE, rates.get(SECOND_RESULT_INDEX).currencyRate().rate());
	}
}
