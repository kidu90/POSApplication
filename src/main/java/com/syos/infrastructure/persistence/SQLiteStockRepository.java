package com.syos.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.ProductId;
import com.syos.infrastructure.database.DatabaseManager;

public class SQLiteStockRepository implements StockRepository {
    private final DatabaseManager databaseManager;

    public SQLiteStockRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void save(StockBatch batch) {
        String sql = "INSERT OR REPLACE INTO stock_batches (batch_number, product_id, inventory_channel, quantity, expiry_date, received_date) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, batch.getBatchNumber().getValue());
            pstmt.setString(2, batch.getProductId().getValue());
            pstmt.setString(3, batch.getInventoryChannel().name());
            pstmt.setInt(4, batch.getQuantity());
            pstmt.setString(5, batch.getExpiryDate().toString());
            pstmt.setString(6, batch.getReceivedDate().toString());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save stock batch", e);
        }
    }

    @Override
    public Optional<StockBatch> findByBatchNumber(BatchNumber batchNumber) {
        String sql = "SELECT * FROM stock_batches WHERE batch_number = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, batchNumber.getValue());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToStockBatch(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find stock batch", e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<StockBatch> findByProductId(ProductId productId) {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches WHERE product_id = ? ORDER BY received_date";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productId.getValue());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                batches.add(mapResultSetToStockBatch(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find stock batches by product", e);
        }
        
        return batches;
    }

    @Override
    public List<StockBatch> findByProductIdAndChannel(ProductId productId, InventoryChannel channel) {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches WHERE product_id = ? AND inventory_channel = ? ORDER BY received_date";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productId.getValue());
            pstmt.setString(2, channel.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                batches.add(mapResultSetToStockBatch(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find stock batches by product and channel", e);
        }

        return batches;
    }

    @Override
    public List<StockBatch> findAll() {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches ORDER BY product_id, received_date";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                batches.add(mapResultSetToStockBatch(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all stock batches", e);
        }
        
        return batches;
    }

    @Override
    public List<StockBatch> findExpired() {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches WHERE expiry_date < ? AND quantity > 0 ORDER BY expiry_date";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                batches.add(mapResultSetToStockBatch(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find expired batches", e);
        }
        
        return batches;
    }

    @Override
    public List<StockBatch> findExpiringSoon(int daysThreshold) {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches WHERE expiry_date >= ? AND expiry_date <= ? AND quantity > 0 ORDER BY expiry_date";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            LocalDate now = LocalDate.now();
            LocalDate threshold = now.plusDays(daysThreshold);
            
            pstmt.setString(1, now.toString());
            pstmt.setString(2, threshold.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                batches.add(mapResultSetToStockBatch(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find expiring batches", e);
        }
        
        return batches;
    }

    @Override
    public int getTotalQuantityForProduct(ProductId productId) {
        String sql = "SELECT SUM(quantity) as total FROM stock_batches WHERE product_id = ? AND expiry_date >= ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productId.getValue());
            pstmt.setString(2, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get total quantity", e);
        }
        
        return 0;
    }

    @Override
    public int getTotalQuantityForProductAndChannel(ProductId productId, InventoryChannel channel) {
        String sql = "SELECT SUM(quantity) as total FROM stock_batches WHERE product_id = ? AND inventory_channel = ? AND expiry_date >= ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productId.getValue());
            pstmt.setString(2, channel.name());
            pstmt.setString(3, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get total quantity by channel", e);
        }

        return 0;
    }

    @Override
    public void update(StockBatch batch) {
        save(batch);
    }

    private StockBatch mapResultSetToStockBatch(ResultSet rs) throws SQLException {
        String channelValue = rs.getString("inventory_channel");
        InventoryChannel channel = channelValue == null ? InventoryChannel.STORE : InventoryChannel.valueOf(channelValue);
        return new StockBatch(
            new BatchNumber(rs.getString("batch_number")),
            new ProductId(rs.getString("product_id")),
            channel,
            rs.getInt("quantity"),
            LocalDate.parse(rs.getString("expiry_date")),
            LocalDate.parse(rs.getString("received_date"))
        );
    }
}
