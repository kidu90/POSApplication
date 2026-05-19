package com.syos.presentation.console;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;

public class BillPrinter {
    public void print(Bill bill) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println(" ".repeat(25) + "SYNEX OUTLET STORE");
        System.out.println(" ".repeat(20) + "GST No: 29XXXXX1234X1Z5");
        System.out.println("-".repeat(80));
        System.out.println("Bill No: " + bill.getBillNumber());
        System.out.println("Date/Time: " + bill.getTimestamp());
        System.out.println("Type: " + bill.getSaleType());

        if (bill.getSaleType() == Bill.SaleType.ONLINE) {
            System.out.println("Customer: " + bill.getCustomerName());
            System.out.println("Address: " + bill.getCustomerAddress());
        }

        System.out.println("-".repeat(80));
        System.out.println(String.format("%-30s %-8s %-12s %-12s", "Item", "Qty", "Rate", "Amount"));
        System.out.println("-".repeat(80));

        for (BillItem item : bill.getItems()) {
            System.out.println(String.format("%-30s %-8d %-12s %-12s",
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()));
        }

        System.out.println("-".repeat(80));
        System.out.println(String.format("%-50s %-12s", "Subtotal:", bill.getSubtotal()));
        System.out.println(String.format("%-50s %-12s", "Discount:", bill.getDiscount()));
        System.out.println(String.format("%-50s %-12s", "TOTAL:", bill.getTotal()));
        System.out.println("-".repeat(80));
    }
}
