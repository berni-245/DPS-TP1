package ar.edu.itba.dps.exchange.domain;

import java.time.Instant;

public record CurrencyConversionResponse(
		Money source,
		TargetCurrencyRate targetRate,
		Instant timestamp
) {

	public Money target() {
		return this.source.convertedTo(this.targetRate);
	}

	public CurrencyRate rate() {
		return this.targetRate.currencyRate();
	}
}
