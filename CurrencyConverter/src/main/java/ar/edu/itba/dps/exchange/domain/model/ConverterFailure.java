package ar.edu.itba.dps.exchange.domain.model;

import ar.edu.itba.dps.exchange.domain.exception.CurrencyRateProviderException;

/**
 * Motivo de fallo al resolver una operación del {@link ar.edu.itba.dps.exchange.domain.service.CurrencyConverter}.
 */
public sealed interface ConverterFailure permits ConverterFailure.ProviderError, ConverterFailure.NoRatesAvailable {

	record ProviderError(CurrencyRateProviderException exception) implements ConverterFailure {}

	record NoRatesAvailable() implements ConverterFailure {}
}
