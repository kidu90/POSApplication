package com.syos.infrastructure.persistence;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.domain.repository.BillRepository;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.BillNumber;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;
import com.syos.infrastructure.database.DatabaseManager;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteBillRepository implements BillRepository {
    private final DatabaseManager databaseManager;

    public SQLiteBillRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void save(Bill bill) {
        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                String billSql = "INSERT OR REPLACE INTO bills (bill_number, timestamp, sale_type, subtotal, discount, total, customer_name, customer_address) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(billSql)) {
                    pstmt.setString(1, bill.getBillNumber().getValue());
                    pstmt.setString(2, bill.getTimestamp().toString());
                    pstmt.setString(3, bill.getSaleType().name());
                    pstmt.setDouble(4, bill.getSubtotal().getAmount().doubleValue());
                    pstmt.setDouble(5, bill.getDiscount().getAmount().doubleValue());
                    pstmt.setDouble(6, bill.getTotal().getAmount().doubleValue());
                    pstmt.setString(7, bill.getCustomerName());
                    pstmt.setString(8, bill.getCustomerAddress());
                    pstmt.executeUpdate();
                }

                String itemSql = "INSERT INTO bill_items (bill_number, product_id, product_name, quantity, unit_price, batch_number, line_total, discount) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                    for (BillItem item : bill.getItems()) {
                        pstmt.setString(1, bill.getBillNumber().getValue());
                        pstmt.setString(2, item.getProductId().getValue());
                        pstmt.setString(3, item.getProductName());
                        pstmt.setInt(4, item.getQuantity());
                        pstmt.setDouble(5, item.getUnitPrice().getAmount().doubleValue());
                        pstmt.setString(6, item.getBatchNumber().getValue());
                        pstmt.setDouble(7, item.getLineTotal().getAmount().doubleValue());
                        pstmt.setDouble(8, item.getDiscount().getAmount().doubleValue());
                        pstmt.executeUpdate();
                    }
                }
                
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save bill", e);
        }
    }

    @Override
    public Optional<Bill> findByBillNumber(BillNumber billNumber) {
        String sql = "SELECT * FROM bills WHERE bill_number = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, billNumber.getValue());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Bill bill = mapResultSetToBill(rs);
                loadBillItems(bill);
                return Optional.of(bill);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find bill", e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<Bill> findAll() {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills ORDER BY timestamp DESC";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Bill bill = mapResultSetToBill(rs);
                loadBillItems(bill);
                bills.add(bill);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all bills", e);
        }
        
        return bills;
    }

    @Override
    public List<Bill> findByDate(LocalDate date) {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE DATE(timestamp) = ? ORDER BY timestamp";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, date.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bill bill = mapResultSetToBill(rs);
                loadBillItems(bill);
                bills.add(bill);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find bills by date", e);
        }
        
        return bills;
    }

    @Override
    public List<Bill> findBySaleType(Bill.SaleType saleType) {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE sale_type = ? ORDER BY timestamp DESC";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, saleType.name());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bill bill = mapResultSetToBill(rs);
                loadBillItems(bill);
                bills.add(bill);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find bills by sale type", e);
        }
        
        return bills;
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill(
            new BillNumber(rs.getString("bill_number")),
            LocalDateTime.parse(rs.getString("timestamp")),
            Bill.SaleType.valueOf(rs.getString("sale_type"))
        );

        String customerName = rs.getString("customer_name");
        String customerAddress = rs.getString("customer_address");
        if (customerName != null) {
            bill.setCustomerDetails(customerName, customerAddress);
        }

        return bill;
    }

    private void loadBillItems(Bill bill) {
        String sql = "SELECT * FROM bill_items WHERE bill_number = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, bill.getBillNumber().getValue());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                BillItem item = new BillItem(
                    new ProductId(rs.getString("product_id")),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    new Money(rs.getDouble("unit_price")),
                    new BatchNumber(rs.getString("batch_number"))
                );
                
                double discount = rs.getDouble("discount");
                if (discount > 0) {
                    item.applyDiscount(new Money(discount));
                }
                
                bill.addItem(item);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load bill items", e);
        }
    }
}
