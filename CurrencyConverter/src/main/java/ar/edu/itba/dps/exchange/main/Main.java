import ar.edu.itba.dps.exchange.CurrencyApiProvider;
import ar.edu.itba.dps.exchange.CurrencyConverter;
import ar.edu.itba.dps.exchange.UnirestHttpClient;

void main() {
	final var httpClient = new UnirestHttpClient();
	final var provider = new CurrencyApiProvider(httpClient);
	final var converter = new CurrencyConverter(provider);
	System.out.println(converter.convert(Currency.getInstance("EUR"), Currency.getInstance("USD"), 100));
}
