package ar.edu.itba.dps.exchange.domain;

import java.time.Instant;

public record CurrencyConversionResponse(
		Money source,
		Money target,
		CurrencyRate rate,
		Instant timestamp
) {
}
