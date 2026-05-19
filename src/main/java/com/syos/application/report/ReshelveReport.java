package com.syos.application.report;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.domain.entity.Product;
import com.syos.domain.repository.BillReadRepository;
import com.syos.domain.repository.ProductReadRepository;

public class ReshelveReport extends ReportGenerator {
    private final BillReadRepository billRepository;
    private final ProductReadRepository productRepository;
    private final LocalDate date;

    public ReshelveReport(BillReadRepository billRepository, ProductReadRepository productRepository, LocalDate date) {
        this.billRepository = billRepository;
        this.productRepository = productRepository;
        this.date = date;
    }

    @Override
    protected String getReportHeader() {
        return "=".repeat(80) + "\n" +
               "RESHELVE REPORT - SYNEX OUTLET STORE\n" +
               "Date: " + date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\n" +
               "=".repeat(80);
    }

    @Override
    protected String generateReportBody() {
        List<Bill> bills = billRepository.findByDate(date);
        Map<String, Integer> totalsByProduct = new HashMap<>();

        for (Bill bill : bills) {
            for (BillItem item : bill.getItems()) {
                totalsByProduct.merge(item.getProductId().getValue(), item.getQuantity(), Integer::sum);
            }
        }

        if (totalsByProduct.isEmpty()) {
            return "\nNo sales recorded for this date (nothing to reshelve).\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-30s %-12s%n", "Product ID", "Name", "Quantity Sold"));
        sb.append("-".repeat(80)).append("\n");

        for (var entry : totalsByProduct.entrySet()) {
            String pid = entry.getKey();
            int qty = entry.getValue();
            Product prod = productRepository.findById(new com.syos.domain.valueobject.ProductId(pid)).orElse(null);
            String name = prod == null ? "<unknown>" : prod.getName();
            sb.append(String.format("%-10s %-30s %-12d%n", pid, name, qty));
        }

        return sb.toString();
    }
}
