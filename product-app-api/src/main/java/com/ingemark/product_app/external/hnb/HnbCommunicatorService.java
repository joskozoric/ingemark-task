package com.ingemark.product_app.external.hnb;

import com.ingemark.product_app.external.hnb.dto.HnbExchangeRateDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HnbCommunicatorService {

    private static final Logger log = LoggerFactory.getLogger(HnbCommunicatorService.class);

    private final Map<String, HnbExchangeRateDto> currentRates = new ConcurrentHashMap<>(20, 0.75f, 1); // only the refresh task can write

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

            if (CollectionUtils.isEmpty(newRates)) {
                log.warn("HNB returned an empty list of exchange rates");
                return;
            }

            newRates.forEach(this::insertOrUpdateExistingRate);
            log.info("Successfully fetched and stored {} exchange rates.",  newRates.size());
        } catch (Exception e) {
            log.error("Error fetching exchange rates from HNB: {}", e.getMessage(), e);
        }
    }

    void insertOrUpdateExistingRate(HnbExchangeRateDto newRate) {
        currentRates.merge(newRate.getCurrency(), newRate, (existingValue, newValue) -> {
            try {
                // compare application dates
                LocalDate newRateDate = LocalDate.parse(newValue.getApplicationDate(),
                        DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDate currentRateDate = LocalDate.parse(existingValue.getApplicationDate(),
                        DateTimeFormatter.ISO_LOCAL_DATE);

                if(newRateDate.isAfter(currentRateDate)){
                    return newValue;
                }
            } catch (Exception e) {
                log.warn("Exception while parsing application date for: {}, skipping...", newValue.getCurrency());
            }
            return existingValue;
        });
    }

    public BigDecimal getMiddleRateUSD() {
        return getMiddleRateForCurrency("USD");
    }

    public BigDecimal getMiddleRateForCurrency(String currency) {
            return currentRates.containsKey(currency) ? currentRates.get(currency).getMiddleRate() : BigDecimal.ZERO;
    }
}
