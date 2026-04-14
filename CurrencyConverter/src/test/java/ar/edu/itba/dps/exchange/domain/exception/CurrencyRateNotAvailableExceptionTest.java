package ar.edu.itba.dps.exchange.domain.exception;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CurrencyRateNotAvailableExceptionTest {

	private static final String DEFAULT_MESSAGE = "Currency rate is not available";
	private static final String CUSTOM_MESSAGE = "custom";
	private static final String CAUSE_MESSAGE = "x";

	@Test
	void noArgConstructorSetsDefaultMessage() {
		final var ex = new CurrencyRateNotAvailableException();
		assertThat(ex.getMessage(), is(DEFAULT_MESSAGE));
	}

	@Test
	void messageConstructorPreservesCause() {
		final var cause = new RuntimeException(CAUSE_MESSAGE);
		final var ex = new CurrencyRateNotAvailableException(CUSTOM_MESSAGE, cause);
		assertThat(ex.getMessage(), is(CUSTOM_MESSAGE));
		assertThat(ex.getCause(), is(cause));
	}
}
