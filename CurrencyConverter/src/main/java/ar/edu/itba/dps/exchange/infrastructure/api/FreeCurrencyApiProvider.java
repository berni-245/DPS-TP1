package ar.edu.itba.dps.exchange.infrastructure.api;

import ar.edu.itba.dps.exchange.domain.CurrencyRate;
import ar.edu.itba.dps.exchange.domain.CurrencyRateNotAvailable;
import ar.edu.itba.dps.exchange.domain.CurrencyRateProvider;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpClient;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpResponse;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Currency;
import java.util.Map;

@RequiredArgsConstructor
public class FreeCurrencyApiProvider implements CurrencyRateProvider {

	private static final String API_KEY = "fca_live_JA6vg6L8PJQe85FIp4ohUOdZgUn6LjQ42sS07OAB";
	private static final String BASE_URL = "https://api.freecurrencyapi.com/v1/";

	private final HttpClient httpClient;

	@Override
	public CurrencyRate getCurrencyRate(Currency from, Currency to) {
		final var response = this.getConversionRate(from.getCurrencyCode(), to.getCurrencyCode());
		if (response.statusCode() == 200) {
			final var rate = getExchangeRateResponse(response);
			return new CurrencyRate(rate.getExchange(to.getCurrencyCode()));
		} else {
			throw new CurrencyRateNotAvailable();
		}
	}

	private static ExchangeRateResponse getExchangeRateResponse(HttpResponse response) {
		return new Gson().fromJson(response.body(), ExchangeRateResponse.class);
	}

	private URI buildUrl(String endpoint) {
		return URI.create(BASE_URL + endpoint);
	}

	private HttpResponse getConversionRate(String fromCurrency, String toCurrency) {
		return this.httpClient.get(buildUrl("latest"),
				Map.of("base_currency", fromCurrency, "currencies", toCurrency),
				Map.of("accept", "application/json", "apikey", API_KEY));
	}

	private record ExchangeRateResponse(Map<String, Double> data) {

		public double getExchange(final String toCurrency) {
			if (this.data == null) {
				throw new IllegalStateException("Missing exchange data");
			}
			final Double rate = this.data.get(toCurrency);
			if (rate == null) {
				throw new IllegalStateException("Missing exchange rate for currency: " + toCurrency);
			}
			return rate;
		}
	}
}
