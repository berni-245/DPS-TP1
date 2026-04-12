package ar.edu.itba.dps.exchange.domain;

import java.util.Currency;

public record Money(Currency currency, double amount) {
}

