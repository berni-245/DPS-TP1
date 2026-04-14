package ar.edu.itba.dps.exchange.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;

class MoneyTest {

	private static final Currency USD = Currency.getInstance("USD");
	private static final Currency EUR = Currency.getInstance("EUR");

	@Test
	void convertMultipliesAmountByRateAndUsesTargetCurrency() {
		final var money = new Money(USD, new BigDecimal("10"));
		final var line = new TargetCurrencyRate(EUR, new CurrencyRate(new BigDecimal("0.9")));

		final var converted = money.convert(line);

		assertThat(converted.currency(), is(EUR));
		assertThat(converted.amount(), comparesEqualTo(new BigDecimal("9")));
	}
}
