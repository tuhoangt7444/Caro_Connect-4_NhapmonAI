package view; 

import javax.swing.*;
import java.awt.*;

// Import các lớp cần thiết
import model.Connect4Model;
import controller.Connect4Controller;
// KHÔNG CẦN import Connect4View; vì đã cùng package 'view'

/**
 * Lớp View hiển thị menu.
 */
public class MenuPanel extends JPanel {
    
    private final Connect4Controller controller;

    public MenuPanel(Connect4Controller controller) {
        this.controller = controller;
        setLayout(new GridBagLayout()); 
        setBackground(Connect4View.COLOR_BLACK); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 
        gbc.gridy = 0; 

        JLabel title = new JLabel("CONNECT 4 AI");
        title.setFont(new Font("Arial", Font.BOLD, 50));
        title.setForeground(Connect4View.COLOR_WHITE); 
        gbc.gridy++;
        add(title, gbc);

        gbc.gridy++;
        add(Box.createVerticalStrut(30), gbc);

        // Nút 1:
        JButton pvpButton = createMenuButton("Người vs Người");
        pvpButton.addActionListener(e -> controller.startGame(Connect4Model.GameMode.PVP)); 
        gbc.gridy++;
        add(pvpButton, gbc);

        // Nút 2:
        JButton pveEasyButton = createMenuButton("Người vs Máy (Dễ)");
        pveEasyButton.addActionListener(e -> controller.startGame(Connect4Model.GameMode.PVE_EASY)); 
        gbc.gridy++;
        add(pveEasyButton, gbc);

        // Nút 3:
        JButton pveHardButton = createMenuButton("Người vs Máy (Khó)");
        pveHardButton.addActionListener(e -> controller.startGame(Connect4Model.GameMode.PVE_HARD)); 
        gbc.gridy++;
        add(pveHardButton, gbc);

        // Nút 4:
        JButton cvcButton = createMenuButton("Máy (Dễ) vs Máy (Khó)");
        cvcButton.addActionListener(e -> controller.startGame(Connect4Model.GameMode.CVC)); 
        gbc.gridy++;
        add(cvcButton, gbc);

        gbc.gridy++;
        add(Box.createVerticalStrut(30), gbc);

        // Nút 5:
        JButton quitButton = createMenuButton("Thoát");
        quitButton.addActionListener(e -> System.exit(0));
        gbc.gridy++;
        add(quitButton, gbc);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 20));
        button.setBackground(Connect4View.COLOR_BLUE); 
        button.setForeground(Connect4View.COLOR_WHITE); 
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(300, 50));
        return button;
    }
}