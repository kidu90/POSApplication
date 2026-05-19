package com.syos.unit.application.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.syos.application.service.BillNumberGenerator;

class BillNumberGeneratorTest {

    @BeforeEach
    void setUp() {
        BillNumberGenerator.getInstance().reset();
    }

    @Test
    void shouldGenerateUniqueBillNumbers() {
        BillNumberGenerator generator = BillNumberGenerator.getInstance();

        String billNumber1 = generator.generateBillNumber("POS");
        String billNumber2 = generator.generateBillNumber("POS");

        assertNotEquals(billNumber1, billNumber2);
    }

    @Test
    void shouldIncludePrefix() {
        BillNumberGenerator generator = BillNumberGenerator.getInstance();

        String billNumber = generator.generateBillNumber("POS");

        assertTrue(billNumber.startsWith("POS-"));
    }

    @Test
    void shouldIncludeDate() {
        BillNumberGenerator generator = BillNumberGenerator.getInstance();

        String billNumber = generator.generateBillNumber("POS");

        assertTrue(billNumber.contains("20260201"));
    }

    @Test
    void shouldGenerateSequentialNumbers() {
        BillNumberGenerator generator = BillNumberGenerator.getInstance();

        String billNumber1 = generator.generateBillNumber("POS");
        String billNumber2 = generator.generateBillNumber("POS");

        assertTrue(billNumber1.endsWith("00001"));
        assertTrue(billNumber2.endsWith("00002"));
    }

    @Test
    void shouldReturnSameInstance() {
        BillNumberGenerator instance1 = BillNumberGenerator.getInstance();
        BillNumberGenerator instance2 = BillNumberGenerator.getInstance();

        assertSame(instance1, instance2);
    }
}
