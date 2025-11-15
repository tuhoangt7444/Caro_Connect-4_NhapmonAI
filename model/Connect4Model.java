package model; 

import java.util.List;
import java.util.Random;
import java.util.Map;

/**
 * Lớp Model
 */
public class Connect4Model {
    
    // Hằng số
    public static final int ROW_COUNT = 6;
    public static final int COLUMN_COUNT = 7;
    public static final int EMPTY = 0;
    public static final int PLAYER_PIECE = 1;
    public static final int AI_PIECE = 2;
    public static final int DEPTH_EASY = 1;
    public static final int DEPTH_HARD = 5;

    public enum GameMode {
        PVP, CVC, PVE_EASY, PVE_HARD
    }
    
    // Trạng thái game
    private int[][] board;
    private GameMode mode;
    private int turn; 
    private boolean gameOver;
    private String gameOverMessage;
    
    private final AIPlayer ai;
    private final Random random;

    public Connect4Model(GameMode mode) {
        this.mode = mode;
        this.ai = new AIPlayer(); 
        this.random = new Random();
        initializeGame();
    }
    
    public void initializeGame() {
        this.board = ai.createBoard();
        this.turn = 0;
        this.gameOver = false;
        this.gameOverMessage = "";
    }

    public boolean performMove(int col) {
        if (gameOver || !ai.isValidLocation(board, col)) {
            return false;
        }
        int row = ai.getNextOpenRow(board, col);
        int piece = (turn == 0) ? PLAYER_PIECE : AI_PIECE;
        ai.dropPiece(board, row, col, piece);

        if (ai.checkWin(board, piece)) {
            gameOver = true;
            gameOverMessage = "Người chơi " + ((piece == PLAYER_PIECE) ? 1 : 2) + " THẮNG!";
        } else if (ai.getValidLocations(board).isEmpty()) {
            gameOver = true;
            gameOverMessage = "HÒA!";
        } else {
            turn = (turn + 1) % 2; 
        }
        return true;
    }

    public Object[] getAIMoveWithScores() {
        int piece = (turn == 0) ? PLAYER_PIECE : AI_PIECE;
        boolean isRandom = false;
        int depth = 0;
        
        if (mode == GameMode.PVE_EASY && turn == 1) {
            isRandom = true;
        } else if (mode == GameMode.PVE_HARD && turn == 1) {
            depth = DEPTH_HARD;
        } else if (mode == GameMode.CVC) {
            if (turn == 0) isRandom = true; 
            else depth = DEPTH_HARD;       
        }

        if (isRandom) {
            List<Integer> validCols = ai.getValidLocations(board);
            if (!validCols.isEmpty()) {
                return new Object[]{validCols.get(random.nextInt(validCols.size())), null};
            }
        } else if (depth > 0) {
            boolean maximizing = (piece == AI_PIECE); 
            return ai.getBestMoveAndAllScores(board, depth, maximizing);
        }
        return new Object[]{-1, null}; 
    }

    // Getters
    public int[][] getBoard() { return board; }
    public GameMode getMode() { return mode; }
    public int getTurn() { return turn; }
    public boolean isGameOver() { return gameOver; }
    public String getGameOverMessage() { return gameOverMessage; }
    public List<Integer> getValidLocations() { return ai.getValidLocations(board); }

    public boolean isAITurn() {
        if (gameOver) return false;
        if ((mode == GameMode.PVE_EASY || mode == GameMode.PVE_HARD) && turn == 1) return true;
        if (mode == GameMode.CVC) return true;
        return false;
    }
}