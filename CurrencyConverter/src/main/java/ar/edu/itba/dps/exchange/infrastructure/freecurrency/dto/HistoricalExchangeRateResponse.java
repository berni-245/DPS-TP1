package ar.edu.itba.dps.exchange.infrastructure.freecurrency.dto;

import java.math.BigDecimal;
import java.util.Map;

public record HistoricalExchangeRateResponse(Map<String, Map<String, BigDecimal>> data) {

	public BigDecimal getExchange(final String dateIso, final String toCurrency) {
		if (this.data == null) {
			throw new IllegalStateException("Missing exchange data");
		}
		final Map<String, BigDecimal> dayRates = this.data.get(dateIso);
		if (dayRates == null) {
			throw new IllegalStateException("Missing rates for date: " + dateIso);
		}
		final BigDecimal rate = dayRates.get(toCurrency);
		if (rate == null) {
			throw new IllegalStateException("Missing exchange rate for currency: " + toCurrency);
		}
		return rate;
	}
}
