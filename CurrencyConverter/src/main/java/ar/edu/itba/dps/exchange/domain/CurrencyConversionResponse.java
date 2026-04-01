package ar.edu.itba.dps.exchange.domain;

import java.time.Instant;

public record CurrencyConversionResponse(double convertedAmount, Instant timestamp) {
}
