package com.syos.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.syos.domain.valueobject.BillNumber;
import com.syos.domain.valueobject.Money;

public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;

    private final BillNumber billNumber;
    private final LocalDateTime timestamp;
    private final SaleType saleType;
    private final List<BillItem> items;
    private Money subtotal;
    private Money discount;
    private Money total;
    private String customerName;
    private String customerAddress;

    public enum SaleType {
        IN_STORE,
        ONLINE
    }

    public Bill(BillNumber billNumber, LocalDateTime timestamp, SaleType saleType) {
        this.billNumber = billNumber;
        this.timestamp = timestamp;
        this.saleType = saleType;
        this.items = new ArrayList<>();
        this.subtotal = new Money(0);
        this.discount = new Money(0);
        this.total = new Money(0);
    }

    public void addItem(BillItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Bill item cannot be null");
        }
        items.add(item);
        recalculateTotal();
    }

    public void applyDiscount(Money discountAmount) {
        if (discountAmount == null) {
            throw new IllegalArgumentException("Discount amount cannot be null");
        }
        this.discount = discountAmount;
        recalculateTotal();
    }

    public void setCustomerDetails(String name, String address) {
        this.customerName = name;
        this.customerAddress = address;
    }

    private void recalculateTotal() {
        Money itemsTotal = new Money(0);
        for (BillItem item : items) {
            itemsTotal = itemsTotal.add(item.getLineTotal());
        }
        this.subtotal = itemsTotal;
        this.total = subtotal.subtract(discount);
    }

    public BillNumber getBillNumber() {
        return billNumber;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public SaleType getSaleType() {
        return saleType;
    }

    public List<BillItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public Money getDiscount() {
        return discount;
    }

    public Money getTotal() {
        return total;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bill bill = (Bill) o;
        return billNumber.equals(bill.billNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billNumber);
    }
}
