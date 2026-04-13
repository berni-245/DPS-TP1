package ar.edu.itba.dps.exchange;

import ar.edu.itba.dps.exchange.domain.CurrencyConverter;
import ar.edu.itba.dps.exchange.domain.Money;
import ar.edu.itba.dps.exchange.domain.CurrencyRate;
import ar.edu.itba.dps.exchange.domain.TargetCurrencyRate;
import ar.edu.itba.dps.exchange.domain.CurrencyRateProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.Currency;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrencyConverterTest {

	public static final Currency USD = Currency.getInstance("USD");
	public static final Currency ARS = Currency.getInstance("ARS");
	public static final Currency EUR = Currency.getInstance("EUR");

	@Test
	void testConvert() {
		// Given
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(ARS, List.of(USD)))
				.thenReturn(List.of(new TargetCurrencyRate(USD, new CurrencyRate(BigDecimal.ONE))));
		final var fixedInstant = Instant.parse("2026-04-01T10:00:00Z");
		final var clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));
		final var converter = new CurrencyConverter(provider, clock);

		// When
		final var result = converter.convert(new Money(ARS, BigDecimal.valueOf(100)), USD);

		// Then
		assertThat(result.source().currency(), is(ARS));
		assertThat(result.source().amount(), comparesEqualTo(BigDecimal.valueOf(100.0)));
		assertThat(result.target().currency(), is(USD));
		assertThat(result.target().amount(), comparesEqualTo(BigDecimal.valueOf(100.0)));
		assertThat(result.rate(), comparesEqualTo(BigDecimal.valueOf(1.0)));
		assertThat(result.timestamp(), is(fixedInstant));
	}

	@Test
	void testConvertToMultipleCurrencies() {
		// Given
		final var provider = mock(CurrencyRateProvider.class);
		final var targets = List.of(USD, EUR);
		when(provider.getCurrencyRates(ARS, targets))
				.thenReturn(List.of(
						new TargetCurrencyRate(USD, new CurrencyRate(BigDecimal.ONE)),
						new TargetCurrencyRate(EUR, new CurrencyRate(BigDecimal.valueOf(1.2)))));
		final var fixedInstant = Instant.parse("2026-04-01T10:00:00Z");
		final var clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));
		final var converter = new CurrencyConverter(provider, clock);

		// When
		final var results = converter.convert(new Money(ARS, BigDecimal.valueOf(100)), targets);

		// Then
		assertThat(results, hasSize(2));
		assertThat(results.get(0).source().currency(), is(ARS));
		assertThat(results.get(1).source().currency(), is(ARS));
		assertThat(results.get(0).source().amount(), comparesEqualTo(BigDecimal.valueOf(100.0)));
		assertThat(results.get(1).source().amount(), comparesEqualTo(BigDecimal.valueOf(100.0)));
		assertThat(results.get(0).target().currency(), is(USD));
		assertThat(results.get(1).target().currency(), is(EUR));
		assertThat(results.get(0).target().amount(), comparesEqualTo(BigDecimal.valueOf(100.0)));
		assertThat(results.get(1).target().amount(), comparesEqualTo(BigDecimal.valueOf(120.0)));
		assertThat(results.get(0).rate(), comparesEqualTo(BigDecimal.valueOf(1.0)));
		assertThat(results.get(1).rate(), comparesEqualTo(BigDecimal.valueOf(1.2)));
		assertThat(results.get(0).timestamp(), is(fixedInstant));
		assertThat(results.get(1).timestamp(), is(fixedInstant));
	}

	@Test
	void testGetExchangeRate() {
		// Given
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(ARS, List.of(USD)))
				.thenReturn(List.of(new TargetCurrencyRate(USD, new CurrencyRate(BigDecimal.ONE))));
		final var fixedInstant = Instant.parse("2026-04-01T10:00:00Z");
		final var clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));
		final var converter = new CurrencyConverter(provider, clock);

		// When
		final var result = converter.getCurrencyRate(ARS, USD);

		// Then
		assertThat(result.fromCurrency(), is(ARS));
		assertThat(result.toCurrency(), is(USD));
		assertThat(result.rate().rate(), comparesEqualTo(BigDecimal.valueOf(1.0)));
		assertThat(result.timestamp(), is(fixedInstant));
	}

	@Test
	void testGetSupportedCurrencies() {
		// Given
		final var provider = mock(CurrencyRateProvider.class);
		final var expectedCurrencies = List.of(USD, ARS);
		when(provider.getAvailableCurrencies()).thenReturn(expectedCurrencies);
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		// When
		final var result = converter.getSupportedCurrencies();

		// Then
		assertThat(result, is(expectedCurrencies));
	}

	@Test
	void testConvertToMultipleCurrenciesOnDate() {
		// Given
		final var provider = mock(CurrencyRateProvider.class);
		final var targets = List.of(USD, EUR);
		final var date = LocalDate.of(2022, 1, 1);
		final var measurementTime = LocalTime.of(23, 59, 59);
		when(provider.getHistoricalCurrencyRates(ARS, targets, date))
				.thenReturn(List.of(
						new TargetCurrencyRate(USD, new CurrencyRate(BigDecimal.ONE)),
						new TargetCurrencyRate(EUR, new CurrencyRate(BigDecimal.valueOf(0.85)))));
		when(provider.getDailyTimeOfRateMeasurement()).thenReturn(measurementTime);
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		// When
		final var results = converter.convert(new Money(ARS, BigDecimal.valueOf(100)), targets, date);

		// Then
		final var expectedTimestamp = date.atTime(measurementTime).toInstant(ZoneOffset.UTC);
		assertThat(results, hasSize(2));
		assertThat(results.get(0).source().currency(), is(ARS));
		assertThat(results.get(1).source().currency(), is(ARS));
		assertThat(results.get(0).source().amount(), comparesEqualTo(BigDecimal.valueOf(100.0)));
		assertThat(results.get(1).source().amount(), comparesEqualTo(BigDecimal.valueOf(100.0)));
		assertThat(results.get(0).target().currency(), is(USD));
		assertThat(results.get(1).target().currency(), is(EUR));
		assertThat(results.get(0).target().amount(), comparesEqualTo(BigDecimal.valueOf(100.0)));
		assertThat(results.get(1).target().amount(), comparesEqualTo(BigDecimal.valueOf(85.0)));
		assertThat(results.get(0).rate(), comparesEqualTo(BigDecimal.valueOf(1.0)));
		assertThat(results.get(1).rate(), comparesEqualTo(BigDecimal.valueOf(0.85)));
		assertThat(results.get(0).timestamp(), is(expectedTimestamp));
		assertThat(results.get(1).timestamp(), is(expectedTimestamp));
	}
}
