package view; 

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Map;

// Import các lớp cần thiết
import model.Connect4Model;
import controller.Connect4Controller;

/**
 * Lớp View hiển thị bàn cờ.
 */
public class GamePanel extends JPanel {

    private final Connect4Model model;
    private final Connect4Controller controller;
    private int mouseX = 0;
    private JButton menuButton;
    //falling animation
    private float fallingProgress = 0;
    private int fallingColumn = -1;
    private int fallingRow = -1;
    private Timer fallingTimer;
    
    // Blink effect for win message
    private Timer blinkTimer;
    private boolean isBlinkVisible = true;
    private int blinkCount = 0;
    private static final int BLINK_DURATION = 3000; // 3 seconds
    
    public static final int RADIUS = (int) (Connect4View.SQUARE_SIZE / 2 - 5);
    
    // Portal colors
    private static final Color PORTAL_BLUE = new Color(29, 99, 137);
    private static final Color PORTAL_ORANGE = new Color(255, 140, 0);
    private static final Color PORTAL_WHITE = new Color(240, 240, 240);
    private static final Color PORTAL_DARK = new Color(20, 20, 30);
    private static final Color BOARD_BG = new Color(30, 40, 60);
    private static final Color PLAYER_BLUE = new Color(100, 180, 255);
    private static final Color AI_ORANGE = new Color(255, 160, 50);

    public GamePanel(Connect4Model model, Connect4Controller controller) {
        this.model = model;
        this.controller = controller;

        setLayout(null);
        setOpaque(false);

        setPreferredSize(new Dimension(Connect4View.SCREEN_WIDTH, Connect4View.SCREEN_HEIGHT));
        
        menuButton = new JButton("⚙");
        menuButton.setBounds(10, 10, 60, 50);
        menuButton.setFont(new Font("Arial", Font.BOLD, 24));
        menuButton.setBackground(PORTAL_BLUE);
        menuButton.setForeground(PORTAL_WHITE);
        menuButton.setFocusable(false);
        menuButton.setBorderPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        menuButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                menuButton.setBackground(PORTAL_ORANGE);
                menuButton.setForeground(PORTAL_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                menuButton.setBackground(PORTAL_BLUE);
                menuButton.setForeground(PORTAL_WHITE);
            }
        });
        
        add(menuButton);

        menuButton.addActionListener(e -> {
            showGameMenu();
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                controller.handlePlayerMove(e.getX() / Connect4View.SQUARE_SIZE); 
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                repaint(); 
            }
        });

        // Tạo Timer để cập nhật animation
        fallingTimer = new Timer(30, e -> {
            if (fallingProgress < 1.0f) {
                fallingProgress += 0.05f; // Tăng 5% mỗi frame
                repaint();
            } else {
                fallingProgress = 0;
                fallingColumn = -1;
                fallingTimer.stop();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Vẽ gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, PORTAL_DARK,
            width, height, BOARD_BG
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Vẽ các hình tròn trang trí nhỏ
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(new Color(29, 99, 137, 40));
        g2d.drawOval(width - 200, -50, 200, 200);
        g2d.setColor(new Color(255, 140, 0, 40));
        g2d.drawOval(-50, height - 150, 150, 150);
        
        super.paintComponent(g);

        int[][] board = model.getBoard();

        // 1. Vẽ bàn cờ với glow effect
        for (int c = 0; c < Connect4Model.COLUMN_COUNT; c++) {
            for (int r = 0; r < Connect4Model.ROW_COUNT; r++) {
                // Vẽ nền ô
                g2d.setColor(BOARD_BG);
                g2d.fillRect(c * Connect4View.SQUARE_SIZE, (r + 1) * Connect4View.SQUARE_SIZE, 
                            Connect4View.SQUARE_SIZE, Connect4View.SQUARE_SIZE);
                
                // Vẽ viền ô
                g2d.setColor(PORTAL_BLUE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(c * Connect4View.SQUARE_SIZE, (r + 1) * Connect4View.SQUARE_SIZE, 
                            Connect4View.SQUARE_SIZE, Connect4View.SQUARE_SIZE);
                
                // Vẽ lỗ trống
                g2d.setColor(new Color(10, 10, 20));
                g2d.fillOval(c * Connect4View.SQUARE_SIZE + (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2,
                             (r + 1) * Connect4View.SQUARE_SIZE + (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2,
                             RADIUS * 2, RADIUS * 2);
            }
        }

        // 2. Vẽ quân cờ
        for (int c = 0; c < Connect4Model.COLUMN_COUNT; c++) {
            for (int r = 0; r < Connect4Model.ROW_COUNT; r++) {
                int piece = board[r][c];
                if (piece != Connect4Model.EMPTY) {
                    g2d.setColor((piece == Connect4Model.PLAYER_PIECE) ? PLAYER_BLUE : AI_ORANGE);

                    int y;
                    if (c == fallingColumn && r == fallingRow && fallingProgress < 1.0f) {
                        int startY = (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2;
                        int targetY = (Connect4Model.ROW_COUNT - r) * Connect4View.SQUARE_SIZE + (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2;
                        y = (int) (startY + (targetY - startY) * fallingProgress);
                    } else {
                        y = (Connect4Model.ROW_COUNT - r) * Connect4View.SQUARE_SIZE + (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2;
                    }

                    int x = c * Connect4View.SQUARE_SIZE + (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2;
                    
                    // Vẽ glow effect
                    g2d.setColor(new Color(piece == Connect4Model.PLAYER_PIECE ? 100 : 255, 
                                          piece == Connect4Model.PLAYER_PIECE ? 180 : 160, 
                                          piece == Connect4Model.PLAYER_PIECE ? 255 : 50, 100));
                    g2d.fillOval(x - 5, y - 5, RADIUS * 2 + 10, RADIUS * 2 + 10);
                    
                    // Vẽ quân cờ
                    g2d.setColor((piece == Connect4Model.PLAYER_PIECE) ? PLAYER_BLUE : AI_ORANGE);
                    g2d.fillOval(x, y, RADIUS * 2, RADIUS * 2);
                }
            }
        }

        // 3. Vẽ quân cờ mờ (preview)
        if (!model.isGameOver() && !controller.isAIThinking()) {
            Color hoverColor = null;
            if (model.getMode() == Connect4Model.GameMode.PVP) {
                hoverColor = (model.getTurn() == 0) ? PLAYER_BLUE : AI_ORANGE;
            } else if (model.getTurn() == 0) { 
                hoverColor = PLAYER_BLUE;
            }
            if (hoverColor != null) {
                g2d.setColor(new Color(hoverColor.getRed(), hoverColor.getGreen(), hoverColor.getBlue(), 100));
                g2d.fillOval(mouseX - RADIUS, (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2, RADIUS * 2, RADIUS * 2);
            }
        }

        // 4. Vẽ điểm số
        Map<Integer, Integer> moveScores = controller.getLastMoveScores();
        if (moveScores != null && !moveScores.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(PORTAL_WHITE);
            FontMetrics metrics = g2d.getFontMetrics();
            for (Map.Entry<Integer, Integer> entry : moveScores.entrySet()) {
                int col = entry.getKey();
                int score = entry.getValue();
                String scoreStr;
                if (score > 9999999) scoreStr = "WIN";
                else if (score < -9999999) scoreStr = "LOSE";
                else scoreStr = Integer.toString(score);
                int x = (col * Connect4View.SQUARE_SIZE) + (Connect4View.SQUARE_SIZE - metrics.stringWidth(scoreStr)) / 2;
                int y = (Connect4View.SQUARE_SIZE - metrics.getHeight()) / 2 + metrics.getAscent();
                g2d.drawString(scoreStr, x, y);
            }
        }

        // 5. Vẽ thông báo kết thúc game
        if (model.isGameOver()) {
            // Highlight 4 cờ nối nhau khi blink
            if (isBlinkVisible && model.getWinningLine() != null) {
                int[][] winningLine = model.getWinningLine();
                g2d.setColor(new Color(255, 200, 0, 150));
                g2d.setStroke(new BasicStroke(6));
                for (int[] pos : winningLine) {
                    int r = pos[0];
                    int c = pos[1];
                    int cx = c * Connect4View.SQUARE_SIZE + (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2 + RADIUS;
                    int cy = (Connect4Model.ROW_COUNT - r) * Connect4View.SQUARE_SIZE + (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2 + RADIUS;
                    g2d.drawOval(cx - RADIUS - 3, cy - RADIUS - 3, (RADIUS + 3) * 2, (RADIUS + 3) * 2);
                }
            }
            
            // Vẽ thông báo ở trên (không có overlay tối)
            if (isBlinkVisible) {
                g2d.setColor(PORTAL_ORANGE);
                g2d.setFont(new Font("Arial", Font.BOLD, 60));
                FontMetrics metrics = g2d.getFontMetrics();
                String message = model.getGameOverMessage();
                int x = (width - metrics.stringWidth(message)) / 2;
                int y = 80;  // Đặt ở phía trên thay vì giữa
                g2d.drawString(message, x, y);
                
                // Vẽ glow effect
                g2d.setColor(new Color(255, 140, 0, 100));
                for (int i = 1; i <= 5; i++) {
                    g2d.setStroke(new BasicStroke(i));
                    g2d.drawString(message, x, y);
                }
            }
        }
    }

    // Hàm để bắt đầu animation rơi
    public void startFallingAnimation(int col) {
        this.fallingColumn = col;
        
        // Lấy hàng của quân mới từ Model (lưu trong performMove)
        this.fallingRow = model.getLastMoveRow();
        
        this.fallingProgress = 0;
        if (fallingTimer != null && fallingTimer.isRunning()) {
            fallingTimer.stop();
        }
        fallingTimer.start();
    }
    
    // Bắt đầu blink effect khi game over
    public void startBlinkEffect() {
        if (blinkTimer != null && blinkTimer.isRunning()) {
            blinkTimer.stop();
        }
        
        blinkCount = 0;
        isBlinkVisible = true;
        blinkTimer = new Timer(200, e -> {
            isBlinkVisible = !isBlinkVisible;
            blinkCount++;
            repaint();
            
            // Dừng sau 3 giây (15 lần blink)
            if (blinkCount > 15) {
                blinkTimer.stop();
                isBlinkVisible = true;
                showGameOverDialog();
            }
        });
        blinkTimer.start();
    }
    
    // Dialog cho người dùng chọn
    private void showGameOverDialog() {
        String[] options = {"Chơi lại", "Quay về Menu"};
        int choice = JOptionPane.showOptionDialog(
            this,
            model.getGameOverMessage(),
            "Kết thúc game",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice == 0) {
            // Chơi lại với tên cũ (không hỏi đặt tên)
            controller.restartGameWithSameName(model.getMode());
        } else {
            // Quay về menu
            controller.handleExitToMenu();
        }
    }
    
    // Menu game đẹp
    private void showGameMenu() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Menu Game");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, PORTAL_DARK,
                    getWidth(), getHeight(), BOARD_BG
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(350, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel title = new JLabel("MENU GAME");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(PORTAL_WHITE);
        gbc.gridy++;
        panel.add(title, gbc);
        
        // Khoảng cách
        gbc.gridy++;
        panel.add(Box.createVerticalStrut(10), gbc);
        
        // Nút 1: Tiếp tục
        JButton continueBtn = createDialogButton("▶ Tiếp tục");
        continueBtn.addActionListener(e -> dialog.dispose());
        gbc.gridy++;
        panel.add(continueBtn, gbc);
        
        // Nút 2: Undo
        JButton undoBtn = createDialogButton("↶ Quay lại 1 bước");
        undoBtn.addActionListener(e -> {
            dialog.dispose();
            controller.handleUndo();
        });
        gbc.gridy++;
        panel.add(undoBtn, gbc);
        
        // Nút 3: Exit
        JButton exitBtn = createDialogButton("⛔ Thoát (Về Menu)");
        exitBtn.addActionListener(e -> {
            dialog.dispose();
            controller.handleExitToMenu();
        });
        gbc.gridy++;
        panel.add(exitBtn, gbc);
        
        dialog.add(panel);
        dialog.pack();
        dialog.setVisible(true);
    }
    
    // Tạo nút đẹp cho dialog menu
    private JButton createDialogButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(PORTAL_BLUE);
        button.setForeground(PORTAL_WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(300, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(PORTAL_ORANGE);
                button.setForeground(PORTAL_DARK);
                button.setFont(new Font("Arial", Font.BOLD, 20));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(PORTAL_BLUE);
                button.setForeground(PORTAL_WHITE);
                button.setFont(new Font("Arial", Font.BOLD, 18));
            }
        });
        
        return button;
    }
}