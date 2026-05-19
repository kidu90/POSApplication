package com.syos.presentation.gui;

import java.awt.BorderLayout;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class ReportsFrame extends JFrame {
    private final GuiAppContext context;
    private final JTextArea dailySalesArea = new JTextArea();
    private final JTextArea stockStatusArea = new JTextArea();
    private final JTextArea reshelveArea = new JTextArea();
    private final JTextArea billReportArea = new JTextArea();

    public ReportsFrame(GuiAppContext context) {
        this.context = context;
        setTitle("Reports");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 760);
        setLocationRelativeTo(null);
        buildUi();
        refreshReports();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Daily Sales Report", new JScrollPane(dailySalesArea));
        tabs.addTab("Stock Status Report", new JScrollPane(stockStatusArea));
        tabs.addTab("Reshelve Report", new JScrollPane(reshelveArea));
        tabs.addTab("Bill Report", new JScrollPane(billReportArea));
        add(tabs, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshReports());
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> {
            dispose();
            new MainMenuFrame(context).setVisible(true);
        });
        footer.add(refreshButton);
        footer.add(backButton);
        add(footer, BorderLayout.SOUTH);
    }

    private void refreshReports() {
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() {
                return new String[] {
                    context.getClientService().getDailyReport(),
                    context.getClientService().getStockReport(),
                    context.getClientService().getReshelveReport(),
                    context.getClientService().getBillReport()
                };
            }

            @Override
            protected void done() {
                try {
                    String[] reports = get();
                    dailySalesArea.setText(reports[0]);
                    stockStatusArea.setText(reports[1]);
                    reshelveArea.setText(reports[2]);
                    billReportArea.setText(reports[3]);
                    dailySalesArea.setCaretPosition(0);
                    stockStatusArea.setCaretPosition(0);
                    reshelveArea.setCaretPosition(0);
                    billReportArea.setCaretPosition(0);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    dailySalesArea.setText(ex.getMessage());
                    stockStatusArea.setText(ex.getMessage());
                } catch (ExecutionException ex) {
                    dailySalesArea.setText(ex.getMessage());
                    stockStatusArea.setText(ex.getMessage());
                }
            }
        }.execute();
    }
}