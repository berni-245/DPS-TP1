package ar.edu.itba.dps.exchange.domain;

import java.util.Currency;

public record TargetCurrencyRate(Currency target, CurrencyRate currencyRate) {
}
