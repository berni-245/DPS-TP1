package ar.edu.itba.dps.exchange.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;

public interface CurrencyRateProvider {
	default CurrencyRate getCurrencyRate(Currency from, Currency to) {
		return getCurrencyRates(from, List.of(to)).getFirst();
	}

	List<CurrencyRate> getCurrencyRates(Currency from, List<Currency> to);

	List<Currency> getAvailableCurrencies();

	LocalTime getDailyTimeOfRateMeasurement();

	default CurrencyRate getHistoricalCurrencyRate(Currency from, Currency to, LocalDate date) {
		return getHistoricalCurrencyRates(from, List.of(to), date).getFirst();
	}

	List<CurrencyRate> getHistoricalCurrencyRates(Currency from, List<Currency> to, LocalDate date);
}
