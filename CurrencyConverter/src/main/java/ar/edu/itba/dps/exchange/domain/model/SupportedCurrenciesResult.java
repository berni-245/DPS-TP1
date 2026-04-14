package ar.edu.itba.dps.exchange.domain.model;

import java.util.Currency;
import java.util.List;

public sealed interface SupportedCurrenciesResult
		permits SupportedCurrenciesResult.Success, SupportedCurrenciesResult.Failure {

	record Success(List<Currency> currencies) implements SupportedCurrenciesResult {}

	record Failure(ConverterFailure reason) implements SupportedCurrenciesResult {}
}
