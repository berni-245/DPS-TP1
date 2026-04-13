package ar.edu.itba.dps.exchange.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;

public interface CurrencyRateProvider {
	List<TargetCurrencyRate> getCurrencyRates(Currency from, List<Currency> to);

	List<Currency> getAvailableCurrencies();

	LocalTime getDailyTimeOfRateMeasurement();

	List<TargetCurrencyRate> getHistoricalCurrencyRates(Currency from, List<Currency> to, LocalDate date);
}
