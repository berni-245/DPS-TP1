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

	public CurrencyConversionResponse convert(final Money money, final Currency to) {
		return convert(money, List.of(to)).getFirst();
	}

	public List<CurrencyConversionResponse> convert(final Money money, final List<Currency> to) {
		final var quotes = this.currencyRateProvider.getCurrencyRates(money.currency(), to);
		return toResponses(money, quotes, Instant.now(clock));
	}

	public CurrencyRateQuote getCurrencyRate(final Currency from, final Currency to) {
		final var quote = this.currencyRateProvider.getCurrencyRates(from, List.of(to)).getFirst();
		return new CurrencyRateQuote(from, quote.target(), quote.quotedRate().rate(), Instant.now(clock));
	}

	public List<Currency> getSupportedCurrencies() {
		return this.currencyRateProvider.getAvailableCurrencies();
	}

	public List<CurrencyConversionResponse> convert(final Money money, final List<Currency> to, final LocalDate date) {
		final var quotes = this.currencyRateProvider.getHistoricalCurrencyRates(money.currency(), to, date);
		final var timestamp = date.atTime(this.currencyRateProvider.getDailyTimeOfRateMeasurement())
				.toInstant(ZoneOffset.UTC);
		return toResponses(money, quotes, timestamp);
	}

	private static List<CurrencyConversionResponse> toResponses(final Money money,
	                                                              final List<TargetCurrencyQuote> quotes,
	                                                              final Instant timestamp) {
		return quotes.stream()
				.map(q -> {
					final var r = q.quotedRate().rate();
					return new CurrencyConversionResponse(
							money,
							new Money(q.target(), money.amount().multiply(r)),
							r,
							timestamp);
				})
				.toList();
	}
}
