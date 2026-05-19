package com.syos.presentation.gui;

import java.time.format.DateTimeFormatter;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;

public final class BillTextFormatter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private BillTextFormatter() {
    }

    public static String format(Bill bill) {
        StringBuilder output = new StringBuilder();
        output.append("\n").append("-".repeat(80)).append("\n");
        output.append("SYNEX OUTLET STORE\n");
        output.append("GST No: 29XXXXX1234X1Z5\n");
        output.append("-".repeat(80)).append("\n");
        output.append("Bill No: ").append(bill.getBillNumber()).append("\n");
        output.append("Date/Time: ").append(bill.getTimestamp().format(DATE_TIME_FORMATTER)).append("\n");
        output.append("Type: ").append(bill.getSaleType()).append("\n");

        if (bill.getSaleType() == Bill.SaleType.ONLINE) {
            output.append("Customer: ").append(bill.getCustomerName()).append("\n");
            output.append("Address: ").append(bill.getCustomerAddress()).append("\n");
        }

        output.append("-".repeat(80)).append("\n");
        output.append(String.format("%-30s %-8s %-12s %-12s%n", "Item", "Qty", "Rate", "Amount"));
        output.append("-".repeat(80)).append("\n");

        for (BillItem item : bill.getItems()) {
            output.append(String.format("%-30s %-8d %-12s %-12s%n",
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()));
        }

        output.append("-".repeat(80)).append("\n");
        output.append(String.format("%-50s %-12s%n", "Subtotal:", bill.getSubtotal()));
        output.append(String.format("%-50s %-12s%n", "Discount:", bill.getDiscount()));
        output.append(String.format("%-50s %-12s%n", "TOTAL:", bill.getTotal()));
        output.append("-".repeat(80)).append("\n");
        return output.toString();
    }
}