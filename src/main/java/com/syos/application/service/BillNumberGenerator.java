package com.syos.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class BillNumberGenerator implements BillNumberService {
    private static BillNumberGenerator instance;
    private final AtomicLong counter;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private BillNumberGenerator() {
        this.counter = new AtomicLong(1);
    }

    public static synchronized BillNumberGenerator getInstance() {
        if (instance == null) {
            instance = new BillNumberGenerator();
        }
        return instance;
    }

    @Override
    public String generateBillNumber(String prefix) {
        LocalDateTime referenceDate = LocalDateTime.of(2026, 2, 1, 0, 0);
        String date = referenceDate.format(DATE_FORMAT);
        long sequence = counter.getAndIncrement();
        return String.format("%s-%s-%05d", prefix, date, sequence);
    }

    public void reset() {
        counter.set(1);
    }
}
