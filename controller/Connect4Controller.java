package controller;

import javax.swing.SwingWorker;
import javax.swing.Timer;
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
        this.model = new Connect4Model(mode); // tạo ra 1 đối tượng với với chế độ đã chọn
        view.showGame(model, this); // chuyển cảnh theo chế độ đã chọn
        
        if (mode == Connect4Model.GameMode.CVC) { // nếu là chế độ máy với máy thì gọi triggerAIMove() để AI đầu tiên bắt đầu đi
            triggerAIMove();
        }
    }
    
    public void handlePlayerMove(int col) { // khi người chơi click vào cột
        if (model.isGameOver() || aiThinking) return; // nếu game kết thúc hoặc AI đang suy nghĩ thì không làm gì

        boolean isHumanTurn = model.getMode() == Connect4Model.GameMode.PVP || model.getTurn() == 0; // kiểm tra xem có đúng lượt của bạn không

        if (isHumanTurn && model.getValidLocations().contains(col)) {
            lastMoveScores = null; 
            model.performMove(col); 

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
    
    public boolean isAIThinking() {
        return aiThinking;
    }

    public Map<Integer, Integer> getLastMoveScores() {
        return lastMoveScores;
    }
}