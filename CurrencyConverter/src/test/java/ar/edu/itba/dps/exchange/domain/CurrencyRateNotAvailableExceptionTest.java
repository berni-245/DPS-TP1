package ar.edu.itba.dps.exchange.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CurrencyRateNotAvailableExceptionTest {

	@Test
	void noArgConstructor_setsDefaultMessage() {
		final var ex = new CurrencyRateNotAvailableException();
		assertThat(ex.getMessage(), is("Currency rate is not available"));
	}

	@Test
	void messageConstructor_preservesCause() {
		final var cause = new RuntimeException("x");
		final var ex = new CurrencyRateNotAvailableException("custom", cause);
		assertThat(ex.getMessage(), is("custom"));
		assertThat(ex.getCause(), is(cause));
	}
}
