package ar.edu.itba.dps.exchange.domain;

/**
 * A currency rate could not be obtained from an otherwise successful response (e.g. invalid payload).
 */
public class CurrencyRateNotAvailable extends CurrencyRateProviderException {

	public CurrencyRateNotAvailable() {
		super("Currency rate is not available");
	}

	public CurrencyRateNotAvailable(final String message, final Throwable cause) {
		super(message, cause);
	}
}
