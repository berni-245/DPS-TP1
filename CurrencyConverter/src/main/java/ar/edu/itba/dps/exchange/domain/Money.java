package ar.edu.itba.dps.exchange.domain;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(Currency currency, BigDecimal amount) {

	public Money convertedTo(final TargetCurrencyRate targetCurrencyRate) {
		return convertedTo(targetCurrencyRate.target(), targetCurrencyRate.currencyRate());
	}

	public Money convertedTo(final Currency targetCurrency, final CurrencyRate currencyRate) {
		return new Money(targetCurrency, this.amount.multiply(currencyRate.rate()));
	}
}

