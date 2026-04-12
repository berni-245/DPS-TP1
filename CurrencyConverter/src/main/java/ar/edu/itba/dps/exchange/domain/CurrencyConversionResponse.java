package ar.edu.itba.dps.exchange.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record CurrencyConversionResponse(
		Money source,
		Money target,
		BigDecimal rate,
		Instant timestamp
) {
	public CurrencyConversionResponse(final Money source, final Money target, final double rate,
	                                  final Instant timestamp) {
		this(source, target, BigDecimal.valueOf(rate), timestamp);
	}
}
