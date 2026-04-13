package ar.edu.itba.dps.exchange.domain;

import java.util.Currency;

public record TargetCurrencyQuote(Currency target, CurrencyRate quotedRate) {
}
