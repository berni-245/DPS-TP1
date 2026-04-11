package ar.edu.itba.dps.exchange.domain;

/**
 * Base type for failures when obtaining currency rates from a {@link CurrencyRateProvider}.
 */
public abstract class CurrencyRateProviderException extends RuntimeException {

	protected CurrencyRateProviderException(final String message) {
		super(message);
	}

	protected CurrencyRateProviderException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
