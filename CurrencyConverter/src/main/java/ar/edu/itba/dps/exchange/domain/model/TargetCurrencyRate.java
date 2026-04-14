package ar.edu.itba.dps.exchange.domain.model;

import java.util.Currency;

public record TargetCurrencyRate(Currency target, CurrencyRate currencyRate) {
}
