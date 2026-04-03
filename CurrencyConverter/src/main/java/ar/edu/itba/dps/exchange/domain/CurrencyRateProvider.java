package ar.edu.itba.dps.exchange.domain;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

public interface CurrencyRateProvider {
	default CurrencyRate getCurrencyRate(Currency from, Currency to) {
		return getCurrencyRates(from, List.of(to)).getFirst();
	}

	List<CurrencyRate> getCurrencyRates(Currency from, List<Currency> to);

	List<Currency> getAvailableCurrencies();

	CurrencyRate getHistoricalCurrencyRate(Currency from, Currency to, LocalDate date);
}
