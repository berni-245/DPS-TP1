package ar.edu.itba.dps.exchange.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record CurrencyConversionResponse(
		Money source,
		Money target,
		BigDecimal rate,
		Instant timestamp
) {
}
