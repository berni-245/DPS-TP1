package ar.edu.itba.dps.exchange.domain;

import java.time.Instant;
import java.util.Currency;

public record CurrencyConversionResponse(
		Currency fromCurrency,
		Currency toCurrency,
		double convertedAmount,
		double rate,
		Instant timestamp) {
}
