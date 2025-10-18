package com.ingemark.product_app.external.hnb;

import com.ingemark.product_app.external.hnb.dto.HnbExchangeRateDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class HnbCommunicatorService {

    private static final Logger log = LoggerFactory.getLogger(HnbCommunicatorService.class);

    private final List<HnbExchangeRateDto> currentRates = new CopyOnWriteArrayList<>();

    private final WebClient webClient;

    public HnbCommunicatorService(WebClient hnbCommunicatorWebClient) {
        this.webClient = hnbCommunicatorWebClient;
        // initial fetch on startup
        fetchAndStoreRates();
    }

    @Scheduled(cron = "${external.hnb.refresh.cron:0 0 6 * * *}") // refresh every day at 6am
    public void fetchAndStoreRates() {
        log.info("Started refresh rates task");
        try {
            List<HnbExchangeRateDto> newRates = webClient.get()
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<HnbExchangeRateDto>>() {})
                    .block();

            if (newRates != null && !newRates.isEmpty()) {
                currentRates.clear();
                currentRates.addAll(newRates);
                log.info("Successfully fetched and stored {} exchange rates. Effective date: {}",
                        newRates.size(), newRates.get(0).getApplicationDate());
            } else {
                log.warn("HNB returned an empty list of exchange rates");
            }
        } catch (Exception e) {
            log.error("Error fetching exchange rates from HNB: {}", e.getMessage(), e);
        }
    }

    public BigDecimal getMiddleRateUSD() {
        return getMiddleRateForCurrency("USD");
    }

    public BigDecimal getMiddleRateForCurrency(String currency) {
            return currentRates.stream()
                    .filter(dto -> dto.getCurrency() != null && dto.getCurrency().equalsIgnoreCase(currency))
                    .map(HnbExchangeRateDto::getMiddleRate)
                    .findFirst().orElse(BigDecimal.ZERO);
    }
}
