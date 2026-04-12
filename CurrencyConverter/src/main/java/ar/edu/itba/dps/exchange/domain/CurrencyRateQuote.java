package ar.edu.itba.dps.exchange.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

public record CurrencyRateQuote(Currency fromCurrency, Currency toCurrency, BigDecimal rate, Instant timestamp) {
}
