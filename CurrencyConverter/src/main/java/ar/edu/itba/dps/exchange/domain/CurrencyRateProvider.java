package ar.edu.itba.dps.exchange.domain;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

public interface CurrencyRateProvider {
	CurrencyRate getCurrencyRate(Currency from, Currency to);

	List<Currency> getAvailableCurrencies();

	CurrencyRate getHistoricalCurrencyRate(Currency from, Currency to, LocalDate date);
}
