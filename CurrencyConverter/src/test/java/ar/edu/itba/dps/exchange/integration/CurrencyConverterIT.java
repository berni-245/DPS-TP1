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

	private static final Currency EUR = Currency.getInstance("EUR");
	private static final Currency USD = Currency.getInstance("USD");

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(options().dynamicPort().usingFilesUnderClasspath("wiremock"))
			.build();

	@Test
	void convertCurrentRatesIncludesTimestampSourceTargetAndQuotedRate() {
		final var fixedInstant = Instant.parse("2026-04-01T12:00:00Z");
		final var clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var converter = new CurrencyConverter(provider, clock);

		final var response = converter.convert(new Money(EUR, BigDecimal.valueOf(100)), USD);

		assertThat(response.source().currency(), is(EUR));
		assertThat(response.source().amount(), comparesEqualTo(new BigDecimal("100")));
		assertThat(response.target().currency(), is(USD));
		assertThat(response.target().amount(), comparesEqualTo(new BigDecimal("108.47")));
		assertThat(response.rate().rate(), comparesEqualTo(new BigDecimal("1.0847")));
		assertThat(response.timestamp(), is(fixedInstant));
	}

	@Test
	void convertHistoricalRatesUsesDateStartAsTimestamp() {
		final var clock = Clock.systemUTC();
		final var httpClient = new UnirestHttpClient();
		final var provider = new FreeCurrencyApiProvider(httpClient, wireMock.baseUrl() + "/v1/");
		final var converter = new CurrencyConverter(provider, clock);
		final var date = LocalDate.of(2022, 1, 1);
		final var expectedTs = date.atStartOfDay().toInstant(ZoneOffset.UTC);

		final var response = converter.convert(new Money(EUR, BigDecimal.valueOf(100)), List.of(USD), date).getFirst();

		assertThat(response.target().amount(), comparesEqualTo(new BigDecimal("113.47")));
		assertThat(response.rate().rate(), comparesEqualTo(new BigDecimal("1.1347")));
		assertThat(response.timestamp(), is(expectedTs));
	}
}
