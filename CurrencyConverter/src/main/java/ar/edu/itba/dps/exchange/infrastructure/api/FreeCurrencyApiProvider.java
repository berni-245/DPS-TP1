package ar.edu.itba.dps.exchange.infrastructure.api;

import ar.edu.itba.dps.exchange.domain.CurrencyRate;
import ar.edu.itba.dps.exchange.domain.CurrencyRateNotAvailable;
import ar.edu.itba.dps.exchange.domain.CurrencyRateProvider;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpClient;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpResponse;
import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public class FreeCurrencyApiProvider implements CurrencyRateProvider {

	private static final Gson GSON = new Gson();

	private static final String API_KEY = "fca_live_JA6vg6L8PJQe85FIp4ohUOdZgUn6LjQ42sS07OAB";
	private static final String DEFAULT_BASE_URL = "https://api.freecurrencyapi.com/v1/";

	private static final String ENDPOINT_LATEST = "latest";
	private static final String ENDPOINT_CURRENCIES = "currencies";

	private static final String QUERY_BASE_CURRENCY = "base_currency";
	private static final String QUERY_CURRENCIES = "currencies";

	private static final Map<String, String> JSON_REQUEST_HEADERS = Map.of(
			"accept", "application/json",
			"apikey", API_KEY
	);

	private final HttpClient httpClient;
	private final String baseUrl;

	public FreeCurrencyApiProvider(final HttpClient httpClient) {
		this(httpClient, DEFAULT_BASE_URL);
	}

	public FreeCurrencyApiProvider(final HttpClient httpClient, final String baseUrl) {
		this.httpClient = httpClient;
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
	}

	@Override
	public CurrencyRate getCurrencyRate(final Currency from, final Currency to) {
		final HttpResponse response = this.getConversionRate(
				from.getCurrencyCode(),
				to.getCurrencyCode());
		final ExchangeRateResponse rate = this.parseJsonOrUnavailable(response, ExchangeRateResponse.class);
		return new CurrencyRate(rate.getExchange(to.getCurrencyCode()));
	}

	@Override
	public List<Currency> getAvailableCurrencies() {
		final HttpResponse response = this.getCurrencies();
		final AvailableCurrenciesResponse body = this.parseJsonOrUnavailable(response,
				AvailableCurrenciesResponse.class);
		return body.getCurrencies();
	}

	// FIX: The exception is not specific to each endpoint.
	private <T> T parseJsonOrUnavailable(final HttpResponse response, final Class<T> type) {
		if (response.statusCode() != HttpURLConnection.HTTP_OK) {
			throw new CurrencyRateNotAvailable();
		}
		return GSON.fromJson(response.body(), type);
	}

	private URI buildUrl(final String endpoint) {
		return URI.create(this.baseUrl + endpoint);
	}

	private HttpResponse getConversionRate(final String fromCurrency, final String toCurrency) {
		return this.httpClient.get(
				this.buildUrl(ENDPOINT_LATEST),
				Map.of(QUERY_BASE_CURRENCY, fromCurrency, QUERY_CURRENCIES, toCurrency),
				JSON_REQUEST_HEADERS);
	}

	private HttpResponse getCurrencies() {
		return this.httpClient.get(this.buildUrl(ENDPOINT_CURRENCIES), null, JSON_REQUEST_HEADERS);
	}
}
