package ar.edu.itba.dps.exchange.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyTest {

	private static final String USD_CODE = "USD";
	private static final String EUR_CODE = "EUR";
	private static final Currency USD = Currency.getInstance(USD_CODE);
	private static final Currency EUR = Currency.getInstance(EUR_CODE);
	private static final String SOURCE_AMOUNT_PLAIN = "10";
	private static final String CONVERSION_RATE_PLAIN = "0.9";
	private static final String EXPECTED_CONVERTED_AMOUNT_PLAIN = "9";
	private static final BigDecimal SOURCE_AMOUNT = new BigDecimal(SOURCE_AMOUNT_PLAIN);
	private static final BigDecimal CONVERSION_RATE = new BigDecimal(CONVERSION_RATE_PLAIN);
	private static final BigDecimal EXPECTED_CONVERTED_AMOUNT = new BigDecimal(EXPECTED_CONVERTED_AMOUNT_PLAIN);

	@Test
	void convertMultipliesAmountByRateAndUsesTargetCurrency() {
		final var money = new Money(USD, SOURCE_AMOUNT);
		final var line = new TargetCurrencyRate(EUR, new CurrencyRate(CONVERSION_RATE));

		final var converted = money.convert(line);

		assertEquals(EUR, converted.currency());
		assertEquals(0, EXPECTED_CONVERTED_AMOUNT.compareTo(converted.amount()));
	}
}
