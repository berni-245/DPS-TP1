package ar.edu.itba.dps.exchange.domain;

import java.util.Currency;
import java.util.List;

public interface CurrencyRateProvider {
	CurrencyRate getCurrencyRate(Currency from, Currency to);

	List<Currency> getAvailableCurrencies();
}
