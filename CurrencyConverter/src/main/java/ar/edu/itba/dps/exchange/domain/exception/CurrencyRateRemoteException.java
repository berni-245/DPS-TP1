package ar.edu.itba.dps.exchange.domain.exception;

/**
 * The remote currency API returned an error HTTP status (e.g. 404, 500).
 */
public final class CurrencyRateRemoteException extends CurrencyRateProviderException {

	private static final int DETAIL_MAX_LEN = 256;

	public CurrencyRateRemoteException(final int statusCode, final String body) {
		super(messageFor(statusCode, body));
	}

	private static String messageFor(final int statusCode, final String body) {
		final String detail = sanitize(body);
		return "Currency service returned status " + statusCode
				+ (detail.isEmpty() ? "" : " — " + detail);
	}

	private static String sanitize(final String body) {
		if (body == null || body.isBlank()) {
			return "";
		}
		String s = body.strip().replace('\n', ' ').replace('\r', ' ');
		return s.length() <= DETAIL_MAX_LEN ? s : s.substring(0, DETAIL_MAX_LEN) + "…";
	}
}
