package ar.edu.itba.dps.exchange.infrastructure.api;

import java.math.BigDecimal;
import java.util.Map;

record ExchangeRateResponse(Map<String, BigDecimal> data) {

	BigDecimal getExchange(String toCurrency) {
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
