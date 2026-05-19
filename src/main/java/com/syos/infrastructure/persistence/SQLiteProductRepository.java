package com.syos.infrastructure.persistence;

import com.syos.domain.entity.Product;
import com.syos.domain.repository.ProductRepository;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;
import com.syos.infrastructure.database.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteProductRepository implements ProductRepository {
    private final DatabaseManager databaseManager;

    public SQLiteProductRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void save(Product product) {
        String sql = "INSERT OR REPLACE INTO products (id, name, category, unit_price, unit) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getId().getValue());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getCategory());
            pstmt.setDouble(4, product.getUnitPrice().getAmount().doubleValue());
            pstmt.setString(5, product.getUnit());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save product", e);
        }
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id.getValue());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find product", e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all products", e);
        }
        
        return products;
    }

    @Override
    public List<Product> findByCategory(String category) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ? ORDER BY id";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find products by category", e);
        }
        
        return products;
    }

    @Override
    public void delete(ProductId id) {
        String sql = "DELETE FROM products WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id.getValue());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
            new ProductId(rs.getString("id")),
            rs.getString("name"),
            rs.getString("category"),
            new Money(rs.getDouble("unit_price")),
            rs.getString("unit")
        );
    }
}
