package ar.edu.itba.dps.exchange.domain;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

@RequiredArgsConstructor
public class CurrencyConverter {

	private final CurrencyRateProvider currencyRateProvider;
	private final Clock clock;

	public CurrencyConversionResponse convert(Currency from, Currency to, double amount) {
		final var rate = this.currencyRateProvider.getCurrencyRate(from, to).rate();
		return new CurrencyConversionResponse(amount * rate, Instant.now(clock));
	}

	public List<Currency> getSupportedCurrencies() {
		return this.currencyRateProvider.getAvailableCurrencies();
	}

}
