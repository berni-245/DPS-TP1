package ar.edu.itba.dps.exchange.domain;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(
        Currency currency,
        BigDecimal amount) {
    public Money convert(TargetCurrencyRate targetCurrencyRate){
        return new Money(
                targetCurrencyRate.target(),
                amount.multiply(targetCurrencyRate.currencyRate().rate())
        );
    }
}

