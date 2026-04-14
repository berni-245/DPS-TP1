package ar.edu.itba.dps.exchange.domain.exception;

/**
 * The currency rate service could not be reached (network error, timeout, etc.).
 */
public final class CurrencyRateConnectionException extends CurrencyRateProviderException {

	public CurrencyRateConnectionException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
