package ar.edu.itba.dps.exchange.infrastructure.freecurrency.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRateResponseTest {

	private static final String USD_CODE = "USD";
	private static final String EUR_CODE = "EUR";
	private static final String EXCHANGE_DATA_SNIPPET = "exchange data";

	@Test
	void getExchangeWhenDataNullThrows() {
		final var r = new ExchangeRateResponse(null);
		final var ex = assertThrows(IllegalStateException.class, () -> r.getExchange(USD_CODE));
		assertThat(ex.getMessage(), containsString(EXCHANGE_DATA_SNIPPET));
	}

	@Test
	void getExchangeWhenCurrencyMissingThrows() {
		final var r = new ExchangeRateResponse(Map.of(EUR_CODE, BigDecimal.ONE));
		final var ex = assertThrows(IllegalStateException.class, () -> r.getExchange(USD_CODE));
		assertThat(ex.getMessage(), containsString(USD_CODE));
	}
}
