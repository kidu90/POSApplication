package com.syos.presentation.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.syos.application.strategy.DiscountStrategy;
import com.syos.application.strategy.StockSelectionStrategy;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.Product;

public abstract class AbstractSalesFrame extends JFrame {
    private final GuiAppContext context;
    private final String headerText;
    private final String customerName;
    private final String customerAddress;
    private final Map<String, Product> productLookup = new LinkedHashMap<>();
    private final Map<String, Integer> cart = new HashMap<>();
    private final DefaultTableModel productTableModel = new DefaultTableModel(new Object[] {"ID", "Name", "Price"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel cartTableModel = new DefaultTableModel(new Object[] {"ID", "Name", "Qty", "Unit Price", "Line Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTextArea billArea = new JTextArea();
    private final JTextField productIdField = new JTextField(12);
    private final JSpinner quantitySpinner = new JSpinner(new javax.swing.SpinnerNumberModel(1, 1, 9999, 1));
    private final JLabel headerLabel = new JLabel();

    protected AbstractSalesFrame(GuiAppContext context,
                                 String title,
                                 String headerText,
                                 String customerName,
                                 String customerAddress) {
        this.context = context;
        this.headerText = headerText;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        buildUi();
        loadCatalog();
    }

    protected abstract Bill.SaleType getSaleType();

    protected abstract DiscountStrategy getDiscountStrategy();

    protected abstract StockSelectionStrategy getStockSelectionStrategy();

    protected GuiAppContext getContext() {
        return context;
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        headerLabel.setText(headerText);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(headerLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.52);
        splitPane.setLeftComponent(buildCatalogPanel());
        splitPane.setRightComponent(buildCartAndBillPanel());
        add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> {
            dispose();
            new MainMenuFrame(context).setVisible(true);
        });
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel buildCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(540, 650));

        JTable productTable = new JTable(productTableModel);
        productTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel addPanel = new JPanel();
        addPanel.add(new JLabel("Product ID:"));
        addPanel.add(productIdField);
        addPanel.add(new JLabel("Qty:"));
        addPanel.add(quantitySpinner);

        JButton addButton = new JButton("Add to Cart");
        addButton.addActionListener(e -> addToCart());

        JButton refreshButton = new JButton("Refresh Catalog");
        refreshButton.addActionListener(e -> loadCatalog());

        addPanel.add(addButton);
        addPanel.add(refreshButton);
        panel.add(addPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCartAndBillPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(620, 650));

        JTable cartTable = new JTable(cartTableModel);
        cartTable.setFillsViewportHeight(true);

        JPanel cartButtonPanel = new JPanel();
        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelectedCartItem(cartTable.getSelectedRow()));
        JButton clearButton = new JButton("Clear Cart");
        clearButton.addActionListener(e -> clearCart(true));
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.addActionListener(e -> checkout());
        cartButtonPanel.add(removeButton);
        cartButtonPanel.add(clearButton);
        cartButtonPanel.add(checkoutButton);

        JPanel cartPanel = new JPanel(new BorderLayout(8, 8));
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        cartPanel.add(cartButtonPanel, BorderLayout.SOUTH);

        billArea.setEditable(false);
        billArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel billPanel = new JPanel(new BorderLayout(8, 8));
        billPanel.add(new JScrollPane(billArea), BorderLayout.CENTER);

        JPanel customerPanel = buildCustomerPanel();

        JPanel rightTop = new JPanel(new BorderLayout(8, 8));
        rightTop.add(customerPanel, BorderLayout.NORTH);
        rightTop.add(cartPanel, BorderLayout.CENTER);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightTop, billPanel);
        verticalSplit.setResizeWeight(0.5);
        panel.add(verticalSplit, BorderLayout.CENTER);
        return panel;
    }

    protected JPanel buildCustomerPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Customer: " + safeInfo(customerName)));
        panel.add(new JLabel("Address: " + safeInfo(customerAddress)));
        return panel;
    }

    private String safeInfo(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private void loadCatalog() {
        new SwingWorker<List<Product>, Void>() {
            @Override
            protected List<Product> doInBackground() {
                return context.getClientService().getProducts();
            }

            @Override
            protected void done() {
                try {
                    List<Product> products = get();
                    productLookup.clear();
                    productTableModel.setRowCount(0);
                    for (Product product : products) {
                        productLookup.put(product.getId().getValue(), product);
                        productTableModel.addRow(new Object[] {
                            product.getId().getValue(),
                            product.getName(),
                            product.getUnitPrice()
                        });
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex.getMessage());
                } catch (ExecutionException ex) {
                    showError(ex.getMessage());
                }
            }
        }.execute();
    }

    private void addToCart() {
        String productIdValue = productIdField.getText().trim().toUpperCase();
        int quantity = (Integer) quantitySpinner.getValue();

        if (productIdValue.isEmpty()) {
            showError("Enter a product ID");
            return;
        }

        Product product = productLookup.get(productIdValue);
        if (product == null) {
            showError("Product not found: " + productIdValue);
            return;
        }

        cart.merge(productIdValue, quantity, Integer::sum);
        refreshCartTable();
        productIdField.setText("");
        quantitySpinner.setValue(1);
    }

    private void removeSelectedCartItem(int selectedRow) {
        if (selectedRow < 0) {
            showError("Select a cart row to remove");
            return;
        }

        String productId = (String) cartTableModel.getValueAt(selectedRow, 0);
        cart.remove(productId);
        refreshCartTable();
    }

    private void clearCart(boolean clearBillArea) {
        cart.clear();
        refreshCartTable();
        if (clearBillArea) {
            billArea.setText("");
        }
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product product = productLookup.get(entry.getKey());
            if (product == null) {
                continue;
            }

            int quantity = entry.getValue();
            cartTableModel.addRow(new Object[] {
                product.getId().getValue(),
                product.getName(),
                quantity,
                product.getUnitPrice(),
                product.getUnitPrice().multiply(quantity)
            });
        }
    }

    private void checkout() {
        if (cart.isEmpty()) {
            showError("Cart cannot be empty");
            return;
        }

        Map<String, Integer> cartSnapshot = new LinkedHashMap<>(cart);
        new SwingWorker<Bill, Void>() {
            @Override
            protected Bill doInBackground() {
                if (getSaleType() == Bill.SaleType.ONLINE) {
                    return context.getClientService().checkoutOnline(cartSnapshot, customerName, customerAddress);
                }
                return context.getClientService().checkoutInStore(cartSnapshot);
            }

            @Override
            protected void done() {
                try {
                    Bill bill = get();
                    clearCart(false);
                    billArea.setText(BillTextFormatter.format(bill));
                    loadCatalog();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex.getMessage());
                } catch (ExecutionException ex) {
                    showError(ex.getMessage());
                }
            }
        }.execute();
    }

    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}