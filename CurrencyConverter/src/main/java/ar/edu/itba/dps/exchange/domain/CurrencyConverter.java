package ar.edu.itba.dps.exchange.domain;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class CurrencyConverter {

	private final CurrencyRateProvider currencyRateProvider;
	private final Clock clock;

	public CurrencyConversionResponse convert(Currency from, Currency to, double amount) {
		return convert(from, List.of(to), amount).getFirst();
	}

	public List<CurrencyConversionResponse> convert(Currency from, List<Currency> to, double amount) {
		final var currencyRates = this.currencyRateProvider.getCurrencyRates(from, to);
		final var timestamp = Instant.now(clock);
		return IntStream.range(0, to.size())
				.mapToObj(i -> {
					final double r = currencyRates.get(i).rate();
					return new CurrencyConversionResponse(from, to.get(i), amount * r, r, timestamp);
				})
				.toList();
	}

	public CurrencyRateQuote getCurrencyRate(Currency from, Currency to) {
		final var currencyRates = this.currencyRateProvider.getCurrencyRates(from, List.of(to));
		final var currencyRate = currencyRates.getFirst();
		return new CurrencyRateQuote(from, to, currencyRate.rate(), Instant.now(clock));
	}

	public List<Currency> getSupportedCurrencies() {
		return this.currencyRateProvider.getAvailableCurrencies();
	}

	public List<CurrencyConversionResponse> convert(Currency from, List<Currency> to, double amount, LocalDate date) {
		final var currencyRates = this.currencyRateProvider.getHistoricalCurrencyRates(from, to, date);
		final var timestamp = date.atTime(this.currencyRateProvider.getDailyTimeOfRateMeasurement()).toInstant(ZoneOffset.UTC);
		return IntStream.range(0, to.size())
				.mapToObj(i -> {
					final double r = currencyRates.get(i).rate();
					return new CurrencyConversionResponse(from, to.get(i), amount * r, r, timestamp);
				})
				.toList();
	}
}
