package controller;

import javax.swing.SwingWorker;
import java.util.Map;

// Import các lớp từ các package khác
import model.Connect4Model;
import view.Connect4View;
import view.GamePanel;
import view.NameInputDialog;
import view.SoundManager;

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
        String p1Name = "Người 1"; // Tên mặc định
        String p2Name = "Người 2"; // Tên mặc định

        if (mode == Connect4Model.GameMode.PVP) {
            // Hỏi tên 2 người
            NameInputDialog dialog = new NameInputDialog(view.getFrame(), "Nhập tên người chơi", "Người 1", "Người 2", "Người 1", "Người 2");
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                p1Name = dialog.getPlayer1Name();
                p2Name = dialog.getPlayer2Name();
                if (p1Name.isEmpty()) p1Name = "Người 1";
                if (p2Name.isEmpty()) p2Name = "Người 2";
            } else {
                return; // Hủy bỏ, quay về menu
            }

        } else if (mode == Connect4Model.GameMode.PVE_EASY || mode == Connect4Model.GameMode.PVE_HARD) {
            // Hỏi tên 1 người, 1 máy
            NameInputDialog dialog = new NameInputDialog(view.getFrame(), "Nhập tên của bạn", "Tên của bạn", "", "Người chơi", "");
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                p1Name = dialog.getPlayer1Name();
                if (p1Name.isEmpty()) p1Name = "Người chơi";
                
                if (mode == Connect4Model.GameMode.PVE_EASY) p2Name = "Máy (Dễ)";
                else p2Name = "Máy (Khó)";
            } else {
                return; // Hủy bỏ, quay về menu
            }

        } else if (mode == Connect4Model.GameMode.CVC) {
            // Tự đặt tên cho 2 máy
            p1Name = "Máy (Dễ)";
            p2Name = "Máy (Khó)";
        }

        // Truyền tên vào Model
        startGameWithNames(mode, p1Name, p2Name);
    }
    
    // Khởi động game với tên đã biết (dùng khi chơi lại)
    public void restartGameWithSameName(Connect4Model.GameMode mode) {
        if (model == null) {
            startGame(mode);
            return;
        }
        String p1Name = model.getPlayer1Name();
        String p2Name = model.getPlayer2Name();
        startGameWithNames(mode, p1Name, p2Name);
    }
    
    // Hàm riêng để khởi động game với tên đã cho
    private void startGameWithNames(Connect4Model.GameMode mode, String p1Name, String p2Name) {
        this.model = new Connect4Model(mode, p1Name, p2Name);
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
            SoundManager.playPlacementSound(); // Thêm sound khi đặt quân

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
                        SoundManager.playPlacementSound(); // Thêm sound khi đặt quân
                        gamePanel.startFallingAnimation(col);
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
        // Gọi blink effect từ GamePanel thay vì auto quay về menu
        SoundManager.playGameEndSound(); // Thêm sound khi game kết thúc
        gamePanel.startBlinkEffect();
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