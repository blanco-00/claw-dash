package com.clawdash.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExponentialBackoffTest {

    private static final int BASE_DELAY = 5;
    private static final int MAX_DELAY = 300;

    LocalDateTime calculateExponentialBackoff(int retryCount) {
        int delaySeconds = (int) Math.min(
                BASE_DELAY * Math.pow(2, retryCount),
                MAX_DELAY
        );
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    @Test
    void exponentialBackoff_FirstRetry() {
        LocalDateTime result = calculateExponentialBackoff(0);
        int expectedSecond = LocalDateTime.now().getSecond() + 5;
        assertEquals(expectedSecond % 60, result.getSecond());
    }

    @Test
    void exponentialBackoff_SecondRetry() {
        LocalDateTime result = calculateExponentialBackoff(1);
        int expectedSecond = LocalDateTime.now().getSecond() + 10;
        assertEquals(expectedSecond % 60, result.getSecond());
    }

    @Test
    void exponentialBackoff_ThirdRetry() {
        LocalDateTime result = calculateExponentialBackoff(2);
        int expectedSecond = LocalDateTime.now().getSecond() + 20;
        assertEquals(expectedSecond % 60, result.getSecond());
    }

    @Test
    void exponentialBackoff_FourthRetry() {
        LocalDateTime result = calculateExponentialBackoff(3);
        int expectedSecond = LocalDateTime.now().getSecond() + 40;
        assertEquals(expectedSecond % 60, result.getSecond());
    }

    @Test
    void exponentialBackoff_MaxCapped() {
        LocalDateTime result = calculateExponentialBackoff(10);
        int expectedSecond = LocalDateTime.now().getSecond() + 300;
        assertEquals(expectedSecond % 60, result.getSecond());
    }
}
