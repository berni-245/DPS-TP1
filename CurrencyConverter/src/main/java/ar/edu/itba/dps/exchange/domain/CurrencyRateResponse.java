package ar.edu.itba.dps.exchange.domain;

import java.time.Instant;
import java.util.Currency;

public record CurrencyRateResponse(
		Currency fromCurrency,
		Currency toCurrency,
		CurrencyRate rate,
		Instant timestamp) {
}
