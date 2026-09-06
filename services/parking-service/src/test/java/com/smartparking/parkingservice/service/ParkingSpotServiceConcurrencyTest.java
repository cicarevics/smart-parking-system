package com.smartparking.parkingservice.service;

import com.smartparking.parkingservice.model.ParkingLot;
import com.smartparking.parkingservice.model.ParkingSpot;
import com.smartparking.parkingservice.repository.ParkingLotRepository;
import com.smartparking.parkingservice.repository.ParkingSpotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// Proves the atomicity ParkingSpotRepository.claimIfAvailable() is meant to
// guarantee (see its @Modifying conditional UPDATE) at the actual database
// level -- a mocked repository can only verify the service's *reaction* to
// 0-vs-1 updated rows, not that Postgres genuinely serializes the race.
@Testcontainers
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class ParkingSpotServiceConcurrencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ParkingSpotService spotService;

    @Autowired
    private ParkingLotRepository lotRepository;

    @Autowired
    private ParkingSpotRepository spotRepository;

    @Test
    void claim_concurrentCallsOnSameSpot_onlyOneSucceeds() throws Exception {
        ParkingLot lot = lotRepository.save(new ParkingLot("Downtown Garage", "123 Main St"));
        ParkingSpot spot = spotRepository.save(new ParkingSpot(lot.getId(), "A1"));

        int attempts = 8;
        List<Callable<Boolean>> claimAttempts = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            claimAttempts.add(() -> {
                try {
                    spotService.claim(spot.getId());
                    return true;
                } catch (RuntimeException expectedForLosers) {
                    return false;
                }
            });
        }

        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        List<Future<Boolean>> results = executor.invokeAll(claimAttempts);
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        long successCount = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                successCount++;
            }
        }

        assertThat(successCount).isEqualTo(1);
        assertThat(spotRepository.findById(spot.getId()).orElseThrow().isAvailable()).isFalse();
    }
}
