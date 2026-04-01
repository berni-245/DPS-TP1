import ar.edu.itba.dps.exchange.domain.CurrencyConverter;
import ar.edu.itba.dps.exchange.infrastructure.api.FreeCurrencyApiProvider;
import ar.edu.itba.dps.exchange.infrastructure.http.UnirestHttpClient;

import java.time.Clock;
import java.util.Currency;

void main() {
	final var httpClient = new UnirestHttpClient();
	final var provider = new FreeCurrencyApiProvider(httpClient);
	final var converter = new CurrencyConverter(provider, Clock.systemUTC());
	System.out.println(converter.convert(Currency.getInstance("EUR"), Currency.getInstance("USD"), 100));
}
