package ar.edu.itba.dps.exchange.infrastructure.api;

import java.util.Map;

record HistoricalExchangeRateResponse(Map<String, Map<String, Double>> data) {

	double getExchange(String dateIso, String toCurrency) {
		if (this.data == null) {
			throw new IllegalStateException("Missing exchange data");
		}
		final Map<String, Double> dayRates = this.data.get(dateIso);
		if (dayRates == null) {
			throw new IllegalStateException("Missing rates for date: " + dateIso);
		}
		final Double rate = dayRates.get(toCurrency);
		if (rate == null) {
			throw new IllegalStateException("Missing exchange rate for currency: " + toCurrency);
		}
		return rate;
	}
}
