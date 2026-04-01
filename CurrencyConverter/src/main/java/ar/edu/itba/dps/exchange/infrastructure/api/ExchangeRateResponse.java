package ar.edu.itba.dps.exchange.infrastructure.api;

import java.util.Map;

record ExchangeRateResponse(Map<String, Double> data) {

	double getExchange(String toCurrency) {
		if (this.data == null) {
			throw new IllegalStateException("Missing exchange data");
		}
		final Double rate = this.data.get(toCurrency);
		if (rate == null) {
			throw new IllegalStateException("Missing exchange rate for currency: " + toCurrency);
		}
		return rate;
	}
}
