package com.syos.presentation.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.syos.domain.entity.Product;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.valueobject.InventoryChannel;

public class StockManagementFrame extends JFrame {
    private final GuiAppContext context;
    private final DefaultTableModel stockTableModel = new DefaultTableModel(new Object[] {"Batch Number", "Product", "Channel", "Quantity", "Expiry", "Received Date"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel productTableModel = new DefaultTableModel(new Object[] {"ID", "Name", "Category", "Price", "Unit"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTextField batchNumberField = new JTextField(10);
    private final JComboBox<Product> productCombo = new JComboBox<>();
    private final JComboBox<InventoryChannel> channelCombo = new JComboBox<>(InventoryChannel.values());
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
    private final JTextField expiryDateField = new JTextField(10);
    private final JTextField receivedDateField = new JTextField(10);
    private final JTextField productIdField = new JTextField(10);
    private final JTextField productNameField = new JTextField(14);
    private final JTextField categoryField = new JTextField(12);
    private final JTextField priceField = new JTextField(8);
    private final JTextField unitField = new JTextField(8);
    private final Map<String, String> productNameLookup = new LinkedHashMap<>();
    private JTable stockTable;

    public StockManagementFrame(GuiAppContext context) {
        this.context = context;
        setTitle("Stock Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null);
        buildUi();
        refreshAll();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("View Current Stock", buildCurrentStockPanel());
        tabs.addTab("Add Stock", buildAddStockPanel());
        tabs.addTab("Products", buildProductsPanel());
        add(tabs, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> {
            stopStockPolling();
            dispose();
            new MainMenuFrame(context).setVisible(true);
        });
        footer.add(backButton);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildCurrentStockPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        stockTable = new JTable(stockTableModel);
        panel.add(new JScrollPane(stockTable), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshStockTable(false));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAddStockPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Batch Number:"));
        form.add(batchNumberField);
        form.add(new JLabel("Product:"));
        form.add(productCombo);
        form.add(new JLabel("Channel:"));
        form.add(channelCombo);
        form.add(new JLabel("Quantity:"));
        form.add(quantitySpinner);
        form.add(new JLabel("Expiry Date (yyyy-MM-dd):"));
        form.add(expiryDateField);
        form.add(new JLabel("Received Date (yyyy-MM-dd):"));
        form.add(receivedDateField);

        JButton submitButton = new JButton("Submit Stock");
        submitButton.addActionListener(e -> addStock());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearStockForm());

        JPanel actions = new JPanel();
        actions.add(submitButton);
        actions.add(clearButton);

        panel.add(form, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JTable table = new JTable(productTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Product ID:"));
        form.add(productIdField);
        form.add(new JLabel("Name:"));
        form.add(productNameField);
        form.add(new JLabel("Category:"));
        form.add(categoryField);
        form.add(new JLabel("Price:"));
        form.add(priceField);
        form.add(new JLabel("Unit:"));
        form.add(unitField);

        JButton addButton = new JButton("Add Product");
        addButton.addActionListener(e -> addProduct());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAll());

        JPanel actions = new JPanel();
        actions.add(addButton);
        actions.add(refreshButton);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(actions, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    public void refreshAll() {
        refreshProducts();
        refreshStockTable(false);
    }

    private void refreshProducts() {
        new SwingWorker<List<Product>, Void>() {
            @Override
            protected List<Product> doInBackground() {
                return context.getClientService().getProducts();
            }

            @Override
            protected void done() {
                try {
                    List<Product> products = get();
                    productTableModel.setRowCount(0);
                    productCombo.removeAllItems();
                    productNameLookup.clear();

                    for (Product product : products) {
                        productTableModel.addRow(new Object[] {
                            product.getId().getValue(),
                            product.getName(),
                            product.getCategory(),
                            product.getUnitPrice(),
                            product.getUnit()
                        });
                        productCombo.addItem(product);
                        productNameLookup.put(product.getId().getValue(), product.getName());
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(StockManagementFrame.this, ex.getMessage(), "Refresh Products Failed", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(StockManagementFrame.this, ex.getMessage(), "Refresh Products Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void refreshStockTable(boolean onlyIfChanged) {
        new SwingWorker<List<StockBatch>, Void>() {
            @Override
            protected List<StockBatch> doInBackground() {
                return context.getClientService().getStock();
            }

            @Override
            protected void done() {
                try {
                    populateStockTable(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    if (!onlyIfChanged) {
                        JOptionPane.showMessageDialog(StockManagementFrame.this, ex.getMessage(), "Refresh Stock Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (ExecutionException ex) {
                    if (!onlyIfChanged) {
                        JOptionPane.showMessageDialog(StockManagementFrame.this, ex.getMessage(), "Refresh Stock Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }.execute();
    }

    public void populateStockTable(List<StockBatch> batches) {
        DefaultTableModel model = (DefaultTableModel) stockTable.getModel();
        model.setRowCount(0);
        for (StockBatch batch : batches) {
            model.addRow(new Object[] {
                batch.getBatchNumber().getValue(),
                productNameLookup.getOrDefault(batch.getProductId().getValue(), batch.getProductId().getValue()),
                batch.getInventoryChannel().name(),
                batch.getQuantity(),
                batch.getExpiryDate().toString(),
                batch.getReceivedDate().toString()
            });
        }
        stockTable.revalidate();
        stockTable.repaint();
    }

    private void addStock() {
        try {
            Product product = (Product) productCombo.getSelectedItem();
            if (product == null) {
                throw new IllegalArgumentException("Select a product");
            }

            String batchNumber = batchNumberField.getText();
            String productId = product.getId().getValue();
            String channel = ((InventoryChannel) channelCombo.getSelectedItem()).name();
            int quantity = (Integer) quantitySpinner.getValue();
            String expiryDate = LocalDate.parse(expiryDateField.getText().trim()).toString();
            String receivedDate = LocalDate.parse(receivedDateField.getText().trim()).toString();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    context.getClientService().addStock(
                        batchNumber,
                        productId,
                        channel,
                        quantity,
                        expiryDate,
                        receivedDate
                    );
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        refreshAll();
                        clearStockForm();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        JOptionPane.showMessageDialog(StockManagementFrame.this, ex.getMessage(), "Add Stock Failed", JOptionPane.ERROR_MESSAGE);
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(StockManagementFrame.this, ex.getMessage(), "Add Stock Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Add Stock Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProduct() {
        try {
            String id = productIdField.getText();
            String name = productNameField.getText();
            String category = categoryField.getText();
            double price = Double.parseDouble(priceField.getText().trim());
            String unit = unitField.getText();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    context.getClientService().addProduct(id, name, category, price, unit);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        refreshAll();
                        clearProductForm();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        JOptionPane.showMessageDialog(StockManagementFrame.this, ex.getMessage(), "Add Product Failed", JOptionPane.ERROR_MESSAGE);
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(StockManagementFrame.this, ex.getMessage(), "Add Product Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Add Product Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopStockPolling() {
        // No polling timer active.
    }

    private void clearStockForm() {
        batchNumberField.setText("");
        quantitySpinner.setValue(1);
        expiryDateField.setText("");
        receivedDateField.setText("");
    }

    private void clearProductForm() {
        productIdField.setText("");
        productNameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        unitField.setText("");
    }
}