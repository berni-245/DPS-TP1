package ar.edu.itba.dps.exchange.infrastructure.freecurrency.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateResponse(Map<String, BigDecimal> data) {

	public BigDecimal getExchange(final String toCurrency) {
		if (this.data == null) {
			throw new IllegalStateException("Missing exchange data");
		}
		final BigDecimal rate = this.data.get(toCurrency);
		if (rate == null) {
			throw new IllegalStateException("Missing exchange rate for currency: " + toCurrency);
		}
		return rate;
	}
}
