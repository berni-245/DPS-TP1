package ar.edu.itba.dps.exchange.infrastructure.http;

import com.mashape.unirest.http.Unirest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class UnirestHttpClient implements HttpClient {

	private static final Logger LOG = LogManager.getLogger(UnirestHttpClient.class);

	@Override
	public HttpResponse get(final URI url, final Map<String, Object> queryParams,
	                        final Map<String, String> headers) {
		try {
			final var request = Unirest.get(url.toString())
					.queryString(Objects.requireNonNullElse(queryParams, Map.of()))
					.headers(headers);
			final var response = request.asString();
			return new HttpResponse(response.getBody(), response.getStatus());
		} catch (final Exception e) {
			LOG.warn("HTTP GET failed for {}", url.getPath(), e);
			throw new HttpTransportException("HTTP request failed: " + e.getMessage(), e);
		}
	}
}
