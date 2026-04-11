package ar.edu.itba.dps.exchange.domain;

import java.util.Optional;

/**
 * The remote currency service responded with an error. The HTTP status code is exposed for
 * diagnostics; callers may map it to user-facing messages without depending on this type's name.
 */
public final class CurrencyRateRemoteException extends CurrencyRateProviderException {

	private final int statusCode;
	private final String responseDetail;

	public CurrencyRateRemoteException(final int statusCode, final String responseDetail) {
		super(buildMessage(statusCode, responseDetail));
		this.statusCode = statusCode;
		this.responseDetail = responseDetail == null ? "" : responseDetail;
	}

	public int statusCode() {
		return this.statusCode;
	}

	/**
	 * Sanitized excerpt of the response body when present (may be empty).
	 */
	public Optional<String> responseDetail() {
		return this.responseDetail.isEmpty() ? Optional.empty() : Optional.of(this.responseDetail);
	}

	private static String buildMessage(final int statusCode, final String responseDetail) {
		final String detail = responseDetail == null || responseDetail.isBlank()
				? ""
				: " — " + responseDetail;
		return "Currency service returned status " + statusCode + detail;
	}
}
