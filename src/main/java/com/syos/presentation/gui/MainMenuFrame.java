package com.syos.presentation.gui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainMenuFrame extends JFrame {
    private final GuiAppContext context;

    public MainMenuFrame(GuiAppContext context) {
        this.context = context;
        setTitle("SYOS Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 420);
        setLocationRelativeTo(null);
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("SYNEX OUTLET STORE", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        JButton inStoreButton = new JButton("In-Store POS");
        inStoreButton.addActionListener(e -> {
            dispose();
            new InStorePOSFrame(context).setVisible(true);
        });

        JButton onlineButton = new JButton("Online Sales Management");
        onlineButton.addActionListener(e -> {
            dispose();
            new OnlineLoginFrame(context).setVisible(true);
        });

        JButton stockButton = new JButton("Stock Management");
        stockButton.addActionListener(e -> {
            dispose();
            StockManagementFrame stockManagementFrame = context.getStockManagementFrame();
            if (stockManagementFrame == null || !stockManagementFrame.isDisplayable()) {
                stockManagementFrame = new StockManagementFrame(context);
                context.setStockManagementFrame(stockManagementFrame);
            }
            stockManagementFrame.refreshAll();
            stockManagementFrame.setVisible(true);
        });

        JButton reportsButton = new JButton("Reports");
        reportsButton.addActionListener(e -> {
            dispose();
            new ReportsFrame(context).setVisible(true);
        });

        buttons.add(inStoreButton);
        buttons.add(onlineButton);
        buttons.add(stockButton);
        buttons.add(reportsButton);
        add(buttons, BorderLayout.CENTER);
    }
}