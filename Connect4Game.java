import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
// THÊM CÁC IMPORT NÀY
import java.util.Map;
import java.util.HashMap;

/**
 * Lớp chính của game Connect 4, chứa hàm main() và JFrame.
 * Toàn bộ logic được đóng gói trong file này với các lớp lồng nhau.
 */
public class Connect4Game {

    // --- HẰNG SỐ CỦA GAME ---

    // Kích thước logic
    public static final int ROW_COUNT = 6;
    public static final int COLUMN_COUNT = 7;

    // Định danh người chơi
    public static final int EMPTY = 0;
    public static final int PLAYER_PIECE = 1; // Người 1 (Đỏ)
    public static final int AI_PIECE = 2;     // Người 2 / AI (Vàng)

    // Độ sâu tìm kiếm AI
    public static final int DEPTH_EASY = 1; // Thực tế sẽ dùng Random
    public static final int DEPTH_HARD = 5; // Tăng độ sâu nếu muốn AI mạnh hơn (ví dụ: 6 hoặc 7)

    // Kích thước đồ họa (pixels)
    public static final int SQUARE_SIZE = 100;
    public static final int RADIUS = (int) (SQUARE_SIZE / 2 - 5);
    public static final int SCREEN_WIDTH = COLUMN_COUNT * SQUARE_SIZE;
    // Thêm 1 hàng ở trên cho việc "thả" quân cờ
    public static final int SCREEN_HEIGHT = (ROW_COUNT + 1) * SQUARE_SIZE;

    // Màu sắc
    public static final Color COLOR_BLUE = new Color(0, 100, 200);
    public static final Color COLOR_BLACK = Color.BLACK;
    public static final Color COLOR_RED = new Color(200, 0, 0);
    public static final Color COLOR_YELLOW = new Color(200, 200, 0);
    public static final Color COLOR_WHITE = Color.WHITE;

    // Chế độ chơi (dùng enum cho rõ ràng)
    public enum GameMode {
        PVP,      // Người vs Người
        CVC,      // Máy vs Máy
        PVE_EASY, // Người vs Máy (Dễ)
        PVE_HARD  // Người vs Máy (Khó)
    }

    // Biến JFrame chính, static để các Panel có thể truy cập
    private static JFrame frame;

    /**
     * Hàm main - Điểm khởi đầu của ứng dụng
     */
    public static void main(String[] args) {
        // Đảm bảo GUI được tạo trên Event Dispatch Thread (luồng sự kiện)
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Connect 4 AI (Java Swing)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Kích thước cửa sổ cần + 1 chút cho thanh tiêu đề (tùy HĐH)
            frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT + 37);
            frame.setLocationRelativeTo(null); // Căn giữa màn hình
            frame.setResizable(false);

            showMenu(); // Hiển thị MenuPanel đầu tiên

            frame.setVisible(true);
        });
    }

    /**
     * Hiển thị Menu chính
     */
    public static void showMenu() {
        frame.getContentPane().removeAll();
        frame.add(new MenuPanel());
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Bắt đầu game với chế độ đã chọn
     */
    public static void startGame(GameMode mode) {
        frame.getContentPane().removeAll();
        frame.add(new GamePanel(mode));
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Lớp MenuPanel (lớp lồng nhau - nested class)
     * Hiển thị các nút chọn chế độ chơi.
     */
    static class MenuPanel extends JPanel {
        
        public MenuPanel() {
            setLayout(new GridBagLayout()); // Dùng GridBagLayout để căn giữa dễ dàng
            setBackground(COLOR_BLACK);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10); // Khoảng cách giữa các nút
            gbc.gridy = 0; // Vị trí hàng (y)

            // Tiêu đề
            JLabel title = new JLabel("CONNECT 4 AI");
            title.setFont(new Font("Arial", Font.BOLD, 50));
            title.setForeground(COLOR_WHITE);
            gbc.gridy++;
            add(title, gbc);

            // Thêm khoảng trống
            gbc.gridy++;
            add(Box.createVerticalStrut(30), gbc);

            // Nút 1: Người vs Người
            JButton pvpButton = createMenuButton("Người vs Người");
            pvpButton.addActionListener(e -> Connect4Game.startGame(GameMode.PVP));
            gbc.gridy++;
            add(pvpButton, gbc);

            // Nút 2: Người vs Máy (Dễ)
            JButton pveEasyButton = createMenuButton("Người vs Máy (Dễ)");
            pveEasyButton.addActionListener(e -> Connect4Game.startGame(GameMode.PVE_EASY));
            gbc.gridy++;
            add(pveEasyButton, gbc);

            // Nút 3: Người vs Máy (Khó)
            JButton pveHardButton = createMenuButton("Người vs Máy (Khó)");
            pveHardButton.addActionListener(e -> Connect4Game.startGame(GameMode.PVE_HARD));
            gbc.gridy++;
            add(pveHardButton, gbc);

            // Nút 4: Máy (Dễ) vs Máy (Khó)
            JButton cvcButton = createMenuButton("Máy (Dễ) vs Máy (Khó)");
            cvcButton.addActionListener(e -> Connect4Game.startGame(GameMode.CVC));
            gbc.gridy++;
            add(cvcButton, gbc);

            // Thêm khoảng trống
            gbc.gridy++;
            add(Box.createVerticalStrut(30), gbc);

            // Nút 5: Thoát
            JButton quitButton = createMenuButton("Thoát");
            quitButton.addActionListener(e -> System.exit(0));
            gbc.gridy++;
            add(quitButton, gbc);
        }

        // Hàm tiện ích tạo nút bấm với style chung
        private JButton createMenuButton(String text) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 20));
            button.setBackground(COLOR_BLUE);
            button.setForeground(COLOR_WHITE);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(300, 50));
            return button;
        }
    }

    /**
     * Lớp GamePanel (lớp lồng nhau)
     * Nơi diễn ra toàn bộ logic vẽ và xử lý game.
     */
    static class GamePanel extends JPanel {

        private int[][] board;
        private GameMode mode;
        private int turn; // 0 = P1 (Đỏ), 1 = P2/AI (Vàng)
        private boolean gameOver;
        private String gameOverMessage = "";
        private int mouseX = 0;
        private boolean aiThinking = false;
        
        // --- THAY ĐỔI 1: Thêm Map để lưu điểm số ---
        private Map<Integer, Integer> moveScores = new HashMap<>();
        
        private AIPlayer ai; // Đối tượng chứa logic AI
        private Random random = new Random();

        public GamePanel(GameMode mode) {
            this.mode = mode;
            this.ai = new AIPlayer(); // Khởi tạo bộ não AI
            this.board = ai.createBoard();
            this.turn = 0; // P1 luôn đi trước
            this.gameOver = false;

            setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
            setBackground(COLOR_BLACK);
            
            // Thêm bộ lắng nghe chuột
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMouseClick(e.getX());
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mouseX = e.getX();
                    repaint(); // Vẽ lại để cập nhật vị trí quân cờ mờ
                }
            });

            // Nếu chế độ CVC, AI tự động đi nước đầu tiên
            if (mode == GameMode.CVC) {
                triggerAIMove();
            }
        }

        /**
         * Xử lý khi người chơi click chuột
         */
        private void handleMouseClick(int x) {
            if (gameOver || aiThinking) return;

            // Kiểm tra xem có phải lượt của người không
            boolean isHumanTurn = false;
            if (mode == GameMode.PVP) isHumanTurn = true;
            if ((mode == GameMode.PVE_EASY || mode == GameMode.PVE_HARD) && turn == 0) isHumanTurn = true;

            if (isHumanTurn) {
                int col = x / SQUARE_SIZE;
                if (ai.isValidLocation(board, col)) {
                    // Xóa điểm số cũ khi người chơi đi
                    moveScores.clear();
                    performMove(col); // Người thực hiện nước đi
                    
                    // Nếu sau đó là lượt AI, kích hoạt AI
                    if (!gameOver && (mode == GameMode.PVE_EASY || mode == GameMode.PVE_HARD) && turn == 1) {
                        triggerAIMove();
                    }
                }
            }
        }

        /**
         * Kích hoạt AI suy nghĩ trong một luồng (thread) riêng
         * để không làm "đơ" (freeze) giao diện.
         */
        private void triggerAIMove() {
            aiThinking = true;
            moveScores.clear(); // Xóa điểm số của lượt trước
            repaint(); // Cập nhật giao diện (ví dụ: ẩn quân cờ mờ)

            // --- THAY ĐỔI 2: Đổi kiểu SwingWorker sang Object[] ---
            // Nó sẽ trả về {cột, mapĐiểmSố}
            SwingWorker<Object[], Void> worker = new SwingWorker<>() {
                
                @Override
                protected Object[] doInBackground() throws Exception {
                    // Logic xác định nước đi của AI
                    int piece = (turn == 0) ? PLAYER_PIECE : AI_PIECE;
                    boolean isRandom = false;
                    int depth = 0;

                    if (mode == GameMode.PVE_EASY && turn == 1) {
                        isRandom = true;
                    } else if (mode == GameMode.PVE_HARD && turn == 1) {
                        depth = DEPTH_HARD;
                    } else if (mode == GameMode.CVC) {
                        if (turn == 0) isRandom = true; // AI 1 (Dễ)
                        else depth = DEPTH_HARD;        // AI 2 (Khó)
                    }

                    // Tính toán nước đi
                    if (isRandom) {
                        // AI Dễ (Random)
                        Thread.sleep(500); // Giả lập suy nghĩ
                        List<Integer> validCols = ai.getValidLocations(board);
                        if (!validCols.isEmpty()) {
                            // Trả về {cột, map rỗng}
                            return new Object[]{validCols.get(random.nextInt(validCols.size())), new HashMap<>()};
                        }
                    } else {
                        // AI Khó (Minimax) - GỌI HÀM MỚI
                        boolean maximizing = (piece == AI_PIECE);
                        return ai.getBestMoveAndAllScores(board, depth, maximizing);
                    }
                    // Trả về {cột -1, map rỗng} nếu thất bại
                    return new Object[]{-1, new HashMap<>()};
                }

                @Override
                protected void done() {
                    // Hàm này được gọi khi doInBackground() hoàn tất
                    try {
                        // --- THAY ĐỔI 3: Nhận kết quả là Object[] ---
                        Object[] result = get();
                        int col = (Integer) result[0]; // Lấy cột
                        
                        // Chỉ lưu điểm số nếu đó là lượt của AI Khó
                        if ((mode == GameMode.PVE_HARD && turn == 1) || (mode == GameMode.CVC && turn == 1)) {
                             moveScores = (Map<Integer, Integer>) result[1]; // Lấy map điểm số
                        }

                        if (col != -1) {
                            performMove(col); // Thực hiện nước đi trên bàn cờ
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        aiThinking = false;
                        
                        // Nếu là CVC và game chưa kết thúc, tự động gọi lượt AI tiếp theo
                        if (mode == GameMode.CVC && !gameOver) {
                            triggerAIMove();
                        }
                        
                        repaint(); // Vẽ lại bàn cờ sau khi AI đi (để hiển thị điểm)
                    }
                }
            };
            
            worker.execute(); // Bắt đầu chạy luồng SwingWorker
        }

        /**
         * Thực hiện một nước đi (cả người và máy) và cập nhật trạng thái game
         */
        private void performMove(int col) {
            if (gameOver || !ai.isValidLocation(board, col)) return;
            
            int row = ai.getNextOpenRow(board, col);
            int piece = (turn == 0) ? PLAYER_PIECE : AI_PIECE;
            ai.dropPiece(board, row, col, piece);

            if (ai.checkWin(board, piece)) {
                gameOver = true;
                gameOverMessage = "Người chơi " + piece + " THẮNG!";
                moveScores.clear(); // Xóa điểm số khi game kết thúc
            } else if (ai.getValidLocations(board).isEmpty()) {
                gameOver = true;
                gameOverMessage = "HÒA!";
                moveScores.clear();
            } else {
                turn = (turn + 1) % 2; // Đổi lượt (0 -> 1, 1 -> 0)
            }
            
            repaint(); // Yêu cầu vẽ lại toàn bộ panel

            // Nếu game kết thúc, đợi 3 giây rồi quay lại menu
            if (gameOver) {
                Timer exitTimer = new Timer(3000, e -> Connect4Game.showMenu());
                exitTimer.setRepeats(false); // Chỉ chạy 1 lần
                exitTimer.start();
            }
        }

        /**
         * Hàm vẽ chính (Ghi đè từ JPanel)
         * Đây là nơi tất cả đồ họa diễn ra.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Bật khử răng cưa cho đẹp
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Vẽ bàn cờ (các ô vuông xanh và lỗ đen)
            // (Code giữ nguyên)
            for (int c = 0; c < COLUMN_COUNT; c++) {
                for (int r = 0; r < ROW_COUNT; r++) {
                    g2d.setColor(COLOR_BLUE);
                    g2d.fillRect(c * SQUARE_SIZE, (r + 1) * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                    g2d.setColor(COLOR_BLACK);
                    g2d.fillOval(c * SQUARE_SIZE + (SQUARE_SIZE - RADIUS * 2) / 2,
                                 (r + 1) * SQUARE_SIZE + (SQUARE_SIZE - RADIUS * 2) / 2,
                                 RADIUS * 2, RADIUS * 2);
                }
            }

            // 2. Vẽ các quân cờ đã thả (Đỏ và Vàng)
            // (Code giữ nguyên)
            for (int c = 0; c < COLUMN_COUNT; c++) {
                for (int r = 0; r < ROW_COUNT; r++) {
                    int piece = board[r][c];
                    if (piece != EMPTY) {
                        g2d.setColor((piece == PLAYER_PIECE) ? COLOR_RED : COLOR_YELLOW);
                        int y = (ROW_COUNT - r) * SQUARE_SIZE + (SQUARE_SIZE - RADIUS * 2) / 2;
                        int x = c * SQUARE_SIZE + (SQUARE_SIZE - RADIUS * 2) / 2;
                        g2d.fillOval(x, y, RADIUS * 2, RADIUS * 2);
                    }
                }
            }

            // 3. Vẽ quân cờ mờ (theo chuột)
            // (Code giữ nguyên)
            if (!gameOver && !aiThinking) {
                Color hoverColor = null;
                if ((mode == GameMode.PVP || mode == GameMode.PVE_EASY || mode == GameMode.PVE_HARD) && turn == 0) {
                    hoverColor = COLOR_RED;
                } else if (mode == GameMode.PVP && turn == 1) {
                    hoverColor = COLOR_YELLOW;
                }

                if (hoverColor != null) {
                    g2d.setColor(hoverColor);
                    g2d.fillOval(mouseX - RADIUS, (SQUARE_SIZE - RADIUS * 2) / 2, RADIUS * 2, RADIUS * 2);
                }
            }

            // --- THAY ĐỔI 4: Vẽ điểm số (NẾU CÓ) ---
            if (!moveScores.isEmpty()) {
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                FontMetrics metrics = g2d.getFontMetrics();

                for (Map.Entry<Integer, Integer> entry : moveScores.entrySet()) {
                    int col = entry.getKey();
                    int score = entry.getValue();
                    String scoreStr;

                    // Làm cho điểm số dễ đọc hơn
                    if (score > 9999999) scoreStr = "WIN";
                    else if (score < -9999999) scoreStr = "LOSE";
                    else scoreStr = Integer.toString(score);
                    
                    // Tính vị trí để căn giữa chữ
                    int x = (col * SQUARE_SIZE) + (SQUARE_SIZE - metrics.stringWidth(scoreStr)) / 2;
                    int y = (SQUARE_SIZE - metrics.getHeight()) / 2 + metrics.getAscent();

                    g2d.setColor(COLOR_WHITE);
                    g2d.drawString(scoreStr, x, y);
                }
            }

            // 5. Vẽ thông báo kết thúc game (Đổi số thứ tự)
            if (gameOver) {
                g2d.setColor(new Color(0, 0, 0, 150)); // Màu đen mờ
                g2d.fillRect(0, 0, SCREEN_WIDTH, SQUARE_SIZE); // Vẽ nền cho chữ
                
                g2d.setColor(COLOR_WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 50));
                FontMetrics metrics = g2d.getFontMetrics();
                int x = (SCREEN_WIDTH - metrics.stringWidth(gameOverMessage)) / 2;
                int y = (SQUARE_SIZE - metrics.getHeight()) / 2 + metrics.getAscent();
                g2d.drawString(gameOverMessage, x, y);
            }
        }
    }

    /**
     * Lớp AIPlayer (lớp lồng nhau)
     * Chứa toàn bộ "bộ não" AI: logic game và Minimax.
     * Tách biệt logic này khỏi GamePanel (GUI).
     */
    static class AIPlayer {
        private Random random = new Random();
        
        // --- 1. HÀM LOGIC GAME CƠ BẢN ---
        // (Toàn bộ phần này giữ nguyên)

        public int[][] createBoard() {
            return new int[ROW_COUNT][COLUMN_COUNT]; 
        }

        public void dropPiece(int[][] board, int row, int col, int piece) {
            board[row][col] = piece;
        }

        public boolean isValidLocation(int[][] board, int col) {
            return board[ROW_COUNT - 1][col] == EMPTY;
        }

        public int getNextOpenRow(int[][] board, int col) {
            for (int r = 0; r < ROW_COUNT; r++) {
                if (board[r][col] == EMPTY) {
                    return r; 
                }
            }
            return -1; // Cột đã đầy
        }

        public boolean checkWin(int[][] board, int piece) {
            // Kiểm tra hàng ngang
            for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                for (int r = 0; r < ROW_COUNT; r++) {
                    if (board[r][c] == piece && board[r][c + 1] == piece && board[r][c + 2] == piece && board[r][c + 3] == piece) {
                        return true;
                    }
                }
            }
            // Kiểm tra hàng dọc
            for (int c = 0; c < COLUMN_COUNT; c++) {
                for (int r = 0; r < ROW_COUNT - 3; r++) {
                    if (board[r][c] == piece && board[r + 1][c] == piece && board[r + 2][c] == piece && board[r + 3][c] == piece) {
                        return true;
                    }
                }
            }
            // Kiểm tra chéo dương (/)
            for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                for (int r = 0; r < ROW_COUNT - 3; r++) {
                    if (board[r][c] == piece && board[r + 1][c + 1] == piece && board[r + 2][c + 2] == piece && board[r + 3][c + 3] == piece) {
                        return true;
                    }
                }
            }
            // Kiểm tra chéo âm (\)
            for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                for (int r = 3; r < ROW_COUNT; r++) { // Bắt đầu từ hàng 3
                    if (board[r][c] == piece && board[r - 1][c + 1] == piece && board[r - 2][c + 2] == piece && board[r - 3][c + 3] == piece) {
                        return true;
                    }
                }
            }
            return false;
        }

        public List<Integer> getValidLocations(int[][] board) {
            List<Integer> validLocations = new ArrayList<>();
            for (int col = 0; col < COLUMN_COUNT; col++) {
                if (isValidLocation(board, col)) {
                    validLocations.add(col);
                }
            }
            return validLocations;
        }

        public boolean isTerminalNode(int[][] board) {
            return checkWin(board, PLAYER_PIECE) || checkWin(board, AI_PIECE) || getValidLocations(board).isEmpty();
        }

        // --- 2. HÀM ĐÁNH GIÁ (HEURISTIC) ---
        // (Toàn bộ phần này giữ nguyên)

        private int evaluateWindow(int[] window, int piece) {
            int score = 0;
            int oppPiece = (piece == PLAYER_PIECE) ? AI_PIECE : PLAYER_PIECE;

            int pieceCount = 0;
            int oppPieceCount = 0;
            int emptyCount = 0;

            for (int p : window) {
                if (p == piece) pieceCount++;
                else if (p == oppPiece) oppPieceCount++;
                else emptyCount++;
            }

            if (pieceCount == 4) score += 1000; // Thắng tuyệt đối
            else if (pieceCount == 3 && emptyCount == 1) score += 10;
            else if (pieceCount == 2 && emptyCount == 2) score += 2;

            if (oppPieceCount == 3 && emptyCount == 1) score -= 80; // Chặn (quan trọng)

            return score;
        }

        public int scorePosition(int[][] board, int piece) {
            int score = 0;
            
            // Ưu tiên cột giữa
            int centerCount = 0;
            for (int r = 0; r < ROW_COUNT; r++) {
                if (board[r][COLUMN_COUNT / 2] == piece) centerCount++;
            }
            score += centerCount * 3;

            // Đánh giá hàng ngang
            for (int r = 0; r < ROW_COUNT; r++) {
                int[] rowArray = board[r];
                for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                    int[] window = {rowArray[c], rowArray[c + 1], rowArray[c + 2], rowArray[c + 3]};
                    score += evaluateWindow(window, piece);
                }
            }

            // Đánh giá hàng dọc
            for (int c = 0; c < COLUMN_COUNT; c++) {
                for (int r = 0; r < ROW_COUNT - 3; r++) {
                    int[] window = {board[r][c], board[r + 1][c], board[r + 2][c], board[r + 3][c]};
                    score += evaluateWindow(window, piece);
                }
            }
            
            // Đánh giá chéo dương (/)
            for (int r = 0; r < ROW_COUNT - 3; r++) {
                for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                    int[] window = {board[r][c], board[r + 1][c + 1], board[r + 2][c + 2], board[r + 3][c + 3]};
                    score += evaluateWindow(window, piece);
                }
            }

            // Đánh giá chéo âm (\)
            for (int r = 3; r < ROW_COUNT; r++) {
                for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                    int[] window = {board[r][c], board[r - 1][c + 1], board[r - 2][c + 2], board[r - 3][c + 3]};
                    score += evaluateWindow(window, piece);
                }
            }
            
            return score;
        }

        // --- THAY ĐỔI 5: Thêm hàm mới getBestMoveAndAllScores ---

        /**
         * HÀM MỚI: Lấy nước đi tốt nhất VÀ điểm số của TẤT CẢ các nước đi
         * Trả về một mảng Object:
         * [0] -> (Integer) Cột tốt nhất
         * [1] -> (Map<Integer, Integer>) Map của {Cột -> Điểm số}
         */
        public Object[] getBestMoveAndAllScores(int[][] board, int depth, boolean maximizingPlayer) {
            Map<Integer, Integer> scores = new HashMap<>();
            List<Integer> validLocations = getValidLocations(board);
            
            int bestColumn = -1;
            int bestValue;

            if (maximizingPlayer) {
                bestValue = Integer.MIN_VALUE;
                bestColumn = validLocations.get(random.nextInt(validLocations.size()));

                for (int col : validLocations) {
                    int row = getNextOpenRow(board, col);
                    int[][] tempBoard = copyBoard(board);
                    dropPiece(tempBoard, row, col, AI_PIECE);
                    
                    int newScore = minimax(tempBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false)[1];
                    scores.put(col, newScore); // LƯU ĐIỂM SỐ CỦA CỘT NÀY

                    if (newScore > bestValue) {
                        bestValue = newScore;
                        bestColumn = col;
                    }
                }
            } else { 
                // Lượt của Minimizing Player (AI Khó chơi quân Đỏ)
                bestValue = Integer.MAX_VALUE;
                bestColumn = validLocations.get(random.nextInt(validLocations.size()));
                
                for (int col : validLocations) {
                    int row = getNextOpenRow(board, col);
                    int[][] tempBoard = copyBoard(board);
                    dropPiece(tempBoard, row, col, PLAYER_PIECE); 

                    int newScore = minimax(tempBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true)[1];
                    scores.put(col, newScore);

                    if (newScore < bestValue) { // Tìm điểm nhỏ nhất
                        bestValue = newScore;
                        bestColumn = col;
                    }
                }
            }
            
            return new Object[]{bestColumn, scores};
        }

        // --- 3. THUẬT TOÁN MINIMAX (ALPHA-BETA) ---
        // (Hàm minimax gốc giữ nguyên 100%)
        /**
         * Thuật toán Minimax với Cắt tỉa Alpha-Beta
         * Trả về một mảng: {cột tốt nhất, điểm số}
         */
        public int[] minimax(int[][] board, int depth, int alpha, int beta, boolean maximizingPlayer) {
            List<Integer> validLocations = getValidLocations(board);
            boolean isTerminal = isTerminalNode(board);

            if (depth == 0 || isTerminal) {
                if (isTerminal) {
                    if (checkWin(board, AI_PIECE)) return new int[]{-1, 100000000};
                    else if (checkWin(board, PLAYER_PIECE)) return new int[]{-1, -100000000};
                    else return new int[]{-1, 0}; // Hòa
                } else { // Hết độ sâu
                    return new int[]{-1, scorePosition(board, AI_PIECE)}; // Luôn đánh giá từ góc nhìn của AI
                }
            }

            if (maximizingPlayer) { // Lượt của AI (Max)
                int value = Integer.MIN_VALUE;
                int column = validLocations.get(random.nextInt(validLocations.size())); // Chọn ngẫu nhiên ban đầu

                for (int col : validLocations) {
                    int row = getNextOpenRow(board, col);
                    int[][] tempBoard = copyBoard(board);
                    dropPiece(tempBoard, row, col, AI_PIECE);
                    
                    int newScore = minimax(tempBoard, depth - 1, alpha, beta, false)[1];
                    
                    if (newScore > value) {
                        value = newScore;
                        column = col;
                    }
                    alpha = Math.max(alpha, value);
                    if (alpha >= beta) break;
                }
                return new int[]{column, value};
                
            } else { // Lượt của Người (Min)
                int value = Integer.MAX_VALUE;
                int column = validLocations.get(random.nextInt(validLocations.size()));

                for (int col : validLocations) {
                    int row = getNextOpenRow(board, col);
                    int[][] tempBoard = copyBoard(board);
                    dropPiece(tempBoard, row, col, PLAYER_PIECE);
                    
                    int newScore = minimax(tempBoard, depth - 1, alpha, beta, true)[1];
                    
                    if (newScore < value) {
                        value = newScore;
                        column = col;
                    }
                    beta = Math.min(beta, value);
                    if (alpha >= beta) break;
                }
                return new int[]{column, value};
            }
        }

        // Hàm tiện ích để sao chép bàn cờ (Java truyền mảng theo tham chiếu)
        // (Code giữ nguyên)
        private int[][] copyBoard(int[][] original) {
            int[][] newBoard = new int[ROW_COUNT][COLUMN_COUNT];
            for (int r = 0; r < ROW_COUNT; r++) {
                System.arraycopy(original[r], 0, newBoard[r], 0, COLUMN_COUNT);
            }
            return newBoard;
        }
    }
}