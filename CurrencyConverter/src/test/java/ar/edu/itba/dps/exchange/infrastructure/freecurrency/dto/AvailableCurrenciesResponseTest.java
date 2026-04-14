package ar.edu.itba.dps.exchange.infrastructure.freecurrency.dto;

import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AvailableCurrenciesResponseTest {

	private static final int DECIMAL_DIGITS = 2;
	private static final int ROUNDING = 0;
	private static final String USD_CODE = "USD";
	private static final String SYMBOL_DOLLAR = "$";
	private static final String US_DOLLAR_NAME = "US Dollar";
	private static final String US_DOLLARS_PLURAL = "US dollars";

	@Test
	void getCurrenciesWhenDataNullReturnsEmpty() {
		final var r = new AvailableCurrenciesResponse(null);
		assertThat(r.getCurrencies(), empty());
	}

	@Test
	void getCurrenciesWhenDataEmptyReturnsEmpty() {
		final var r = new AvailableCurrenciesResponse(Map.of());
		assertThat(r.getCurrencies(), empty());
	}

	@Test
	void getCurrenciesReturnsKeysAsCurrency() {
		final var r = new AvailableCurrenciesResponse(Map.of(
				USD_CODE, new AvailableCurrenciesResponse.CurrencyDetail(
						SYMBOL_DOLLAR, US_DOLLAR_NAME, SYMBOL_DOLLAR, DECIMAL_DIGITS, ROUNDING, USD_CODE, US_DOLLARS_PLURAL)));
		assertThat(r.getCurrencies(), contains(Currency.getInstance(USD_CODE)));
	}
}
