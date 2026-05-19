package com.syos.unit.gui;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;
import com.syos.presentation.gui.BillTextFormatter;

/**
 * Verifies the printable bill formatter because GUI output should remain deterministic and presentation-only.
 */
class BillFormatterTest {

    @Test
    void shouldNotIncludeCustomerDetailsForInStoreBill() {
        Bill bill = createBill(Bill.SaleType.IN_STORE);
        String output = BillTextFormatter.format(bill);

        assertAll(
            () -> assertFalse(output.contains("Customer:")),
            () -> assertFalse(output.contains("Address:"))
        );
    }

    @Test
    void shouldIncludeCustomerDetailsForOnlineBill() {
        Bill bill = createBill(Bill.SaleType.ONLINE);
        bill.setCustomerDetails("Alice", "1 Test Lane");
        String output = BillTextFormatter.format(bill);

        assertAll(
            () -> assertTrue(output.contains("Customer: Alice")),
            () -> assertTrue(output.contains("Address: 1 Test Lane"))
        );
    }

    @Test
    void shouldIncludeBillSummaryFields() {
        Bill bill = createBill(Bill.SaleType.IN_STORE);
        String output = BillTextFormatter.format(bill);

        assertAll(
            () -> assertTrue(output.contains("Bill No:")),
            () -> assertTrue(output.contains("Date/Time:")),
            () -> assertTrue(output.contains("Subtotal:")),
            () -> assertTrue(output.contains("Discount:")),
            () -> assertTrue(output.contains("TOTAL:"))
        );
    }

    @Test
    void shouldShowZeroDiscountWhenNoDiscountApplied() {
        Bill bill = createBill(Bill.SaleType.IN_STORE);
        String output = BillTextFormatter.format(bill);

        assertTrue(output.contains("Discount:"));
    }

    @Test
    void shouldListEachItemOnItsOwnLine() {
        Bill bill = createBill(Bill.SaleType.IN_STORE);
        bill.addItem(new BillItem(new ProductId("P002"), "Tea", 2, new Money(5), new BatchNumber("B002")));
        String output = BillTextFormatter.format(bill);

        assertAll(
            () -> assertTrue(output.contains("Rice")),
            () -> assertTrue(output.contains("Tea"))
        );
    }

    private static Bill createBill(Bill.SaleType saleType) {
        Bill bill = new Bill(new com.syos.domain.valueobject.BillNumber("POS-20260201-00001"), LocalDateTime.now(), saleType);
        bill.addItem(new BillItem(new ProductId("P001"), "Rice", 1, new Money(10), new BatchNumber("B001")));
        return bill;
    }
}
