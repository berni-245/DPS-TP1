package ar.edu.itba.dps.exchange.domain.model;

import java.util.List;

public sealed interface CurrencyConversionsResult
		permits CurrencyConversionsResult.Success, CurrencyConversionsResult.Failure {

	record Success(List<CurrencyConversionResponse> conversions) implements CurrencyConversionsResult {}

	record Failure(ConverterFailure reason) implements CurrencyConversionsResult {}
}
