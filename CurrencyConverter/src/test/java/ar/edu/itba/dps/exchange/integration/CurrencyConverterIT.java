package ar.edu.itba.dps.exchange.integration;

import ar.edu.itba.dps.exchange.domain.model.Money;
import ar.edu.itba.dps.exchange.domain.service.CurrencyConverter;
import ar.edu.itba.dps.exchange.infrastructure.freecurrency.FreeCurrencyApiProvider;
import ar.edu.itba.dps.exchange.infrastructure.http.UnirestHttpClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;

class CurrencyConverterIT {

	private static final String EUR_CODE = "EUR";
	private static final String USD_CODE = "USD";
	private static final String FIXED_INSTANT_STRING = "2026-04-01T12:00:00Z";
	private static final String UTC_ZONE = "UTC";
	private static final String API_V1_PATH_SUFFIX = "/v1/";
	private static final String WIREMOCK_FILES_CLASSPATH = "wiremock";
	private static final Currency EUR = Currency.getInstance(EUR_CODE);
	private static final Currency USD = Currency.getInstance(USD_CODE);
	private static final Instant FIXED_INSTANT = Instant.parse(FIXED_INSTANT_STRING);
	private static final ZoneId UTC = ZoneId.of(UTC_ZONE);
	private static final LocalDate HISTORICAL_DATE = LocalDate.of(2022, 1, 1);
	private static final String SOURCE_AMOUNT_PLAIN = "100";
	private static final String EXPECTED_USD_AMOUNT_PLAIN = "108.47";
	private static final String EXPECTED_SPOT_RATE_PLAIN = "1.0847";
	private static final String HISTORICAL_TARGET_AMOUNT_PLAIN = "113.47";
	private static final String HISTORICAL_RATE_PLAIN = "1.1347";
	private static final BigDecimal SOURCE_AMOUNT = new BigDecimal(SOURCE_AMOUNT_PLAIN);
	private static final BigDecimal EXPECTED_USD_AMOUNT = new BigDecimal(EXPECTED_USD_AMOUNT_PLAIN);
	private static final BigDecimal EXPECTED_SPOT_RATE = new BigDecimal(EXPECTED_SPOT_RATE_PLAIN);
	private static final BigDecimal HISTORICAL_TARGET_AMOUNT = new BigDecimal(HISTORICAL_TARGET_AMOUNT_PLAIN);
	private static final BigDecimal HISTORICAL_RATE = new BigDecimal(HISTORICAL_RATE_PLAIN);
	private static final int FIRST_RESULT_INDEX = 0;

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(options().dynamicPort().usingFilesUnderClasspath(WIREMOCK_FILES_CLASSPATH))
			.build();

	@Test
	void convertCurrentRatesIncludesTimestampSourceTargetAndQuotedRate() {
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + API_V1_PATH_SUFFIX);
		final var converter = new CurrencyConverter(provider, clock);

		final var response = converter.convert(new Money(EUR, SOURCE_AMOUNT), USD);

		assertThat(response.source().currency(), is(EUR));
		assertThat(response.source().amount(), comparesEqualTo(SOURCE_AMOUNT));
		assertThat(response.target().currency(), is(USD));
		assertThat(response.target().amount(), comparesEqualTo(EXPECTED_USD_AMOUNT));
		assertThat(response.rate().rate(), comparesEqualTo(EXPECTED_SPOT_RATE));
		assertThat(response.timestamp(), is(FIXED_INSTANT));
	}

	@Test
	void convertHistoricalRatesUsesDateStartAsTimestamp() {
		final var clock = Clock.systemUTC();
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + API_V1_PATH_SUFFIX);
		final var converter = new CurrencyConverter(provider, clock);
		final var expectedTs = HISTORICAL_DATE.atStartOfDay().toInstant(ZoneOffset.UTC);

		final var response = converter.convert(new Money(EUR, SOURCE_AMOUNT), List.of(USD), HISTORICAL_DATE)
				.get(FIRST_RESULT_INDEX);

		assertThat(response.target().amount(), comparesEqualTo(HISTORICAL_TARGET_AMOUNT));
		assertThat(response.rate().rate(), comparesEqualTo(HISTORICAL_RATE));
		assertThat(response.timestamp(), is(expectedTs));
	}
}
