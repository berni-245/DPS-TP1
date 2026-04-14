package ar.edu.itba.dps.exchange.domain.exception;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CurrencyRateRemoteExceptionTest {

	@Test
	void nullBodyOmitsDetailSuffix() {
		final var ex = new CurrencyRateRemoteException(502, null);
		assertThat(ex.getMessage(), is("Currency service returned status 502"));
	}

	@Test
	void blankBodyOmitsDetailSuffix() {
		final var ex = new CurrencyRateRemoteException(503, "   ");
		assertThat(ex.getMessage(), is("Currency service returned status 503"));
	}

	@Test
	void emptyBodyOmitsDetailSuffix() {
		final var ex = new CurrencyRateRemoteException(503, "");
		assertThat(ex.getMessage(), is("Currency service returned status 503"));
	}

	@Test
	void nonBlankBodyIncludedInMessage() {
		final var ex = new CurrencyRateRemoteException(404, "oops");
		assertThat(ex.getMessage(), containsString("404"));
		assertThat(ex.getMessage(), containsString("oops"));
	}

	@Test
	void longResponseBodyIsTruncatedWithEllipsis() {
		final String longBody = "x".repeat(300);
		final var ex = new CurrencyRateRemoteException(500, longBody);
		assertThat(ex.getMessage(), endsWith("…"));
		assertThat(ex.getMessage().length(), greaterThan("Currency service returned status 500 — ".length()));
	}
}
