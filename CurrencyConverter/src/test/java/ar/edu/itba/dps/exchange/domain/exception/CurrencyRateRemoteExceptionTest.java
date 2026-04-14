package ar.edu.itba.dps.exchange.domain.exception;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CurrencyRateRemoteExceptionTest {

	@Test
	void nullResponseDetailNormalizesToEmptyOptionalAndMessageWithoutSuffix() {
		final var ex = new CurrencyRateRemoteException(502, null);
		assertThat(ex.statusCode(), is(502));
		assertFalse(ex.responseDetail().isPresent());
		assertThat(ex.getMessage(), is("Currency service returned status 502"));
	}

	@Test
	void blankResponseDetailMessageOmitsDetailSuffix() {
		final var ex = new CurrencyRateRemoteException(503, "   ");
		assertThat(ex.getMessage(), is("Currency service returned status 503"));
	}

	@Test
	void emptyResponseDetailOptionalEmpty() {
		final var ex = new CurrencyRateRemoteException(503, "");
		assertFalse(ex.responseDetail().isPresent());
	}

	@Test
	void nonBlankResponseDetailIncludesInMessageAndOptional() {
		final var ex = new CurrencyRateRemoteException(404, "oops");
		assertThat(ex.responseDetail(), is(Optional.of("oops")));
		assertThat(ex.getMessage(), containsString("404"));
		assertThat(ex.getMessage(), containsString("oops"));
	}

	@Test
	void longResponseBodyIsTruncatedWithEllipsis() {
		final String longBody = "x".repeat(300);
		final var ex = new CurrencyRateRemoteException(500, longBody);
		assertThat(ex.responseDetail().isPresent(), is(true));
		assertThat(ex.responseDetail().get(), endsWith("…"));
		assertThat(ex.responseDetail().get().length(), is(257));
		assertThat(ex.getMessage(), containsString("…"));
	}
}
