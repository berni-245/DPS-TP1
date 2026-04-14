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

	private static final String EUR_CODE = "EUR";
	private static final String USD_CODE = "USD";
	private static final String CAD_CODE = "CAD";
	private static final String API_V1_PATH_SUFFIX = "/v1/";
	private static final String WIREMOCK_FILES_CLASSPATH = "wiremock";
	private static final Currency EUR = Currency.getInstance(EUR_CODE);
	private static final Currency USD = Currency.getInstance(USD_CODE);
	private static final Currency CAD = Currency.getInstance(CAD_CODE);
	private static final int EXPECTED_TWO_RATES = 2;
	private static final int FIRST_INDEX = 0;
	private static final int SECOND_INDEX = 1;
	private static final LocalDate HISTORICAL_DATE = LocalDate.of(2022, 1, 1);
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

	@Test
	void getAvailableCurrencies() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + API_V1_PATH_SUFFIX);

		final var currencies = provider.getAvailableCurrencies();

		Assertions.assertEquals(EXPECTED_TWO_RATES, currencies.size());
		Assertions.assertTrue(currencies.contains(USD));
		Assertions.assertTrue(currencies.contains(EUR));
	}

	@Test
	void getHistoricalCurrencyRate() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + API_V1_PATH_SUFFIX);

		final var targetRate = provider.getHistoricalCurrencyRates(EUR, List.of(USD), HISTORICAL_DATE).getFirst();

		Assertions.assertEquals(USD, targetRate.target());
		Assertions.assertEquals(HISTORICAL_USD_RATE, targetRate.currencyRate().rate());
	}

	@Test
	void getHistoricalCurrencyRates() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + API_V1_PATH_SUFFIX);

		final var rates = provider.getHistoricalCurrencyRates(EUR, List.of(USD, CAD), HISTORICAL_DATE);

		Assertions.assertEquals(EXPECTED_TWO_RATES, rates.size());
		Assertions.assertEquals(USD, rates.get(FIRST_INDEX).target());
		Assertions.assertEquals(CAD, rates.get(SECOND_INDEX).target());
		Assertions.assertEquals(HISTORICAL_USD_RATE, rates.get(FIRST_INDEX).currencyRate().rate());
		Assertions.assertEquals(HISTORICAL_CAD_RATE, rates.get(SECOND_INDEX).currencyRate().rate());
	}

	@Test
	void getCurrencyRate() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + API_V1_PATH_SUFFIX);

		final var targetRate = provider.getCurrencyRates(EUR, List.of(USD)).getFirst();

		Assertions.assertEquals(USD, targetRate.target());
		Assertions.assertEquals(LATEST_USD_RATE, targetRate.currencyRate().rate());
	}

	@Test
	void getCurrencyRates() {
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + API_V1_PATH_SUFFIX);

		final var rates = provider.getCurrencyRates(EUR, List.of(USD, CAD));

		Assertions.assertEquals(EXPECTED_TWO_RATES, rates.size());
		Assertions.assertEquals(USD, rates.get(FIRST_INDEX).target());
		Assertions.assertEquals(CAD, rates.get(SECOND_INDEX).target());
		Assertions.assertEquals(LATEST_USD_RATE, rates.get(FIRST_INDEX).currencyRate().rate());
		Assertions.assertEquals(LATEST_CAD_RATE, rates.get(SECOND_INDEX).currencyRate().rate());
	}
}
