package view;

import javax.swing.*;
import java.awt.*;

public class NameInputDialog extends JDialog {
    private JTextField p1NameField;
    private JTextField p2NameField;
    private JButton okButton;
    private JButton cancelButton;
    private boolean confirmed = false;
    
    // Portal colors
    private static final Color PORTAL_BLUE = new Color(29, 99, 137);
    private static final Color PORTAL_ORANGE = new Color(255, 140, 0);
    private static final Color PORTAL_WHITE = new Color(240, 240, 240);
    private static final Color PORTAL_DARK = new Color(20, 20, 30);
    private static final Color BOARD_BG = new Color(30, 40, 60);
    
    public NameInputDialog(JFrame parent, String title, String label1, String label2, String defaultName1, String defaultName2) {
        super(parent, title, true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(400, 250);
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
        
        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new GridLayout(2, 2, 10, 15));
        
        // Player 1 label
        JLabel p1Label = new JLabel(label1 + ":");
        p1Label.setFont(new Font("Arial", Font.BOLD, 14));
        p1Label.setForeground(PORTAL_WHITE);
        
        // Player 1 input
        p1NameField = new JTextField(defaultName1);
        p1NameField.setFont(new Font("Arial", Font.PLAIN, 14));
        p1NameField.setBackground(PORTAL_DARK);
        p1NameField.setForeground(PORTAL_WHITE);
        p1NameField.setCaretColor(PORTAL_ORANGE);
        p1NameField.setBorder(BorderFactory.createLineBorder(PORTAL_BLUE, 2));
        
        inputPanel.add(p1Label);
        inputPanel.add(p1NameField);
        
        // Player 2 label (only if label2 is not null)
        if (label2 != null && !label2.isEmpty()) {
            JLabel p2Label = new JLabel(label2 + ":");
            p2Label.setFont(new Font("Arial", Font.BOLD, 14));
            p2Label.setForeground(PORTAL_WHITE);
            
            // Player 2 input
            p2NameField = new JTextField(defaultName2);
            p2NameField.setFont(new Font("Arial", Font.PLAIN, 14));
            p2NameField.setBackground(PORTAL_DARK);
            p2NameField.setForeground(PORTAL_WHITE);
            p2NameField.setCaretColor(PORTAL_ORANGE);
            p2NameField.setBorder(BorderFactory.createLineBorder(PORTAL_BLUE, 2));
            
            inputPanel.add(p2Label);
            inputPanel.add(p2NameField);
        } else {
            p2NameField = null;
        }
        
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        okButton = new JButton("OK");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setBackground(PORTAL_BLUE);
        okButton.setForeground(PORTAL_WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setPreferredSize(new Dimension(100, 40));
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> {
            SoundManager.playClickSound();
            confirm();
        });
        
        cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(PORTAL_ORANGE);
        cancelButton.setForeground(PORTAL_DARK);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> {
            SoundManager.playClickSound();
            cancel();
        });
        
        // Hover effects
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                okButton.setBackground(PORTAL_ORANGE);
                okButton.setForeground(PORTAL_DARK);
                SoundManager.playHoverSound();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                okButton.setBackground(PORTAL_BLUE);
                okButton.setForeground(PORTAL_WHITE);
            }
        });
        
        // Hover effects
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(PORTAL_BLUE);
                cancelButton.setForeground(PORTAL_WHITE);
                SoundManager.playHoverSound();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(PORTAL_ORANGE);
                cancelButton.setForeground(PORTAL_DARK);
            }
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private void confirm() {
        confirmed = true;
        dispose();
    }
    
    private void cancel() {
        confirmed = false;
        dispose();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getPlayer1Name() {
        return p1NameField.getText().trim();
    }
    
    public String getPlayer2Name() {
        return p2NameField != null ? p2NameField.getText().trim() : null;
    }
}
