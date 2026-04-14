package ar.edu.itba.dps.exchange.infrastructure.http;

/**
 * Low-level HTTP client failure (connection, timeout, etc.), before a response is available.
 */
public final class HttpClientException extends RuntimeException {

	public HttpClientException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
