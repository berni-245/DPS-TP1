package ar.edu.itba.dps.exchange.domain.service;

import ar.edu.itba.dps.exchange.domain.model.CurrencyRate;
import ar.edu.itba.dps.exchange.domain.model.Money;
import ar.edu.itba.dps.exchange.domain.model.TargetCurrencyRate;
import ar.edu.itba.dps.exchange.domain.port.CurrencyRateProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.Currency;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrencyConverterTest {

	private static final String USD_CODE = "USD";
	private static final String ARS_CODE = "ARS";
	private static final String EUR_CODE = "EUR";

	public static final Currency USD = Currency.getInstance(USD_CODE);
	public static final Currency ARS = Currency.getInstance(ARS_CODE);
	public static final Currency EUR = Currency.getInstance(EUR_CODE);

	private static final String FIXED_INSTANT_STRING = "2026-04-01T10:00:00Z";
	private static final String UTC_ZONE = "UTC";
	private static final Instant FIXED_INSTANT = Instant.parse(FIXED_INSTANT_STRING);
	private static final ZoneId UTC = ZoneId.of(UTC_ZONE);
	private static final LocalDate HISTORICAL_DATE = LocalDate.of(2022, 1, 1);
	private static final BigDecimal AMOUNT_HUNDRED = BigDecimal.valueOf(100);
	private static final BigDecimal AMOUNT_HUNDRED_DECIMAL = BigDecimal.valueOf(100.0);
	private static final BigDecimal RATE_PARITY = BigDecimal.valueOf(1.0);
	private static final BigDecimal EUR_SPOT_RATE = BigDecimal.valueOf(1.2);
	private static final BigDecimal EUR_CONVERTED_AMOUNT = BigDecimal.valueOf(120.0);
	private static final BigDecimal HISTORICAL_EUR_RATE = BigDecimal.valueOf(0.85);
	private static final BigDecimal HISTORICAL_EUR_CONVERTED_AMOUNT = BigDecimal.valueOf(85.0);
	private static final int MULTI_TARGET_COUNT = 2;
	private static final int FIRST_RESULT_INDEX = 0;
	private static final int SECOND_RESULT_INDEX = 1;
	private static final BigDecimal AMOUNT_ZERO = BigDecimal.ZERO;
	private static final BigDecimal AMOUNT_NEGATIVE = BigDecimal.valueOf(-100);
	private static final BigDecimal NEGATIVE_CONVERTED_WITH_EUR_RATE = BigDecimal.valueOf(-120.0);
	private static final BigDecimal RATE_ZERO = BigDecimal.ZERO;

	@Test
	void convertSingleCurrency() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenReturn(List.of(new TargetCurrencyRate(USD, new CurrencyRate(BigDecimal.ONE))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var result = converter.convert(new Money(ARS, AMOUNT_HUNDRED), USD);

		assertThat(result.source().currency(), is(ARS));
		assertThat(result.source().amount(), comparesEqualTo(AMOUNT_HUNDRED_DECIMAL));
		assertThat(result.target().currency(), is(USD));
		assertThat(result.target().amount(), comparesEqualTo(AMOUNT_HUNDRED_DECIMAL));
		assertThat(result.rate().rate(), comparesEqualTo(RATE_PARITY));
		assertThat(result.timestamp(), is(FIXED_INSTANT));
	}

	@Test
	void convertToMultipleCurrencies() {
		final var provider = mock(CurrencyRateProvider.class);
		final var targets = List.of(USD, EUR);
		when(provider.getCurrencyRates(ARS, targets))
				.thenReturn(List.of(
						new TargetCurrencyRate(USD, new CurrencyRate(BigDecimal.ONE)),
						new TargetCurrencyRate(EUR, new CurrencyRate(EUR_SPOT_RATE))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var results = converter.convert(new Money(ARS, AMOUNT_HUNDRED), targets);

		assertThat(results, hasSize(MULTI_TARGET_COUNT));
		assertThat(results.get(FIRST_RESULT_INDEX).source().currency(), is(ARS));
		assertThat(results.get(SECOND_RESULT_INDEX).source().currency(), is(ARS));
		assertThat(results.get(FIRST_RESULT_INDEX).source().amount(), comparesEqualTo(AMOUNT_HUNDRED_DECIMAL));
		assertThat(results.get(SECOND_RESULT_INDEX).source().amount(), comparesEqualTo(AMOUNT_HUNDRED_DECIMAL));
		assertThat(results.get(FIRST_RESULT_INDEX).target().currency(), is(USD));
		assertThat(results.get(SECOND_RESULT_INDEX).target().currency(), is(EUR));
		assertThat(results.get(FIRST_RESULT_INDEX).target().amount(), comparesEqualTo(AMOUNT_HUNDRED_DECIMAL));
		assertThat(results.get(SECOND_RESULT_INDEX).target().amount(), comparesEqualTo(EUR_CONVERTED_AMOUNT));
		assertThat(results.get(FIRST_RESULT_INDEX).rate().rate(), comparesEqualTo(RATE_PARITY));
		assertThat(results.get(SECOND_RESULT_INDEX).rate().rate(), comparesEqualTo(EUR_SPOT_RATE));
		assertThat(results.get(FIRST_RESULT_INDEX).timestamp(), is(FIXED_INSTANT));
		assertThat(results.get(SECOND_RESULT_INDEX).timestamp(), is(FIXED_INSTANT));
	}

	@Test
	void getCurrencyRateBetweenTwoCurrencies() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenReturn(List.of(new TargetCurrencyRate(USD, new CurrencyRate(BigDecimal.ONE))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var result = converter.getCurrencyRate(ARS, USD);

		assertThat(result.fromCurrency(), is(ARS));
		assertThat(result.toCurrency(), is(USD));
		assertThat(result.rate().rate(), comparesEqualTo(RATE_PARITY));
		assertThat(result.timestamp(), is(FIXED_INSTANT));
	}

	@Test
	void getSupportedCurrencies() {
		final var provider = mock(CurrencyRateProvider.class);
		final var expectedCurrencies = List.of(USD, ARS);
		when(provider.getAvailableCurrencies()).thenReturn(expectedCurrencies);
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var result = converter.getSupportedCurrencies();

		assertThat(result, is(expectedCurrencies));
	}

	@Test
	void convertWhenNoTargetCurrenciesReturnsEmptyList() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(List::isEmpty))).thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var results = converter.convert(new Money(ARS, BigDecimal.ONE), List.of());

		assertThat(results, empty());
	}

	@Test
	void convertToMultipleCurrenciesOnDate() {
		final var provider = mock(CurrencyRateProvider.class);
		final var targets = List.of(USD, EUR);
		when(provider.getHistoricalCurrencyRates(ARS, targets, HISTORICAL_DATE))
				.thenReturn(List.of(
						new TargetCurrencyRate(USD, new CurrencyRate(BigDecimal.ONE)),
						new TargetCurrencyRate(EUR, new CurrencyRate(HISTORICAL_EUR_RATE))));
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var results = converter.convert(new Money(ARS, AMOUNT_HUNDRED), targets, HISTORICAL_DATE);

		final var expectedTimestamp = HISTORICAL_DATE.atStartOfDay().toInstant(ZoneOffset.UTC);
		assertThat(results, hasSize(MULTI_TARGET_COUNT));
		assertThat(results.get(FIRST_RESULT_INDEX).source().currency(), is(ARS));
		assertThat(results.get(SECOND_RESULT_INDEX).source().currency(), is(ARS));
		assertThat(results.get(FIRST_RESULT_INDEX).source().amount(), comparesEqualTo(AMOUNT_HUNDRED_DECIMAL));
		assertThat(results.get(SECOND_RESULT_INDEX).source().amount(), comparesEqualTo(AMOUNT_HUNDRED_DECIMAL));
		assertThat(results.get(FIRST_RESULT_INDEX).target().currency(), is(USD));
		assertThat(results.get(SECOND_RESULT_INDEX).target().currency(), is(EUR));
		assertThat(results.get(FIRST_RESULT_INDEX).target().amount(), comparesEqualTo(AMOUNT_HUNDRED_DECIMAL));
		assertThat(results.get(SECOND_RESULT_INDEX).target().amount(), comparesEqualTo(HISTORICAL_EUR_CONVERTED_AMOUNT));
		assertThat(results.get(FIRST_RESULT_INDEX).rate().rate(), comparesEqualTo(RATE_PARITY));
		assertThat(results.get(SECOND_RESULT_INDEX).rate().rate(), comparesEqualTo(HISTORICAL_EUR_RATE));
		assertThat(results.get(FIRST_RESULT_INDEX).timestamp(), is(expectedTimestamp));
		assertThat(results.get(SECOND_RESULT_INDEX).timestamp(), is(expectedTimestamp));
	}

	@Test
	void convertHistoricalWhenNoTargetCurrenciesReturnsEmptyList() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getHistoricalCurrencyRates(eq(ARS), argThat(List::isEmpty), eq(HISTORICAL_DATE)))
				.thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var results = converter.convert(new Money(ARS, AMOUNT_HUNDRED), List.of(), HISTORICAL_DATE);

		assertThat(results, empty());
	}

	@Test
	void getSupportedCurrenciesWhenNoneAvailableReturnsEmptyList() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getAvailableCurrencies()).thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		assertThat(converter.getSupportedCurrencies(), empty());
	}

	@Test
	void convertWithZeroAmountYieldsZeroTargetRegardlessOfRate() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(EUR)))))
				.thenReturn(List.of(new TargetCurrencyRate(EUR, new CurrencyRate(EUR_SPOT_RATE))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var result = converter.convert(new Money(ARS, AMOUNT_ZERO), EUR);

		assertThat(result.source().amount(), comparesEqualTo(AMOUNT_ZERO));
		assertThat(result.target().amount(), comparesEqualTo(AMOUNT_ZERO));
		assertThat(result.rate().rate(), comparesEqualTo(EUR_SPOT_RATE));
	}

	@Test
	void convertWithNegativeAmountPreservesSignWithPositiveRate() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(EUR)))))
				.thenReturn(List.of(new TargetCurrencyRate(EUR, new CurrencyRate(EUR_SPOT_RATE))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var result = converter.convert(new Money(ARS, AMOUNT_NEGATIVE), EUR);

		assertThat(result.source().amount(), comparesEqualTo(AMOUNT_NEGATIVE));
		assertThat(result.target().amount(), comparesEqualTo(NEGATIVE_CONVERTED_WITH_EUR_RATE));
	}

	@Test
	void convertWithZeroExchangeRateYieldsZeroTarget() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenReturn(List.of(new TargetCurrencyRate(USD, new CurrencyRate(RATE_ZERO))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var result = converter.convert(new Money(ARS, AMOUNT_HUNDRED), USD);

		assertThat(result.source().amount(), comparesEqualTo(AMOUNT_HUNDRED_DECIMAL));
		assertThat(result.target().amount(), comparesEqualTo(AMOUNT_ZERO));
		assertThat(result.rate().rate(), comparesEqualTo(RATE_ZERO));
	}

	@Test
	void convertSingleCurrencyWhenProviderReturnsNoRatesThrows() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		assertThrows(NoSuchElementException.class,
				() -> converter.convert(new Money(ARS, AMOUNT_HUNDRED), USD));
	}

	@Test
	void getCurrencyRateWhenProviderReturnsNoRatesThrows() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		assertThrows(NoSuchElementException.class, () -> converter.getCurrencyRate(ARS, USD));
	}
}
