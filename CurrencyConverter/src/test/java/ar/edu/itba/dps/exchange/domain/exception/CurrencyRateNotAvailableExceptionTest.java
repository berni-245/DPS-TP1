package ar.edu.itba.dps.exchange.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CurrencyRateNotAvailableExceptionTest {

	private static final String DEFAULT_MESSAGE = "Currency rate is not available";
	private static final String CUSTOM_MESSAGE = "custom";
	private static final String CAUSE_MESSAGE = "x";

	@Test
	void noArgConstructorSetsDefaultMessage() {
		final var ex = new CurrencyRateNotAvailableException();
		assertEquals(DEFAULT_MESSAGE, ex.getMessage());
	}

	@Test
	void messageConstructorPreservesCause() {
		final var cause = new RuntimeException(CAUSE_MESSAGE);
		final var ex = new CurrencyRateNotAvailableException(CUSTOM_MESSAGE, cause);
		assertEquals(CUSTOM_MESSAGE, ex.getMessage());
		assertSame(cause, ex.getCause());
	}
}
