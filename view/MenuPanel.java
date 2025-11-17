package view; 

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

// Import các lớp cần thiết
import model.Connect4Model;
import controller.Connect4Controller;
// KHÔNG CẦN import Connect4View; vì đã cùng package 'view'

/**
 * Lớp View hiển thị menu.
 */
public class MenuPanel extends JPanel {
    
    private final Connect4Controller controller;
    
    // Portal color scheme
    private static final Color PORTAL_BLUE = new Color(29, 99, 137);     // Xanh Portal
    private static final Color PORTAL_ORANGE = new Color(255, 140, 0);   // Cam Portal
    private static final Color PORTAL_WHITE = new Color(240, 240, 240);  // Trắng Portal
    private static final Color PORTAL_DARK = new Color(20, 20, 30);      // Xám đen

    public MenuPanel(Connect4Controller controller) {
        this.controller = controller;
        setLayout(new GridBagLayout()); 
        setOpaque(false);  // Đặt thành false để paintComponent được gọi
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 
        gbc.gridy = 0; 

        JLabel title = new JLabel("CARO CONNECT 4");
        title.setFont(new Font("Arial", Font.BOLD, 60));
        title.setForeground(PORTAL_WHITE); 
        gbc.gridy++;
        add(title, gbc);

        gbc.gridy++;
        add(Box.createVerticalStrut(30), gbc);

        // Nút 1:
        JButton pvpButton = createMenuButton("Người vs Người");
        pvpButton.addActionListener(e -> {
            SoundManager.playClickSound();
            controller.startGame(Connect4Model.GameMode.PVP);
        }); 
        gbc.gridy++;
        add(pvpButton, gbc);

        // Nút 2:
        JButton pveEasyButton = createMenuButton("Người vs Máy (Dễ)");
        pveEasyButton.addActionListener(e -> {
            SoundManager.playClickSound();
            controller.startGame(Connect4Model.GameMode.PVE_EASY);
        }); 
        gbc.gridy++;
        add(pveEasyButton, gbc);

        // Nút 3:
        JButton pveHardButton = createMenuButton("Người vs Máy (Khó)");
        pveHardButton.addActionListener(e -> {
            SoundManager.playClickSound();
            controller.startGame(Connect4Model.GameMode.PVE_HARD);
        }); 
        gbc.gridy++;
        add(pveHardButton, gbc);

        // Nút 4:
        JButton cvcButton = createMenuButton("Máy (Dễ) vs Máy (Khó)");
        cvcButton.addActionListener(e -> {
            SoundManager.playClickSound();
            controller.startGame(Connect4Model.GameMode.CVC);
        }); 
        gbc.gridy++;
        add(cvcButton, gbc);

        gbc.gridy++;
        add(Box.createVerticalStrut(30), gbc);

        // Nút 5:
        JButton quitButton = createMenuButton("Thoát");
        quitButton.addActionListener(e -> {
            SoundManager.playClickSound();
            System.exit(0);
        });
        gbc.gridy++;
        add(quitButton, gbc);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 22));
        button.setBackground(PORTAL_BLUE); 
        button.setForeground(PORTAL_WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(350, 60));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hiệu ứng hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(PORTAL_ORANGE);
                button.setFont(new Font("Arial", Font.BOLD, 24));
                button.setForeground(PORTAL_DARK);
                SoundManager.playHoverSound();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(PORTAL_BLUE);
                button.setFont(new Font("Arial", Font.BOLD, 22));
                button.setForeground(PORTAL_WHITE);
            }
        });
        
        return button;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Vẽ gradient background: từ xanh Portal sang xám đen
        GradientPaint gradient = new GradientPaint(
            0, 0, PORTAL_DARK,
            width, height, PORTAL_BLUE
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Vẽ các hình tròn trang trí (Portal aesthetic)
        g2d.setStroke(new BasicStroke(2));
        
        // Hình tròn xanh góc trên trái
        g2d.setColor(new Color(29, 99, 137, 80));
        g2d.drawOval(-100, -100, 250, 250);
        
        // Hình tròn cam góc dưới phải
        g2d.setColor(new Color(255, 140, 0, 80));
        g2d.drawOval(width - 150, height - 150, 250, 250);
        
        // Hình tròn nhỏ ở giữa
        g2d.setColor(new Color(100, 200, 255, 50));
        g2d.drawOval(width / 2 - 100, height / 2 - 100, 200, 200);
        
        // Gọi super để vẽ components
        super.paintComponent(g);
    }
}