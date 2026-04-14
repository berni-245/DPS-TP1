package ar.edu.itba.dps.exchange.domain.model;

import java.time.Instant;
import java.util.Currency;

public record CurrencyRateResponse(
		Currency fromCurrency,
		Currency toCurrency,
		CurrencyRate rate,
		Instant timestamp) {
}
