package ar.edu.itba.dps.exchange.domain;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;

@RequiredArgsConstructor
public class CurrencyConverter {

	private final CurrencyRateProvider currencyRateProvider;
	private final Clock clock;

	// TODO borrar para sacar tests
	public CurrencyConversionResponse convert(final Money money, final Currency to) {
		return convert(money, List.of(to)).getFirst();
	}

	public List<CurrencyConversionResponse> convert(final Money money, final List<Currency> to) {
		final var targetRates = this.currencyRateProvider.getCurrencyRates(money.currency(), to);
		return toResponses(money, targetRates, Instant.now(clock));
	}

    // TODO quitar este método ya que tenemos de una a varias en otro método
    public CurrencyRateResponse getCurrencyRate(final Currency from, final Currency to) {
		final var targetRate = this.currencyRateProvider.getCurrencyRates(from, List.of(to)).getFirst();
		return new CurrencyRateResponse(from, targetRate.target(), targetRate.currencyRate(), Instant.now(clock));
	}

	public List<Currency> getSupportedCurrencies() {
		return this.currencyRateProvider.getAvailableCurrencies();
	}

	public List<CurrencyConversionResponse> convert(final Money money, final List<Currency> to, final LocalDate date) {
		final var targetRates = this.currencyRateProvider.getHistoricalCurrencyRates(money.currency(), to, date);
		final var timestamp = date.atTime(this.currencyRateProvider.getDailyTimeOfRateMeasurement())
				.toInstant(ZoneOffset.UTC);
		return toResponses(money, targetRates, timestamp);
	}

	private static List<CurrencyConversionResponse> toResponses(final Money money,
	                                                              final List<TargetCurrencyRate> targetRates,
	                                                              final Instant timestamp) {
		return targetRates.stream()
				.map(t -> {
					return new CurrencyConversionResponse(money, money.convert(t), t.currencyRate(), timestamp);
				})
				.toList();
	}
}
