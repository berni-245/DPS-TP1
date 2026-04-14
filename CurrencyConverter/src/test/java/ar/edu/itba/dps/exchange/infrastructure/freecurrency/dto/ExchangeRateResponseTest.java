package ar.edu.itba.dps.exchange.infrastructure.freecurrency.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRateResponseTest {

	@Test
	void getExchange_whenDataNull_throws() {
		final var r = new ExchangeRateResponse(null);
		final var ex = assertThrows(IllegalStateException.class, () -> r.getExchange("USD"));
		assertThat(ex.getMessage(), containsString("exchange data"));
	}

	@Test
	void getExchange_whenCurrencyMissing_throws() {
		final var r = new ExchangeRateResponse(Map.of("EUR", BigDecimal.ONE));
		final var ex = assertThrows(IllegalStateException.class, () -> r.getExchange("USD"));
		assertThat(ex.getMessage(), containsString("USD"));
	}
}
