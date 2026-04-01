package ar.edu.itba.dps.exchange.infrastructure.api;

import com.google.gson.annotations.SerializedName;

import java.util.Currency;
import java.util.List;
import java.util.Map;

record AvailableCurrenciesResponse(Map<String, CurrencyDetail> data) {

	List<Currency> getCurrencies() {
		if (this.data == null || this.data.isEmpty()) {
			return List.of();
		}
		return this.data.keySet().stream()
				.map(Currency::getInstance)
				.toList();
	}

	record CurrencyDetail(
			String symbol,
			String name,
			@SerializedName("symbol_native") String symbolNative,
			@SerializedName("decimal_digits") int decimalDigits,
			int rounding,
			String code,
			@SerializedName("name_plural") String namePlural
	) {
	}
}
