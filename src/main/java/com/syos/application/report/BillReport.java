package com.syos.application.report;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.syos.domain.entity.Bill;
import com.syos.domain.repository.BillReadRepository;

public class BillReport extends ReportGenerator {
    private final BillReadRepository billRepository;

    public BillReport(BillReadRepository billRepository) {
        this.billRepository = billRepository;
    }

    @Override
    protected String getReportHeader() {
        return "=".repeat(80) + "\n" +
               "BILL REPORT - SYNEX OUTLET STORE\n" +
               "=".repeat(80);
    }

    @Override
    protected String generateReportBody() {
        List<Bill> bills = billRepository.findAll();
        if (bills.isEmpty()) {
            return "\nNo bills available.\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        for (Bill bill : bills) {
            sb.append("Bill No: ").append(bill.getBillNumber()).append("\n");
            sb.append("Date/Time: ").append(bill.getTimestamp().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))).append("\n");
            sb.append("Type: ").append(bill.getSaleType()).append("\n");
            if (bill.getSaleType() == Bill.SaleType.ONLINE) {
                sb.append("Customer: ").append(bill.getCustomerName()).append("\n");
                sb.append("Address: ").append(bill.getCustomerAddress()).append("\n");
            }
            sb.append(String.format("%-10s %-30s %-8s %-10s %-10s%n", "Code", "Name", "Qty", "UnitPrice", "Batch"));
            for (var item : bill.getItems()) {
                sb.append(String.format("%-10s %-30s %-8d %-10s %-10s%n",
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getBatchNumber()));
            }
            sb.append(String.format("%-50s %-12s%n", "Subtotal:", bill.getSubtotal()));
            sb.append(String.format("%-50s %-12s%n", "Discount:", bill.getDiscount()));
            sb.append(String.format("%-50s %-12s%n", "TOTAL:", bill.getTotal()));
            sb.append("\n");
        }

        return sb.toString();
    }
}
