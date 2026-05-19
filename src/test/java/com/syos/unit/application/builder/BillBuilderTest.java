package com.syos.unit.application.builder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.syos.application.builder.BillBuilder;
import com.syos.domain.entity.Bill;

class BillBuilderTest {

    @Test
    void shouldBuildInStoreBill() {
        Bill bill = new BillBuilder()
            .withBillNumber("POS-20260201-00001")
            .withTimestamp(LocalDateTime.now())
            .forInStore()
            .build();

        assertNotNull(bill);
        assertEquals("POS-20260201-00001", bill.getBillNumber().getValue());
        assertEquals(Bill.SaleType.IN_STORE, bill.getSaleType());
    }

    @Test
    void shouldBuildOnlineBillWithCustomerDetails() {
        Bill bill = new BillBuilder()
            .withBillNumber("ONL-20260201-00001")
            .withTimestamp(LocalDateTime.now())
            .forOnline("John Doe", "123 Main St")
            .build();

        assertNotNull(bill);
        assertEquals("ONL-20260201-00001", bill.getBillNumber().getValue());
        assertEquals(Bill.SaleType.ONLINE, bill.getSaleType());
        assertEquals("John Doe", bill.getCustomerName());
        assertEquals("123 Main St", bill.getCustomerAddress());
    }

    @Test
    void shouldThrowExceptionWhenBillNumberMissing() {
        BillBuilder builder = new BillBuilder()
            .withTimestamp(LocalDateTime.now())
            .forInStore();

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void shouldThrowExceptionWhenSaleTypeMissing() {
        BillBuilder builder = new BillBuilder()
            .withBillNumber("TEST-001")
            .withTimestamp(LocalDateTime.now());

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void shouldUseCurrentTimeWhenTimestampNotProvided() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        Bill bill = new BillBuilder()
            .withBillNumber("TEST-001")
            .forInStore()
            .build();

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertTrue(bill.getTimestamp().isAfter(before));
        assertTrue(bill.getTimestamp().isBefore(after));
    }
}
