package ar.edu.itba.dps.exchange.infrastructure.freecurrency.dto;

import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AvailableCurrenciesResponseTest {

	@Test
	void getCurrencies_whenDataNull_returnsEmpty() {
		final var r = new AvailableCurrenciesResponse(null);
		assertThat(r.getCurrencies(), empty());
	}

	@Test
	void getCurrencies_whenDataEmpty_returnsEmpty() {
		final var r = new AvailableCurrenciesResponse(Map.of());
		assertThat(r.getCurrencies(), empty());
	}

	@Test
	void getCurrencies_returnsKeysAsCurrency() {
		final var r = new AvailableCurrenciesResponse(Map.of(
				"USD", new AvailableCurrenciesResponse.CurrencyDetail(
						"$", "US Dollar", "$", 2, 0, "USD", "US dollars")));
		assertThat(r.getCurrencies(), contains(Currency.getInstance("USD")));
	}
}
