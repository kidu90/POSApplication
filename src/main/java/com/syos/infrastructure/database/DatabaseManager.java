package com.syos.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final String databaseUrl;

    private DatabaseManager(String databasePath) {
        this.databaseUrl = "jdbc:sqlite:" + databasePath;
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance(String databasePath) {
        if (instance == null) {
            instance = new DatabaseManager(databasePath);
        }
        return instance;
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            return getInstance("syos.db");
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        instance = null;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                "id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "unit_price REAL NOT NULL," +
                "unit TEXT NOT NULL" +
                ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "full_name TEXT NOT NULL," +
                "address TEXT NOT NULL" +
                ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS stock_batches (" +
                "batch_number TEXT PRIMARY KEY," +
                "product_id TEXT NOT NULL," +
                "inventory_channel TEXT NOT NULL DEFAULT 'STORE'," +
                "quantity INTEGER NOT NULL," +
                "expiry_date TEXT NOT NULL," +
                "received_date TEXT NOT NULL," +
                "FOREIGN KEY (product_id) REFERENCES products(id)" +
                ")");

            try {
                stmt.execute("ALTER TABLE stock_batches ADD COLUMN inventory_channel TEXT NOT NULL DEFAULT 'STORE'");
            } catch (SQLException ignored) {
                // Column already exists
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS bills (" +
                "bill_number TEXT PRIMARY KEY," +
                "timestamp TEXT NOT NULL," +
                "sale_type TEXT NOT NULL," +
                "subtotal REAL NOT NULL," +
                "discount REAL NOT NULL," +
                "total REAL NOT NULL," +
                "customer_name TEXT," +
                "customer_address TEXT" +
                ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS bill_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "bill_number TEXT NOT NULL," +
                "product_id TEXT NOT NULL," +
                "product_name TEXT NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "unit_price REAL NOT NULL," +
                "batch_number TEXT NOT NULL," +
                "line_total REAL NOT NULL," +
                "discount REAL NOT NULL," +
                "FOREIGN KEY (bill_number) REFERENCES bills(bill_number)" +
                ")");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public void resetDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS bill_items");
            stmt.execute("DROP TABLE IF EXISTS bills");
            stmt.execute("DROP TABLE IF EXISTS stock_batches");
            stmt.execute("DROP TABLE IF EXISTS products");
            initializeDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reset database", e);
        }
    }
}
