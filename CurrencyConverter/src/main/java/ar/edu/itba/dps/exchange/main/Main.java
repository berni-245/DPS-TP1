package ar.edu.itba.dps.exchange.main;

import ar.edu.itba.dps.exchange.domain.CurrencyConverter;
import ar.edu.itba.dps.exchange.infrastructure.api.FreeCurrencyApiProvider;
import ar.edu.itba.dps.exchange.infrastructure.http.UnirestHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.util.Currency;

public final class Main {

	private static final Logger LOG = LogManager.getLogger(Main.class);

	private Main() {
	}

	public static void main(final String[] args) {
		try {
			final var httpClient = new UnirestHttpClient();
			final var provider = new FreeCurrencyApiProvider(httpClient);
			final var converter = new CurrencyConverter(provider, Clock.systemUTC());
			LOG.info("{}", converter.convert(Currency.getInstance("EUR"), Currency.getInstance("USD"), 100));
		} catch (final RuntimeException e) {
			LOG.error("Conversion demo failed", e);
			System.exit(1);
		}
	}
}
