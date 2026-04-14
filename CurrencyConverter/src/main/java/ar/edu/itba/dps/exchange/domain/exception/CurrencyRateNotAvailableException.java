package ar.edu.itba.dps.exchange.domain.exception;

/**
 * A currency rate could not be obtained from an otherwise successful response (e.g. invalid payload).
 */
public class CurrencyRateNotAvailableException extends CurrencyRateProviderException {

	public CurrencyRateNotAvailableException() {
		super("Currency rate is not available");
	}

	public CurrencyRateNotAvailableException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
