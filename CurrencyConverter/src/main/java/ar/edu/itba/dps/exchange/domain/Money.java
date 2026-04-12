package ar.edu.itba.dps.exchange.domain;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(Currency currency, BigDecimal amount) {

	public Money(final Currency currency, final double amount) {
		this(currency, BigDecimal.valueOf(amount));
	}
}

