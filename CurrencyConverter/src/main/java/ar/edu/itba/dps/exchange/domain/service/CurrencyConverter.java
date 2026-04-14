package ar.edu.itba.dps.exchange.domain.service;

import ar.edu.itba.dps.exchange.domain.exception.CurrencyRateProviderException;
import ar.edu.itba.dps.exchange.domain.model.ConverterFailure;
import ar.edu.itba.dps.exchange.domain.model.CurrencyConversionResponse;
import ar.edu.itba.dps.exchange.domain.model.CurrencyConversionResult;
import ar.edu.itba.dps.exchange.domain.model.CurrencyConversionsResult;
import ar.edu.itba.dps.exchange.domain.model.CurrencyRateQueryResult;
import ar.edu.itba.dps.exchange.domain.model.CurrencyRateResponse;
import ar.edu.itba.dps.exchange.domain.model.Money;
import ar.edu.itba.dps.exchange.domain.model.SupportedCurrenciesResult;
import ar.edu.itba.dps.exchange.domain.model.TargetCurrencyRate;
import ar.edu.itba.dps.exchange.domain.port.CurrencyRateProvider;
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

	public CurrencyConversionResult convert(final Money money, final Currency to) {
		return switch (convert(money, List.of(to))) {
			case CurrencyConversionsResult.Success s ->
					new CurrencyConversionResult.Success(s.conversions().getFirst());
			case CurrencyConversionsResult.Failure f ->
					new CurrencyConversionResult.Failure(f.reason());
		};
	}

	public CurrencyConversionsResult convert(final Money money, final List<Currency> to) {
		if (to.isEmpty()) {
			return new CurrencyConversionsResult.Success(List.of());
		}
		try {
			final var targetRates = this.currencyRateProvider.getCurrencyRates(money.currency(), to);
			if (targetRates.isEmpty()) {
				return new CurrencyConversionsResult.Failure(new ConverterFailure.NoRatesAvailable());
			}
			return new CurrencyConversionsResult.Success(toResponses(money, targetRates, Instant.now(clock)));
		} catch (final CurrencyRateProviderException e) {
			return new CurrencyConversionsResult.Failure(new ConverterFailure.ProviderError(e));
		}
	}

	public CurrencyRateQueryResult getCurrencyRate(final Currency from, final Currency to) {
		try {
			final var rates = this.currencyRateProvider.getCurrencyRates(from, List.of(to));
			if (rates.isEmpty()) {
				return new CurrencyRateQueryResult.Failure(new ConverterFailure.NoRatesAvailable());
			}
			final var targetRate = rates.getFirst();
			return new CurrencyRateQueryResult.Success(new CurrencyRateResponse(from, targetRate.target(),
					targetRate.currencyRate(), Instant.now(clock)));
		} catch (final CurrencyRateProviderException e) {
			return new CurrencyRateQueryResult.Failure(new ConverterFailure.ProviderError(e));
		}
	}

	public SupportedCurrenciesResult getSupportedCurrencies() {
		try {
			return new SupportedCurrenciesResult.Success(this.currencyRateProvider.getAvailableCurrencies());
		} catch (final CurrencyRateProviderException e) {
			return new SupportedCurrenciesResult.Failure(new ConverterFailure.ProviderError(e));
		}
	}

	public CurrencyConversionsResult convert(final Money money, final List<Currency> to, final LocalDate date) {
		if (to.isEmpty()) {
			return new CurrencyConversionsResult.Success(List.of());
		}
		try {
			final var targetRates = this.currencyRateProvider.getHistoricalCurrencyRates(money.currency(), to, date);
			if (targetRates.isEmpty()) {
				return new CurrencyConversionsResult.Failure(new ConverterFailure.NoRatesAvailable());
			}
			final var timestamp = date.atStartOfDay().toInstant(ZoneOffset.UTC);
			return new CurrencyConversionsResult.Success(toResponses(money, targetRates, timestamp));
		} catch (final CurrencyRateProviderException e) {
			return new CurrencyConversionsResult.Failure(new ConverterFailure.ProviderError(e));
		}
	}

	private static List<CurrencyConversionResponse> toResponses(final Money money,
	                                                              final List<TargetCurrencyRate> targetRates,
	                                                              final Instant timestamp) {
		return targetRates.stream()
				.map(t -> new CurrencyConversionResponse(money, money.convert(t), t.currencyRate(), timestamp))
				.toList();
	}
}
