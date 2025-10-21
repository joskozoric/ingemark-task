package com.ingemark.product_app.external.hnb;

import com.ingemark.product_app.external.hnb.dto.HnbExchangeRateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HnbCommunicatorServiceTest {
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private Mono<List<HnbExchangeRateDto>> monoResponse;

    private HnbCommunicatorService hnbCommunicatorService;

    private final Map<String,BigDecimal> exchangeRatesMap = Map.of(
            "USD", new BigDecimal("1.1"),
            "AUD", new BigDecimal("0.9"),
            "RUB", new BigDecimal("0.001")
    );

    @BeforeEach
    void setUp() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(monoResponse);

        List<HnbExchangeRateDto> rates = exchangeRatesMap.entrySet().stream()
                .map(entry -> {
                    HnbExchangeRateDto hnbExchangeRateDto = new HnbExchangeRateDto();
                    hnbExchangeRateDto.setCurrency(entry.getKey());
                    hnbExchangeRateDto.setMiddleRate(entry.getValue());
                    hnbExchangeRateDto.setApplicationDate(LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
                    return hnbExchangeRateDto;
                }).toList();

        when(monoResponse.block()).thenReturn(rates);
        hnbCommunicatorService = new HnbCommunicatorService(webClient);
    }

    @Test
    void fetchAndStoreRates_Success() {
        BigDecimal rate = hnbCommunicatorService.getMiddleRateUSD();
        assertEquals(0, rate.compareTo(exchangeRatesMap.get("USD")));
        verify(monoResponse, times(1)).block();
    }

    @Test
    void fetchAndStoreRates_EmptyResponse() {
        when(monoResponse.block()).thenReturn(Collections.emptyList());
        hnbCommunicatorService.fetchAndStoreRates(); // service should still have existing rates from initial fetch
        BigDecimal rate = hnbCommunicatorService.getMiddleRateUSD();
        assertEquals(0, rate.compareTo(exchangeRatesMap.get("USD")));
    }

    @Test
    void fetchAndStoreRates_WebClientException() {
        when(monoResponse.block()).thenThrow(new RuntimeException("Connection refused"));
        hnbCommunicatorService.fetchAndStoreRates(); // service should still have existing rates from initial fetch
        BigDecimal rate = hnbCommunicatorService.getMiddleRateUSD();
        assertEquals(0, rate.compareTo(exchangeRatesMap.get("USD")));
    }

    @Test
    void fetchAndStoreRates_IgnoresOutdatedApplicationDate() {
        HnbExchangeRateDto outDatedRate = new HnbExchangeRateDto();
        outDatedRate.setCurrency("USD");
        outDatedRate.setMiddleRate(new BigDecimal("2"));
        outDatedRate.setApplicationDate(LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        when(monoResponse.block()).thenReturn(List.of(outDatedRate));

        hnbCommunicatorService.fetchAndStoreRates(); // service should still have existing rates from initial fetch
        BigDecimal rate = hnbCommunicatorService.getMiddleRateUSD();
        assertEquals(0, rate.compareTo(exchangeRatesMap.get("USD")));
    }

    @Test
    void fetchAndStoreRates_UpdatesAccordingToApplicationDate() {
        String newRate = "2";

        HnbExchangeRateDto outDatedRate = new HnbExchangeRateDto();
        outDatedRate.setCurrency("USD");
        outDatedRate.setMiddleRate(new BigDecimal(newRate));
        outDatedRate.setApplicationDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        when(monoResponse.block()).thenReturn(List.of(outDatedRate));

        hnbCommunicatorService.fetchAndStoreRates();
        BigDecimal rate = hnbCommunicatorService.getMiddleRateUSD();
        assertEquals(0, rate.compareTo(new BigDecimal(newRate)));
    }

    @Test
    void getMiddleRateForCurrency_ExistingCurrency_Success() {
        BigDecimal rate = hnbCommunicatorService.getMiddleRateForCurrency("AUD");
        assertEquals(0, rate.compareTo(exchangeRatesMap.get("AUD")));
    }

    @Test
    void getMiddleRateForCurrency_NonExistentCurrency_ReturnsZero() {
        BigDecimal rate = hnbCommunicatorService.getMiddleRateForCurrency("GBP");
        assertEquals(0, rate.compareTo(BigDecimal.ZERO));
    }
}
