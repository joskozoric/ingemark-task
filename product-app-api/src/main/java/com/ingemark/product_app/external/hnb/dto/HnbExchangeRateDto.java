package com.ingemark.product_app.external.hnb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HnbExchangeRateDto {

    @JsonProperty("broj_tecajnice")
    private String tecajnicaNumber;

    @JsonProperty("datum_primjene")
    private String applicationDate;

    @JsonProperty("drzava")
    private String country;

    @JsonProperty("drzava_iso")
    private String countryIso;

    @JsonDeserialize(using = CurrencyExchangeRateDeserializer.class)
    @JsonProperty("kupovni_tecaj")
    private BigDecimal buyingRate;

    @JsonDeserialize(using = CurrencyExchangeRateDeserializer.class)
    @JsonProperty("prodajni_tecaj")
    private BigDecimal sellingRate;

    @JsonDeserialize(using = CurrencyExchangeRateDeserializer.class)
    @JsonProperty("srednji_tecaj")
    private BigDecimal middleRate;

    @JsonProperty("valuta")
    private String currency;

    @JsonProperty("sifra_valute")
    private String currencyCode;

    @Override
    public String toString() {
        return "HnbExchangeRateDto{" +
                "currency='" + currency + '\'' +
                ", middleRate='" + middleRate + '\'' +
                ", applicationDate='" + applicationDate + '\'' +
                '}';
    }
}
