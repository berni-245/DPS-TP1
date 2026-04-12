package ar.edu.itba.dps.exchange.domain;

import java.time.Instant;
import java.util.Currency;

public record CurrencyRateQuote(Currency fromCurrency, Currency toCurrency, double rate, Instant timestamp) {
}
