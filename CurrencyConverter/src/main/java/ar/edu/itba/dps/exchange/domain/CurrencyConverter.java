package ar.edu.itba.dps.exchange.domain;

import lombok.RequiredArgsConstructor;

import java.util.Currency;

@RequiredArgsConstructor
public class CurrencyConverter {

	private final CurrencyRateProvider currencyRateProvider;

	public double convert(Currency from, Currency to, double amount) {
		final var rate = this.currencyRateProvider.getCurrencyRate(from, to).rate();
		return amount * rate;
	}

}
