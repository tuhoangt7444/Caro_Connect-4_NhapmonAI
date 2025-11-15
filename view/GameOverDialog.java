package view;

import javax.swing.*;
import java.awt.*;

public class GameOverDialog extends JDialog {
    private JButton playAgainButton;
    private JButton menuButton;
    private int choice = -1; // -1: không xác định, 0: chơi lại, 1: về menu
    
    // Portal colors
    private static final Color PORTAL_BLUE = new Color(29, 99, 137);
    private static final Color PORTAL_ORANGE = new Color(255, 140, 0);
    private static final Color PORTAL_WHITE = new Color(240, 240, 240);
    private static final Color PORTAL_DARK = new Color(20, 20, 30);
    private static final Color BOARD_BG = new Color(30, 40, 60);
    
    public GameOverDialog(JFrame parent, String message) {
        super(parent, "Kết thúc game", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(450, 250);
        setLocationRelativeTo(parent);
        
        // Main panel with Portal colors
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(BOARD_BG);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Message panel
        JLabel messageLabel = new JLabel("<html><div style='text-align: center; font-size: 18px;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setForeground(PORTAL_ORANGE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        mainPanel.add(messageLabel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        playAgainButton = new JButton("Chơi lại");
        playAgainButton.setFont(new Font("Arial", Font.BOLD, 14));
        playAgainButton.setBackground(PORTAL_BLUE);
        playAgainButton.setForeground(PORTAL_WHITE);
        playAgainButton.setFocusPainted(false);
        playAgainButton.setBorderPainted(false);
        playAgainButton.setPreferredSize(new Dimension(150, 45));
        playAgainButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playAgainButton.addActionListener(e -> {
            SoundManager.playClickSound();
            choice = 0;
            dispose();
        });
        
        menuButton = new JButton("Về Menu");
        menuButton.setFont(new Font("Arial", Font.BOLD, 14));
        menuButton.setBackground(PORTAL_ORANGE);
        menuButton.setForeground(PORTAL_DARK);
        menuButton.setFocusPainted(false);
        menuButton.setBorderPainted(false);
        menuButton.setPreferredSize(new Dimension(150, 45));
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButton.addActionListener(e -> {
            SoundManager.playClickSound();
            choice = 1;
            dispose();
        });
        
        // Hover effects
        playAgainButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                playAgainButton.setBackground(PORTAL_ORANGE);
                playAgainButton.setForeground(PORTAL_DARK);
                SoundManager.playHoverSound();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                playAgainButton.setBackground(PORTAL_BLUE);
                playAgainButton.setForeground(PORTAL_WHITE);
            }
        });
        
        menuButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                menuButton.setBackground(PORTAL_BLUE);
                menuButton.setForeground(PORTAL_WHITE);
                SoundManager.playHoverSound();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                menuButton.setBackground(PORTAL_ORANGE);
                menuButton.setForeground(PORTAL_DARK);
            }
        });
        
        buttonPanel.add(playAgainButton);
        buttonPanel.add(menuButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    public int getChoice() {
        return choice;
    }
}
