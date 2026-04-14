package ar.edu.itba.dps.exchange.infrastructure.freecurrency.dto;

import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AvailableCurrenciesResponseTest {

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
				"USD", new AvailableCurrenciesResponse.CurrencyDetail(
						"$", "US Dollar", "$", 2, 0, "USD", "US dollars")));
		assertThat(r.getCurrencies(), contains(Currency.getInstance("USD")));
	}
}
