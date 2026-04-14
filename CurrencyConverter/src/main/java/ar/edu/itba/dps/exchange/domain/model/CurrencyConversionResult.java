package ar.edu.itba.dps.exchange.domain.model;

public sealed interface CurrencyConversionResult
		permits CurrencyConversionResult.Success, CurrencyConversionResult.Failure {

	record Success(CurrencyConversionResponse conversion) implements CurrencyConversionResult {}

	record Failure(ConverterFailure reason) implements CurrencyConversionResult {}
}
