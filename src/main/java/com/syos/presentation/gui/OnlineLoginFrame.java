package com.syos.presentation.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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

public class OnlineLoginFrame extends JFrame {
    private final GuiAppContext context;
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public OnlineLoginFrame(GuiAppContext context) {
        this.context = context;
        setTitle("Online Login");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 320);
        setLocationRelativeTo(null);
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Online Sales Management", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        JButton registerLink = new JButton("Register");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setForeground(Color.BLUE);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> {
            dispose();
            new OnlineRegisterFrame(context).setVisible(true);
        });
        actions.add(loginButton);
        actions.add(registerLink);
        add(actions, BorderLayout.SOUTH);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                return context.getClientService().login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    dispose();
                    new OnlineSalesFrame(context, user).setVisible(true);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(OnlineLoginFrame.this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(OnlineLoginFrame.this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}