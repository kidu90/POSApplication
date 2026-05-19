package com.syos.unit.domain.entity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.BillNumber;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;

class BillTest {

    private Bill bill;

    @BeforeEach
    void setUp() {
        bill = new Bill(
            new BillNumber("POS-20260201-00001"),
            LocalDateTime.now(),
            Bill.SaleType.IN_STORE
        );
    }

    @Test
    void shouldCreateBillWithValidData() {
        assertEquals("POS-20260201-00001", bill.getBillNumber().getValue());
        assertEquals(Bill.SaleType.IN_STORE, bill.getSaleType());
        assertTrue(bill.getItems().isEmpty());
        assertEquals(new Money(0), bill.getTotal());
    }

    @Test
    void shouldAddItemAndCalculateTotal() {
        BillItem item = new BillItem(
            new ProductId("P001"),
            "Rice",
            5,
            new Money(85.00),
            new BatchNumber("B001")
        );

        bill.addItem(item);

        assertEquals(1, bill.getItems().size());
        assertEquals(new Money(425.00), bill.getSubtotal());
        assertEquals(new Money(425.00), bill.getTotal());
    }

    @Test
    void shouldAddMultipleItemsAndCalculateTotal() {
        BillItem item1 = new BillItem(
            new ProductId("P001"),
            "Rice",
            5,
            new Money(85.00),
            new BatchNumber("B001")
        );

        BillItem item2 = new BillItem(
            new ProductId("P002"),
            "Milk",
            3,
            new Money(65.00),
            new BatchNumber("B002")
        );

        bill.addItem(item1);
        bill.addItem(item2);

        assertEquals(2, bill.getItems().size());
        assertEquals(new Money(620.00), bill.getSubtotal());
    }

    @Test
    void shouldApplyDiscountCorrectly() {
        BillItem item = new BillItem(
            new ProductId("P001"),
            "Rice",
            5,
            new Money(85.00),
            new BatchNumber("B001")
        );

        bill.addItem(item);
        bill.applyDiscount(new Money(50.00));

        assertEquals(new Money(425.00), bill.getSubtotal());
        assertEquals(new Money(50.00), bill.getDiscount());
        assertEquals(new Money(375.00), bill.getTotal());
    }

    @Test
    void shouldSetCustomerDetailsForOnlineSale() {
        Bill onlineBill = new Bill(
            new BillNumber("ONL-20260201-00001"),
            LocalDateTime.now(),
            Bill.SaleType.ONLINE
        );

        onlineBill.setCustomerDetails("John Doe", "123 Main St");

        assertEquals("John Doe", onlineBill.getCustomerName());
        assertEquals("123 Main St", onlineBill.getCustomerAddress());
    }
}
