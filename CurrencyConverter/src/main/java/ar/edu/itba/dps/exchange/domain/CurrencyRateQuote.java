package ar.edu.itba.dps.exchange.domain;

import java.time.Instant;

public record CurrencyRateQuote(double rate, Instant timestamp) {
}
