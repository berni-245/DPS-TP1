package ar.edu.itba.dps.exchange.domain.service;

import ar.edu.itba.dps.exchange.domain.exception.CurrencyRateConnectionException;
import ar.edu.itba.dps.exchange.domain.exception.CurrencyRateRemoteException;
import ar.edu.itba.dps.exchange.domain.model.ConverterFailure;
import ar.edu.itba.dps.exchange.domain.model.CurrencyConversionResponse;
import ar.edu.itba.dps.exchange.domain.model.CurrencyConversionResult;
import ar.edu.itba.dps.exchange.domain.model.CurrencyConversionsResult;
import ar.edu.itba.dps.exchange.domain.model.CurrencyRate;
import ar.edu.itba.dps.exchange.domain.model.CurrencyRateQueryResult;
import ar.edu.itba.dps.exchange.domain.model.CurrencyRateResponse;
import ar.edu.itba.dps.exchange.domain.model.Money;
import ar.edu.itba.dps.exchange.domain.model.SupportedCurrenciesResult;
import ar.edu.itba.dps.exchange.domain.model.TargetCurrencyRate;
import ar.edu.itba.dps.exchange.domain.port.CurrencyRateProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

	private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
		assertEquals(0, expected.compareTo(actual));
	}

	private static CurrencyConversionResponse unwrapSingle(final CurrencyConversionResult result) {
		return switch (result) {
			case CurrencyConversionResult.Success s -> s.conversion();
			case CurrencyConversionResult.Failure f -> throw new AssertionError(f.reason());
		};
	}

	private static List<CurrencyConversionResponse> unwrapBatch(final CurrencyConversionsResult result) {
		return switch (result) {
			case CurrencyConversionsResult.Success s -> s.conversions();
			case CurrencyConversionsResult.Failure f -> throw new AssertionError(f.reason());
		};
	}

	private static CurrencyRateResponse unwrapRate(final CurrencyRateQueryResult result) {
		return switch (result) {
			case CurrencyRateQueryResult.Success s -> s.rate();
			case CurrencyRateQueryResult.Failure f -> throw new AssertionError(f.reason());
		};
	}

	private static List<Currency> unwrapSupported(final SupportedCurrenciesResult result) {
		return switch (result) {
			case SupportedCurrenciesResult.Success s -> s.currencies();
			case SupportedCurrenciesResult.Failure f -> throw new AssertionError(f.reason());
		};
	}

	@Test
	void convertSingleCurrency() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenReturn(List.of(new TargetCurrencyRate(USD, new CurrencyRate(BigDecimal.ONE))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var result = unwrapSingle(converter.convert(new Money(ARS, AMOUNT_HUNDRED), USD));

		assertEquals(ARS, result.source().currency());
		assertBigDecimalEquals(AMOUNT_HUNDRED_DECIMAL, result.source().amount());
		assertEquals(USD, result.target().currency());
		assertBigDecimalEquals(AMOUNT_HUNDRED_DECIMAL, result.target().amount());
		assertBigDecimalEquals(RATE_PARITY, result.rate().rate());
		assertEquals(FIXED_INSTANT, result.timestamp());
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

		final var results = unwrapBatch(converter.convert(new Money(ARS, AMOUNT_HUNDRED), targets));

		assertEquals(MULTI_TARGET_COUNT, results.size());
		assertEquals(ARS, results.get(FIRST_RESULT_INDEX).source().currency());
		assertEquals(ARS, results.get(SECOND_RESULT_INDEX).source().currency());
		assertBigDecimalEquals(AMOUNT_HUNDRED_DECIMAL, results.get(FIRST_RESULT_INDEX).source().amount());
		assertBigDecimalEquals(AMOUNT_HUNDRED_DECIMAL, results.get(SECOND_RESULT_INDEX).source().amount());
		assertEquals(USD, results.get(FIRST_RESULT_INDEX).target().currency());
		assertEquals(EUR, results.get(SECOND_RESULT_INDEX).target().currency());
		assertBigDecimalEquals(AMOUNT_HUNDRED_DECIMAL, results.get(FIRST_RESULT_INDEX).target().amount());
		assertBigDecimalEquals(EUR_CONVERTED_AMOUNT, results.get(SECOND_RESULT_INDEX).target().amount());
		assertBigDecimalEquals(RATE_PARITY, results.get(FIRST_RESULT_INDEX).rate().rate());
		assertBigDecimalEquals(EUR_SPOT_RATE, results.get(SECOND_RESULT_INDEX).rate().rate());
		assertEquals(FIXED_INSTANT, results.get(FIRST_RESULT_INDEX).timestamp());
		assertEquals(FIXED_INSTANT, results.get(SECOND_RESULT_INDEX).timestamp());
	}

	@Test
	void getCurrencyRateBetweenTwoCurrencies() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenReturn(List.of(new TargetCurrencyRate(USD, new CurrencyRate(BigDecimal.ONE))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var result = unwrapRate(converter.getCurrencyRate(ARS, USD));

		assertEquals(ARS, result.fromCurrency());
		assertEquals(USD, result.toCurrency());
		assertBigDecimalEquals(RATE_PARITY, result.rate().rate());
		assertEquals(FIXED_INSTANT, result.timestamp());
	}

	@Test
	void getSupportedCurrencies() {
		final var provider = mock(CurrencyRateProvider.class);
		final var expectedCurrencies = List.of(USD, ARS);
		when(provider.getAvailableCurrencies()).thenReturn(expectedCurrencies);
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var result = unwrapSupported(converter.getSupportedCurrencies());

		assertEquals(expectedCurrencies, result);
	}

	@Test
	void convertWhenNoTargetCurrenciesReturnsEmptyList() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(List::isEmpty))).thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var results = unwrapBatch(converter.convert(new Money(ARS, BigDecimal.ONE), List.of()));

		assertTrue(results.isEmpty());
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

		final var results = unwrapBatch(converter.convert(new Money(ARS, AMOUNT_HUNDRED), targets,
				HISTORICAL_DATE));

		final var expectedTimestamp = HISTORICAL_DATE.atStartOfDay().toInstant(ZoneOffset.UTC);
		assertEquals(MULTI_TARGET_COUNT, results.size());
		assertEquals(ARS, results.get(FIRST_RESULT_INDEX).source().currency());
		assertEquals(ARS, results.get(SECOND_RESULT_INDEX).source().currency());
		assertBigDecimalEquals(AMOUNT_HUNDRED_DECIMAL, results.get(FIRST_RESULT_INDEX).source().amount());
		assertBigDecimalEquals(AMOUNT_HUNDRED_DECIMAL, results.get(SECOND_RESULT_INDEX).source().amount());
		assertEquals(USD, results.get(FIRST_RESULT_INDEX).target().currency());
		assertEquals(EUR, results.get(SECOND_RESULT_INDEX).target().currency());
		assertBigDecimalEquals(AMOUNT_HUNDRED_DECIMAL, results.get(FIRST_RESULT_INDEX).target().amount());
		assertBigDecimalEquals(HISTORICAL_EUR_CONVERTED_AMOUNT, results.get(SECOND_RESULT_INDEX).target().amount());
		assertBigDecimalEquals(RATE_PARITY, results.get(FIRST_RESULT_INDEX).rate().rate());
		assertBigDecimalEquals(HISTORICAL_EUR_RATE, results.get(SECOND_RESULT_INDEX).rate().rate());
		assertEquals(expectedTimestamp, results.get(FIRST_RESULT_INDEX).timestamp());
		assertEquals(expectedTimestamp, results.get(SECOND_RESULT_INDEX).timestamp());
	}

	@Test
	void convertHistoricalWhenNoTargetCurrenciesReturnsEmptyList() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getHistoricalCurrencyRates(eq(ARS), argThat(List::isEmpty), eq(HISTORICAL_DATE)))
				.thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var results = unwrapBatch(converter.convert(new Money(ARS, AMOUNT_HUNDRED), List.of(),
				HISTORICAL_DATE));

		assertTrue(results.isEmpty());
	}

	@Test
	void getSupportedCurrenciesWhenNoneAvailableReturnsEmptyList() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getAvailableCurrencies()).thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		assertTrue(unwrapSupported(converter.getSupportedCurrencies()).isEmpty());
	}

	@Test
	void convertWithZeroAmountYieldsZeroTargetRegardlessOfRate() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(EUR)))))
				.thenReturn(List.of(new TargetCurrencyRate(EUR, new CurrencyRate(EUR_SPOT_RATE))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var result = unwrapSingle(converter.convert(new Money(ARS, AMOUNT_ZERO), EUR));

		assertBigDecimalEquals(AMOUNT_ZERO, result.source().amount());
		assertBigDecimalEquals(AMOUNT_ZERO, result.target().amount());
		assertBigDecimalEquals(EUR_SPOT_RATE, result.rate().rate());
	}

	@Test
	void convertWithNegativeAmountPreservesSignWithPositiveRate() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(EUR)))))
				.thenReturn(List.of(new TargetCurrencyRate(EUR, new CurrencyRate(EUR_SPOT_RATE))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var result = unwrapSingle(converter.convert(new Money(ARS, AMOUNT_NEGATIVE), EUR));

		assertBigDecimalEquals(AMOUNT_NEGATIVE, result.source().amount());
		assertBigDecimalEquals(NEGATIVE_CONVERTED_WITH_EUR_RATE, result.target().amount());
	}

	@Test
	void convertWithZeroExchangeRateYieldsZeroTarget() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenReturn(List.of(new TargetCurrencyRate(USD, new CurrencyRate(RATE_ZERO))));
		final var clock = Clock.fixed(FIXED_INSTANT, UTC);
		final var converter = new CurrencyConverter(provider, clock);

		final var result = unwrapSingle(converter.convert(new Money(ARS, AMOUNT_HUNDRED), USD));

		assertBigDecimalEquals(AMOUNT_HUNDRED_DECIMAL, result.source().amount());
		assertBigDecimalEquals(AMOUNT_ZERO, result.target().amount());
		assertBigDecimalEquals(RATE_ZERO, result.rate().rate());
	}

	@Test
	void convertSingleCurrencyWhenProviderReturnsNoRatesReturnsFailureWithoutProvider() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var result = converter.convert(new Money(ARS, AMOUNT_HUNDRED), USD);
		assertInstanceOf(CurrencyConversionResult.Failure.class, result);
		assertInstanceOf(ConverterFailure.NoRatesAvailable.class,
				((CurrencyConversionResult.Failure) result).reason());
	}

	@Test
	void getCurrencyRateWhenProviderReturnsNoRatesReturnsFailureWithoutProvider() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var result = converter.getCurrencyRate(ARS, USD);
		assertInstanceOf(CurrencyRateQueryResult.Failure.class, result);
		assertInstanceOf(ConverterFailure.NoRatesAvailable.class,
				((CurrencyRateQueryResult.Failure) result).reason());
	}

	@Test
	void convertWhenProviderThrowsReturnsProviderFailure() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenThrow(new CurrencyRateConnectionException("offline", new RuntimeException()));
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var batch = converter.convert(new Money(ARS, AMOUNT_HUNDRED), List.of(USD));
		assertInstanceOf(CurrencyConversionsResult.Failure.class, batch);
		assertInstanceOf(ConverterFailure.ProviderError.class,
				((CurrencyConversionsResult.Failure) batch).reason());

		final var single = converter.convert(new Money(ARS, AMOUNT_HUNDRED), USD);
		assertInstanceOf(CurrencyConversionResult.Failure.class, single);
		assertInstanceOf(ConverterFailure.ProviderError.class,
				((CurrencyConversionResult.Failure) single).reason());
	}

	@Test
	void getCurrencyRateWhenProviderThrowsReturnsProviderFailure() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD)))))
				.thenThrow(new CurrencyRateRemoteException(503, "retry"));
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var result = converter.getCurrencyRate(ARS, USD);
		assertInstanceOf(CurrencyRateQueryResult.Failure.class, result);
		final var reason = ((CurrencyRateQueryResult.Failure) result).reason();
		assertInstanceOf(ConverterFailure.ProviderError.class, reason);
		assertInstanceOf(CurrencyRateRemoteException.class,
				((ConverterFailure.ProviderError) reason).exception());
	}

	@Test
	void getSupportedCurrenciesWhenProviderThrowsReturnsFailure() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getAvailableCurrencies())
				.thenThrow(new CurrencyRateConnectionException("timeout", new RuntimeException()));
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var result = converter.getSupportedCurrencies();
		assertInstanceOf(SupportedCurrenciesResult.Failure.class, result);
		assertInstanceOf(ConverterFailure.ProviderError.class,
				((SupportedCurrenciesResult.Failure) result).reason());
	}

	@Test
	void convertHistoricalWhenProviderReturnsNoRatesReturnsFailure() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getHistoricalCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD))),
				eq(HISTORICAL_DATE))).thenReturn(List.of());
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var result = converter.convert(new Money(ARS, AMOUNT_HUNDRED), List.of(USD), HISTORICAL_DATE);
		assertInstanceOf(CurrencyConversionsResult.Failure.class, result);
		assertInstanceOf(ConverterFailure.NoRatesAvailable.class,
				((CurrencyConversionsResult.Failure) result).reason());
	}

	@Test
	void convertHistoricalWhenProviderThrowsReturnsProviderFailure() {
		final var provider = mock(CurrencyRateProvider.class);
		when(provider.getHistoricalCurrencyRates(eq(ARS), argThat(l -> l.equals(List.of(USD, EUR))),
				eq(HISTORICAL_DATE)))
				.thenThrow(new CurrencyRateConnectionException("offline", new RuntimeException()));
		final var converter = new CurrencyConverter(provider, Clock.systemUTC());

		final var result = converter.convert(new Money(ARS, AMOUNT_HUNDRED), List.of(USD, EUR),
				HISTORICAL_DATE);
		assertInstanceOf(CurrencyConversionsResult.Failure.class, result);
		assertInstanceOf(ConverterFailure.ProviderError.class,
				((CurrencyConversionsResult.Failure) result).reason());
	}
}
