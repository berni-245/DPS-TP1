package ar.edu.itba.dps.exchange.domain;

import lombok.RequiredArgsConstructor;

import java.time.*;
import java.util.Currency;
import java.util.List;

@RequiredArgsConstructor
public class CurrencyConverter {

	private final CurrencyRateProvider currencyRateProvider;
	private final Clock clock;

	public CurrencyConversionResponse convert(Currency from, Currency to, double amount) {
		final var rate = this.currencyRateProvider.getCurrencyRate(from, to).rate();
		return new CurrencyConversionResponse(amount * rate, Instant.now(clock));
	}

	public List<CurrencyConversionResponse> convert(Currency from, List<Currency> to, double amount) {
		final var currencyRates = this.currencyRateProvider.getCurrencyRates(from, to);
		final var timestamp = Instant.now(clock);
		return currencyRates.stream().map(currencyRate ->
				new CurrencyConversionResponse(amount * currencyRate.rate(), timestamp)
		).toList();
	}

	public CurrencyConversionResponse getCurrencyRate(Currency from, Currency to) {
		return convert(from, to, 1);
	}

	public List<Currency> getSupportedCurrencies() {
		return this.currencyRateProvider.getAvailableCurrencies();
	}

	public List<CurrencyConversionResponse> convert(Currency from, List<Currency> to, double amount, LocalDate date) {
		final var currencyRates = this.currencyRateProvider.getHistoricalCurrencyRates(from, to, date);
		final var timestamp = date.atTime(this.currencyRateProvider.getDailyTimeOfRateMeasurement()).toInstant(ZoneOffset.UTC);
		return currencyRates.stream().map(currencyRate ->
				new CurrencyConversionResponse(amount * currencyRate.rate(), timestamp)
		).toList();
	}
}
