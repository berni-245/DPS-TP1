package ar.edu.itba.dps.exchange.infrastructure.api;

import ar.edu.itba.dps.exchange.domain.CurrencyRate;
import ar.edu.itba.dps.exchange.domain.CurrencyRateNotAvailable;
import ar.edu.itba.dps.exchange.domain.CurrencyRateProvider;
import ar.edu.itba.dps.exchange.domain.CurrencyRateRemoteException;
import ar.edu.itba.dps.exchange.domain.CurrencyRateTransportException;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpClient;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpResponse;
import ar.edu.itba.dps.exchange.infrastructure.http.HttpTransportException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FreeCurrencyApiProvider implements CurrencyRateProvider {

	private static final Logger LOG = LogManager.getLogger(FreeCurrencyApiProvider.class);
	private static final Gson GSON = new Gson();

	private static final String API_KEY = "fca_live_JA6vg6L8PJQe85FIp4ohUOdZgUn6LjQ42sS07OAB";
	private static final String DEFAULT_BASE_URL = "https://api.freecurrencyapi.com/v1/";

	private static final String ENDPOINT_LATEST = "latest";
	private static final String ENDPOINT_CURRENCIES = "currencies";
	private static final String ENDPOINT_HISTORICAL = "historical";

	private static final String QUERY_BASE_CURRENCY = "base_currency";
	private static final String QUERY_CURRENCIES = "currencies";
	private static final String QUERY_DATE = "date";

	private static final Map<String, String> JSON_REQUEST_HEADERS = Map.of(
			"accept", "application/json",
			"apikey", API_KEY
	);

	private static final int RESPONSE_DETAIL_MAX_LEN = 256;

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
	public List<CurrencyRate> getCurrencyRates(final Currency from, final List<Currency> to) {
		final String currencies = to.stream()
				.map(Currency::getCurrencyCode)
				.collect(Collectors.joining(","));
		final HttpResponse response = this.getConversionRates(from.getCurrencyCode(), currencies);
		final ExchangeRateResponse rates = this.parseJsonOrUnavailable(response, ExchangeRateResponse.class);
		return to.stream()
				.map(currency -> new CurrencyRate(rates.getExchange(currency.getCurrencyCode())))
				.toList();
	}

	@Override
	public List<Currency> getAvailableCurrencies() {
		final HttpResponse response = this.getCurrencies();
		final AvailableCurrenciesResponse body = this.parseJsonOrUnavailable(response,
				AvailableCurrenciesResponse.class);
		return body.getCurrencies();
	}

	@Override
	public List<CurrencyRate> getHistoricalCurrencyRates(final Currency from, final List<Currency> to,
	                                                     final LocalDate date) {
		final String currencies = to.stream()
				.map(Currency::getCurrencyCode)
				.collect(Collectors.joining(","));
		final HttpResponse response = this.getHistoricalConversionRate(from.getCurrencyCode(), currencies,
				date.toString());
		final HistoricalExchangeRateResponse rates = this.parseJsonOrUnavailable(response,
				HistoricalExchangeRateResponse.class);
		return to.stream()
				.map(currency -> new CurrencyRate(rates.getExchange(date.toString(), currency.getCurrencyCode())))
				.toList();

	}

	@Override
	public LocalTime getDailyTimeOfRateMeasurement() {
		return LocalTime.of(23, 59, 59);
	}

	private <T> T parseJsonOrUnavailable(final HttpResponse response, final Class<T> type) {
		if (response.statusCode() != HttpURLConnection.HTTP_OK) {
			final String excerpt = sanitizeResponseExcerpt(response.body());
			LOG.warn("Currency service returned HTTP {}: {}", response.statusCode(), excerpt);
			throw new CurrencyRateRemoteException(response.statusCode(), excerpt);
		}
		try {
			return GSON.fromJson(response.body(), type);
		} catch (final JsonSyntaxException e) {
			final String excerpt = sanitizeResponseExcerpt(response.body());
			LOG.warn("Currency service response is not valid JSON: {}", excerpt, e);
			throw new CurrencyRateNotAvailable("Could not parse currency service response", e);
		}
	}

	static String sanitizeResponseExcerpt(final String body) {
		if (body == null || body.isBlank()) {
			return "";
		}
		String s = body.strip().replace('\n', ' ').replace('\r', ' ');
		if (s.length() > RESPONSE_DETAIL_MAX_LEN) {
			s = s.substring(0, RESPONSE_DETAIL_MAX_LEN) + "…";
		}
		return s;
	}

	private HttpResponse executeGet(final URI url, final Map<String, Object> queryParams) {
		try {
			return this.httpClient.get(url, queryParams, JSON_REQUEST_HEADERS);
		} catch (final HttpTransportException e) {
			throw new CurrencyRateTransportException("Failed to contact currency service", e);
		}
	}

	private URI buildUrl(final String endpoint) {
		return URI.create(this.baseUrl + endpoint);
	}

	private HttpResponse getConversionRates(final String fromCurrency, final String currencies) {
		return this.executeGet(
				this.buildUrl(ENDPOINT_LATEST),
				Map.of(QUERY_BASE_CURRENCY, fromCurrency, QUERY_CURRENCIES, currencies));
	}

	private HttpResponse getCurrencies() {
		return this.executeGet(this.buildUrl(ENDPOINT_CURRENCIES), Map.of());
	}

	private HttpResponse getHistoricalConversionRate(final String fromCurrency, final String currencies,
	                                                 final String date) {
		return this.executeGet(
				this.buildUrl(ENDPOINT_HISTORICAL),
				Map.of(
						QUERY_BASE_CURRENCY, fromCurrency,
						QUERY_DATE, date,
						QUERY_CURRENCIES, currencies
				));
	}
}
