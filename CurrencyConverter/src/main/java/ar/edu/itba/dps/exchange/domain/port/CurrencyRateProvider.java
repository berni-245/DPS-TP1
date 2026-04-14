package ar.edu.itba.dps.exchange.domain.port;

import ar.edu.itba.dps.exchange.domain.model.TargetCurrencyRate;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

public interface CurrencyRateProvider {
	List<TargetCurrencyRate> getCurrencyRates(Currency from, List<Currency> to);

	List<Currency> getAvailableCurrencies();

	List<TargetCurrencyRate> getHistoricalCurrencyRates(Currency from, List<Currency> to, LocalDate date);
}
