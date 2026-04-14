package ar.edu.itba.dps.exchange.main;

import ar.edu.itba.dps.exchange.domain.model.Money;
import ar.edu.itba.dps.exchange.domain.service.CurrencyConverter;
import ar.edu.itba.dps.exchange.infrastructure.freecurrency.FreeCurrencyApiProvider;
import ar.edu.itba.dps.exchange.infrastructure.http.UnirestHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Currency;

public final class Main {

	private static final Logger LOG = LogManager.getLogger(Main.class);

	static void main() {
		try {
			final var httpClient = new UnirestHttpClient();
			final var provider = new FreeCurrencyApiProvider(httpClient);
			final var converter = new CurrencyConverter(provider, Clock.systemUTC());
			LOG.info("{}", converter.convert(new Money(Currency.getInstance("EUR"), BigDecimal.valueOf(100)), Currency.getInstance("USD")));
		} catch (final RuntimeException e) {
			LOG.error("Conversion demo failed", e);
			System.exit(1);
		}
	}
}
