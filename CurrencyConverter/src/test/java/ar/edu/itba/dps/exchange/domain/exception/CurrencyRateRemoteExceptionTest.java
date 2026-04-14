package ar.edu.itba.dps.exchange.domain.exception;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CurrencyRateRemoteExceptionTest {

	private static final int STATUS_BAD_GATEWAY = 502;
	private static final int STATUS_SERVICE_UNAVAILABLE = 503;
	private static final int STATUS_NOT_FOUND = 404;
	private static final int STATUS_INTERNAL_SERVER_ERROR = 500;
	private static final int LONG_RESPONSE_BODY_LENGTH = 300;
	private static final String STATUS_MESSAGE_PREFIX = "Currency service returned status ";
	private static final String DETAIL_SEPARATOR = " — ";
	private static final String MESSAGE_STATUS_502 = STATUS_MESSAGE_PREFIX + STATUS_BAD_GATEWAY;
	private static final String MESSAGE_STATUS_503 = STATUS_MESSAGE_PREFIX + STATUS_SERVICE_UNAVAILABLE;
	private static final String MESSAGE_PREFIX_STATUS_500 = STATUS_MESSAGE_PREFIX + STATUS_INTERNAL_SERVER_ERROR + DETAIL_SEPARATOR;
	private static final String BLANK_RESPONSE_BODY = "   ";
	private static final String EMPTY_RESPONSE_BODY = "";
	private static final String NON_EMPTY_RESPONSE_BODY = "oops";
	private static final String LONG_BODY_FILLER_CHAR = "x";
	private static final String ELLIPSIS_CHAR = "…";

	@Test
	void nullBodyOmitsDetailSuffix() {
		final var ex = new CurrencyRateRemoteException(STATUS_BAD_GATEWAY, null);
		assertThat(ex.getMessage(), is(MESSAGE_STATUS_502));
	}

	@Test
	void blankBodyOmitsDetailSuffix() {
		final var ex = new CurrencyRateRemoteException(STATUS_SERVICE_UNAVAILABLE, BLANK_RESPONSE_BODY);
		assertThat(ex.getMessage(), is(MESSAGE_STATUS_503));
	}

	@Test
	void emptyBodyOmitsDetailSuffix() {
		final var ex = new CurrencyRateRemoteException(STATUS_SERVICE_UNAVAILABLE, EMPTY_RESPONSE_BODY);
		assertThat(ex.getMessage(), is(MESSAGE_STATUS_503));
	}

	@Test
	void nonBlankBodyIncludedInMessage() {
		final var ex = new CurrencyRateRemoteException(STATUS_NOT_FOUND, NON_EMPTY_RESPONSE_BODY);
		assertThat(ex.getMessage(), containsString(Integer.toString(STATUS_NOT_FOUND)));
		assertThat(ex.getMessage(), containsString(NON_EMPTY_RESPONSE_BODY));
	}

	@Test
	void longResponseBodyIsTruncatedWithEllipsis() {
		final String longBody = LONG_BODY_FILLER_CHAR.repeat(LONG_RESPONSE_BODY_LENGTH);
		final var ex = new CurrencyRateRemoteException(STATUS_INTERNAL_SERVER_ERROR, longBody);
		assertThat(ex.getMessage(), endsWith(ELLIPSIS_CHAR));
		assertThat(ex.getMessage().length(), greaterThan(MESSAGE_PREFIX_STATUS_500.length()));
	}
}
