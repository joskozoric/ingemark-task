package com.ingemark.product_app.external.hnb.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

public class CurrencyExchangeRateDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String string = parser.getText();
        if (string.contains(",")) {
            string = string.replace(",", ".");
        }
        return new BigDecimal(string);
    }
}
