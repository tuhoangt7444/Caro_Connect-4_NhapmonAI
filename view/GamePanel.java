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
// KHÔNG CẦN import Connect4View; vì đã cùng package 'view'

/**
 * Lớp View hiển thị bàn cờ.
 */
public class GamePanel extends JPanel {

    private final Connect4Model model;
    private final Connect4Controller controller;
    private int mouseX = 0;
    private JButton menuButton;
    
    public static final int RADIUS = (int) (Connect4View.SQUARE_SIZE / 2 - 5);

    public GamePanel(Connect4Model model, Connect4Controller controller) {
        this.model = model;
        this.controller = controller;

        setLayout(null);

        setPreferredSize(new Dimension(Connect4View.SCREEN_WIDTH, Connect4View.SCREEN_HEIGHT));
        setBackground(Connect4View.COLOR_BLACK);
        
        menuButton = new JButton("☰"); // Ký tự "hamburger"
        menuButton.setBounds(10, 10, 60, 40); // Đặt vị trí (x, y, rộng, cao)
        menuButton.setFont(new Font("Arial", Font.BOLD, 20));
        menuButton.setFocusable(false); // Bỏ viền xanh khi click
        add(menuButton); // Thêm nút vào GamePanel

        menuButton.addActionListener(e -> {
            // Tạo một Popup Menu (menu con)
            JPopupMenu popup = new JPopupMenu();

            // 1. Nút "Tiếp tục"
            JMenuItem continueItem = new JMenuItem("Tiếp tục");
            continueItem.addActionListener(ev -> popup.setVisible(false)); // Chỉ cần ẩn menu đi
            
            // 2. Nút "Quay lại 1 bước" (Undo)
            JMenuItem undoItem = new JMenuItem("Quay lại 1 bước");
            undoItem.addActionListener(ev -> controller.handleUndo()); // Gọi Controller
            
            // 3. Nút "Thoát"
            JMenuItem exitItem = new JMenuItem("Thoát (Về Menu)");
            exitItem.addActionListener(ev -> controller.handleExitToMenu()); // Gọi Controller

            // Thêm các nút vào menu
            popup.add(continueItem);
            popup.add(undoItem);
            popup.add(exitItem);
            
            // Hiển thị menu ngay bên dưới nút "☰"
            popup.show(menuButton, 0, menuButton.getHeight());
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int[][] board = model.getBoard();

        // 1. Vẽ bàn cờ
        for (int c = 0; c < Connect4Model.COLUMN_COUNT; c++) {
            for (int r = 0; r < Connect4Model.ROW_COUNT; r++) {
                g2d.setColor(Connect4View.COLOR_BLUE);
                g2d.fillRect(c * Connect4View.SQUARE_SIZE, (r + 1) * Connect4View.SQUARE_SIZE, Connect4View.SQUARE_SIZE, Connect4View.SQUARE_SIZE);
                g2d.setColor(Connect4View.COLOR_BLACK);
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
                    g2d.setColor((piece == Connect4Model.PLAYER_PIECE) ? Connect4View.COLOR_RED : Connect4View.COLOR_YELLOW);
                    int y = (Connect4Model.ROW_COUNT - r) * Connect4View.SQUARE_SIZE + (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2;
                    int x = c * Connect4View.SQUARE_SIZE + (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2;
                    g2d.fillOval(x, y, RADIUS * 2, RADIUS * 2);
                }
            }
        }

        // 3. Vẽ quân cờ mờ
        if (!model.isGameOver() && !controller.isAIThinking()) {
            Color hoverColor = null;
            if (model.getMode() == Connect4Model.GameMode.PVP) {
                hoverColor = (model.getTurn() == 0) ? Connect4View.COLOR_RED : Connect4View.COLOR_YELLOW;
            } else if (model.getTurn() == 0) { 
                hoverColor = Connect4View.COLOR_RED;
            }
            if (hoverColor != null) {
                g2d.setColor(hoverColor);
                g2d.fillOval(mouseX - RADIUS, (Connect4View.SQUARE_SIZE - RADIUS * 2) / 2, RADIUS * 2, RADIUS * 2);
            }
        }

        // 4. Vẽ điểm số
        Map<Integer, Integer> moveScores = controller.getLastMoveScores();
        if (moveScores != null && !moveScores.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
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
                g2d.setColor(Connect4View.COLOR_WHITE);
                g2d.drawString(scoreStr, x, y);
            }
        }

        // 5. Vẽ thông báo kết thúc game
        if (model.isGameOver()) {
            g2d.setColor(new Color(0, 0, 0, 150)); 
            g2d.fillRect(0, 0, Connect4View.SCREEN_WIDTH, Connect4View.SQUARE_SIZE); 
            g2d.setColor(Connect4View.COLOR_WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            FontMetrics metrics = g2d.getFontMetrics();
            String message = model.getGameOverMessage();
            int x = (Connect4View.SCREEN_WIDTH - metrics.stringWidth(message)) / 2;
            int y = (Connect4View.SQUARE_SIZE - metrics.getHeight()) / 2 + metrics.getAscent();
            g2d.drawString(message, x, y);
        }
    }
}