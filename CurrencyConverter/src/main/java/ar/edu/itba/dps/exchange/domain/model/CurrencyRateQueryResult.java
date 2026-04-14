package ar.edu.itba.dps.exchange.domain.model;

public sealed interface CurrencyRateQueryResult
		permits CurrencyRateQueryResult.Success, CurrencyRateQueryResult.Failure {

	record Success(CurrencyRateResponse rate) implements CurrencyRateQueryResult {}

	record Failure(ConverterFailure reason) implements CurrencyRateQueryResult {}
}
