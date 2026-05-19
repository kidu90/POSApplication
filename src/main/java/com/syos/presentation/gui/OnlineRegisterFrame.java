package com.syos.presentation.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.syos.domain.entity.User;

public class OnlineRegisterFrame extends JFrame {
    private final GuiAppContext context;
    private final JTextField fullNameField = new JTextField(18);
    private final JTextField addressField = new JTextField(18);
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public OnlineRegisterFrame(GuiAppContext context) {
        this.context = context;
        setTitle("Online Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 360);
        setLocationRelativeTo(null);
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Register New Online Customer", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.add(new JLabel("Name:"));
        form.add(fullNameField);
        form.add(new JLabel("Address:"));
        form.add(addressField);
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> register());
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            dispose();
            new OnlineLoginFrame(context).setVisible(true);
        });
        actions.add(registerButton);
        actions.add(backButton);
        add(actions, BorderLayout.SOUTH);
    }

    private void register() {
        String fullName = fullNameField.getText();
        String address = addressField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                return context.getClientService().register(fullName, address, username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    dispose();
                    new OnlineSalesFrame(context, user).setVisible(true);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(OnlineRegisterFrame.this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(OnlineRegisterFrame.this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}