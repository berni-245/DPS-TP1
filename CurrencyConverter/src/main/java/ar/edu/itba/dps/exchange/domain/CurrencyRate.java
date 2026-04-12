package ar.edu.itba.dps.exchange.domain;

import java.math.BigDecimal;

public record CurrencyRate(BigDecimal rate) {

	public CurrencyRate(final double rate) {
		this(BigDecimal.valueOf(rate));
	}
}
