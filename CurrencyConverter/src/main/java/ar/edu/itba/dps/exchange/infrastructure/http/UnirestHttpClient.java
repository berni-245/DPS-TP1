package ar.edu.itba.dps.exchange.infrastructure.http;

import com.mashape.unirest.http.Unirest;

import java.net.URI;
import java.util.Map;

public class UnirestHttpClient implements HttpClient {

	@Override
	public HttpResponse get(final URI url, final Map<String, Object> queryParams,
                            final Map<String, String> headers) {
		try {
			final var response = Unirest.get(url.toString()).queryString(queryParams).headers(headers).asJson();
			return new HttpResponse(response.getBody().toString(), response.getStatus());
		} catch (final Exception e) {
			System.err.println("Error: " + e.getMessage());
			return new HttpResponse("{\"error\":\"Internal Server Error\"}", 500);
		}
	}
}
