package controller;

import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import java.util.Map;

// Import các lớp từ các package khác
import model.Connect4Model;
import view.Connect4View;
import view.GamePanel;

/**
 * Lớp Controller
 */
public class Connect4Controller {

    private Connect4Model model;
    public GamePanel gamePanel; 
    private final Connect4View view; 
    
    private boolean aiThinking = false;// khi AI đang suy nghĩ ngăn người chơi click chuột
    private Map<Integer, Integer> lastMoveScores; 

    public Connect4Controller(Connect4View view) {
        this.view = view;
    }

    public void startGame(Connect4Model.GameMode mode) { // khi bấm nút chọn chế độ
        // --- BẮT ĐẦU THÊM CODE MỚI ---
        String p1Name = "Người 1"; // Tên mặc định
        String p2Name = "Người 2"; // Tên mặc định

        if (mode == Connect4Model.GameMode.PVP) {
            // Hỏi tên 2 người
            p1Name = JOptionPane.showInputDialog(null, "Nhập tên Người 1:", "Người 1");
            p2Name = JOptionPane.showInputDialog(null, "Nhập tên Người 2:", "Người 2");
            if (p1Name == null || p1Name.trim().isEmpty()) p1Name = "Người 1";
            if (p2Name == null || p2Name.trim().isEmpty()) p2Name = "Người 2";

        } else if (mode == Connect4Model.GameMode.PVE_EASY || mode == Connect4Model.GameMode.PVE_HARD) {
            // Hỏi tên 1 người, 1 máy
            p1Name = JOptionPane.showInputDialog(null, "Nhập tên của bạn:", "Người 1");
            if (p1Name == null || p1Name.trim().isEmpty()) p1Name = "Người 1";
            
            if (mode == Connect4Model.GameMode.PVE_EASY) p2Name = "Máy (Dễ)";
            else p2Name = "Máy (Khó)";

        } else if (mode == Connect4Model.GameMode.CVC) {
            // Tự đặt tên cho 2 máy
            p1Name = "Máy (Dễ)";
            p2Name = "Máy (Khó)";
        }
        // --- KẾT THÚC CODE MỚI ---

        // Sửa dòng này để truyền tên vào Model
        this.model = new Connect4Model(mode, p1Name, p2Name); // <--- SỬA DÒNG NÀY
        
        view.showGame(model, this); 
        
        if (mode == Connect4Model.GameMode.CVC) {
            triggerAIMove();
        }
    }
    
    public void handlePlayerMove(int col) { // khi người chơi click vào cột
        if (model.isGameOver() || aiThinking) return; // nếu game kết thúc hoặc AI đang suy nghĩ thì không làm gì

        boolean isHumanTurn = model.getMode() == Connect4Model.GameMode.PVP || model.getTurn() == 0; // kiểm tra xem có đúng lượt của bạn không

        if (isHumanTurn && model.getValidLocations().contains(col)) {
            lastMoveScores = null; 
            model.performMove(col);

            gamePanel.startFallingAnimation(col); //falling animation

            if (model.isGameOver()) {
                endGame();
            } else if (model.isAITurn()) {
                triggerAIMove(); 
            }
            gamePanel.repaint(); 
        }
    }
    
    public void triggerAIMove() {
        if (model.isGameOver() || aiThinking) return;
        
        aiThinking = true;
        lastMoveScores = null; 
        gamePanel.repaint(); 

        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            
            @Override
            protected Object[] doInBackground() throws Exception {
                if (model.getMode() == Connect4Model.GameMode.PVE_EASY || (model.getMode() == Connect4Model.GameMode.CVC && model.getTurn() == 0)) {
                    Thread.sleep(500); 
                }
                return model.getAIMoveWithScores();
            }

            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    int col = (Integer) result[0];
                    
                    if ((model.getMode() == Connect4Model.GameMode.PVE_HARD && model.getTurn() == 1) || (model.getMode() == Connect4Model.GameMode.CVC && model.getTurn() == 1)) {
                         lastMoveScores = (Map<Integer, Integer>) result[1];
                    }

                    if (col != -1) {
                        model.performMove(col); 
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    aiThinking = false;
                    
                    if (model.isGameOver()) {
                        endGame();
                    } else if (model.getMode() == Connect4Model.GameMode.CVC) {
                        triggerAIMove(); 
                    }
                    gamePanel.repaint(); 
                }
            }
        };
        worker.execute();
    }
    
    private void endGame() {
        Timer exitTimer = new Timer(3000, e -> view.showMenu()); 
        exitTimer.setRepeats(false);
        exitTimer.start();
    }
    // --- THÊM HÀM MỚI: XỬ LÝ UNDO ---
    public void handleUndo() {
        if (aiThinking) return; // Không undo khi AI đang suy nghĩ

        // Nếu game vừa kết thúc, chỉ cần lùi 1 bước
        if (model.isGameOver()) {
            model.undoLastMove();
            gamePanel.repaint();
            return;
        }

        // Xử lý logic undo cho PVE (Người vs Máy)
        if (model.getMode() == Connect4Model.GameMode.PVE_EASY || model.getMode() == Connect4Model.GameMode.PVE_HARD) {
            // Chúng ta cần lùi 2 bước:
            // 1. Lùi nước đi của AI
            // 2. Lùi nước đi của Người
            // để quay về đúng lượt của Người.
            model.undoLastMove(); // Lùi của AI
            model.undoLastMove(); // Lùi của Người
        } 
        // Xử lý cho PVP (Người vs Người)
        else if (model.getMode() == Connect4Model.GameMode.PVP) {
            model.undoLastMove(); // Chỉ lùi 1 bước
        }
        // (Chúng ta có thể bỏ qua CVC, vì undo không có nhiều ý nghĩa)

        // Xóa điểm số AI cũ và vẽ lại bàn cờ
        lastMoveScores = null;
        gamePanel.repaint();
    }

    // --- THÊM HÀM MỚI: XỬ LÝ THOÁT VỀ MENU ---
    public void handleExitToMenu() {
        view.showMenu(); // Chỉ cần gọi View hiển thị lại Menu
    } 
    public boolean isAIThinking() {
        return aiThinking;
    }

    public Map<Integer, Integer> getLastMoveScores() {
        return lastMoveScores;
    }
}