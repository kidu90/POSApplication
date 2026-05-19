package com.syos.application.builder;

import java.time.LocalDateTime;

import com.syos.domain.entity.Bill;
import com.syos.domain.valueobject.BillNumber;

public class BillBuilder {
    private String billNumber;
    private LocalDateTime timestamp;
    private Bill.SaleType saleType;
    private String customerName;
    private String customerAddress;

    public BillBuilder withBillNumber(String billNumber) {
        this.billNumber = billNumber;
        return this;
    }

    public BillBuilder withTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public BillBuilder withSaleType(Bill.SaleType saleType) {
        this.saleType = saleType;
        return this;
    }

    public BillBuilder forInStore() {
        this.saleType = Bill.SaleType.IN_STORE;
        return this;
    }

    public BillBuilder forOnline(String customerName, String customerAddress) {
        this.saleType = Bill.SaleType.ONLINE;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        return this;
    }

    public Bill build() {
        if (billNumber == null) {
            throw new IllegalStateException("Bill number is required");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (saleType == null) {
            throw new IllegalStateException("Sale type is required");
        }

        Bill bill = new Bill(new BillNumber(billNumber), timestamp, saleType);
        if (saleType == Bill.SaleType.ONLINE && customerName != null) {
            bill.setCustomerDetails(customerName, customerAddress);
        }
        return bill;
    }
}
