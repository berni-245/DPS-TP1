package ar.edu.itba.dps.exchange.domain;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CurrencyRateRemoteExceptionTest {

	@Test
	void nullResponseDetail_normalizesToEmptyOptionalAndMessageWithoutSuffix() {
		final var ex = new CurrencyRateRemoteException(502, null);
		assertThat(ex.statusCode(), is(502));
		assertFalse(ex.responseDetail().isPresent());
		assertThat(ex.getMessage(), is("Currency service returned status 502"));
	}

	@Test
	void blankResponseDetail_messageOmitsDetailSuffix() {
		final var ex = new CurrencyRateRemoteException(503, "   ");
		assertThat(ex.getMessage(), is("Currency service returned status 503"));
	}

	@Test
	void emptyResponseDetail_optionalEmpty() {
		final var ex = new CurrencyRateRemoteException(503, "");
		assertFalse(ex.responseDetail().isPresent());
	}

	@Test
	void nonBlankResponseDetail_includesInMessageAndOptional() {
		final var ex = new CurrencyRateRemoteException(404, "oops");
		assertThat(ex.responseDetail(), is(Optional.of("oops")));
		assertThat(ex.getMessage(), containsString("404"));
		assertThat(ex.getMessage(), containsString("oops"));
	}

	@Test
	void longResponseBody_isTruncatedWithEllipsis() {
		final String longBody = "x".repeat(300);
		final var ex = new CurrencyRateRemoteException(500, longBody);
		assertThat(ex.responseDetail().isPresent(), is(true));
		assertThat(ex.responseDetail().get(), endsWith("…"));
		assertThat(ex.responseDetail().get().length(), is(257));
		assertThat(ex.getMessage(), containsString("…"));
	}
}
