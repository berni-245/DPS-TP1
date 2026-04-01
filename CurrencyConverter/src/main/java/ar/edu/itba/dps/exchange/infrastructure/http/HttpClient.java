package ar.edu.itba.dps.exchange.infrastructure.http;

import java.net.URI;
import java.util.Map;

public interface HttpClient {
	HttpResponse get(final URI url, Map<String, Object> queryParams, Map<String, String> headers);
}
