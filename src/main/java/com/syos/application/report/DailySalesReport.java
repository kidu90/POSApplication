package com.syos.application.report;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.syos.domain.entity.Bill;
import com.syos.domain.repository.BillReadRepository;
import com.syos.domain.valueobject.Money;

public class DailySalesReport extends ReportGenerator {
    private final BillReadRepository billRepository;
    private final LocalDate date;

    public DailySalesReport(BillReadRepository billRepository, LocalDate date) {
        this.billRepository = billRepository;
        this.date = date;
    }

    @Override
    protected String getReportHeader() {
        return "=".repeat(80) + "\n" +
               "DAILY SALES REPORT - SYNEX OUTLET STORE\n" +
               "Date: " + date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\n" +
               "=".repeat(80);
    }

    @Override
    protected String generateReportBody() {
        List<Bill> bills = billRepository.findByDate(date);
        
        if (bills.isEmpty()) {
            return "\nNo sales recorded for this date.\n";
        }

        StringBuilder body = new StringBuilder();
        
        Money totalInStore = new Money(0);
        Money totalOnline = new Money(0);
        int totalItemsInStore = 0;
        int totalItemsOnline = 0;

        body.append("\nIN-STORE SALES:\n");
        body.append("-".repeat(80)).append("\n");
        body.append(String.format("%-15s %-20s %-15s %-15s%n", "Bill Number", "Time", "Items", "Total"));
        body.append("-".repeat(80)).append("\n");

        for (Bill bill : bills) {
            if (bill.getSaleType() == Bill.SaleType.IN_STORE) {
                body.append(String.format("%-15s %-20s %-15d %-15s%n",
                    bill.getBillNumber(),
                    bill.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    bill.getItems().size(),
                    bill.getTotal()));
                totalInStore = totalInStore.add(bill.getTotal());
                totalItemsInStore += bill.getItems().size();
            }
        }

        body.append("\nONLINE SALES:\n");
        body.append("-".repeat(80)).append("\n");
        body.append(String.format("%-15s %-25s %-15s %-15s%n", "Bill Number", "Customer", "Items", "Total"));
        body.append("-".repeat(80)).append("\n");

        for (Bill bill : bills) {
            if (bill.getSaleType() == Bill.SaleType.ONLINE) {
                body.append(String.format("%-15s %-25s %-15d %-15s%n",
                    bill.getBillNumber(),
                    bill.getCustomerName(),
                    bill.getItems().size(),
                    bill.getTotal()));
                totalOnline = totalOnline.add(bill.getTotal());
                totalItemsOnline += bill.getItems().size();
            }
        }

        body.append("\nSUMMARY:\n");
        body.append("-".repeat(80)).append("\n");
        body.append(String.format("In-Store Sales: %d transactions, %d items, %s%n",
            bills.stream().filter(b -> b.getSaleType() == Bill.SaleType.IN_STORE).count(),
            totalItemsInStore,
            totalInStore));
        body.append(String.format("Online Sales: %d transactions, %d items, %s%n",
            bills.stream().filter(b -> b.getSaleType() == Bill.SaleType.ONLINE).count(),
            totalItemsOnline,
            totalOnline));
        body.append(String.format("TOTAL: %d transactions, %d items, %s%n",
            bills.size(),
            totalItemsInStore + totalItemsOnline,
            totalInStore.add(totalOnline)));

        return body.toString();
    }
}
