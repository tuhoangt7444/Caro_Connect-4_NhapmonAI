package model; 

import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.Stack;

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
    private String player1Name;
    private String player2Name;
    
    private final AIPlayer ai;
    private final Random random;

    private Stack<int[][]> boardHistory;
    private Stack<Integer> turnHistory;
    
    // Lưu vị trí quân mới được đặt cho animation
    private int lastMoveRow = -1;
    private int lastMoveCol = -1;
    
    // Lưu vị trí 4 cờ nối nhau khi thắng
    private int[][] winningLine = null;

    public Connect4Model(GameMode mode, String p1Name, String p2Name) {
        this.mode = mode;
        this.ai = new AIPlayer(); 
        this.random = new Random();
        this.player1Name = p1Name;
        this.player2Name = p2Name;
        this.boardHistory = new Stack<>();
        this.turnHistory = new Stack<>();
        initializeGame();
    }
    
    public void initializeGame() {
        this.board = ai.createBoard();
        this.turn = 0;
        this.gameOver = false;
        this.gameOverMessage = "";
        this.boardHistory.clear();
        this.turnHistory.clear();
    }

    public boolean performMove(int col) {
        if (gameOver || !ai.isValidLocation(board, col)) {
            return false;
        }
        this.boardHistory.push(copyBoard(this.board)); // Lưu một bản sao của bàn cờ
        this.turnHistory.push(this.turn);             // Lưu lượt đi hiện tại
        int row = ai.getNextOpenRow(board, col);
        int piece = (turn == 0) ? PLAYER_PIECE : AI_PIECE;
        ai.dropPiece(board, row, col, piece);
        
        // Lưu vị trí quân mới cho animation
        this.lastMoveRow = row;
        this.lastMoveCol = col;

        if (ai.checkWin(board, piece)) {
            gameOver = true;
            gameOverMessage = (piece == PLAYER_PIECE) ? (player1Name + " THẮNG!") : (player2Name + " THẮNG!");
            // Lưu vị trí 4 cờ nối nhau
            this.winningLine = ai.getWinningLine(board, piece);
        } else if (ai.getValidLocations(board).isEmpty()) {
            gameOver = true;
            gameOverMessage = "HÒA!";
        } else {
            turn = (turn + 1) % 2; 
        }
        return true;
    }

    public boolean undoLastMove() {
        // Nếu không có gì trong lịch sử, không làm gì cả
        if (boardHistory.isEmpty()) {
            return false;
        }
        
        // Lấy lại trạng thái cũ từ ngăn xếp
        this.board = boardHistory.pop();
        this.turn = turnHistory.pop();
        
        // Nếu game đã kết thúc, ta cũng "mở khóa" lại game
        this.gameOver = false;
        this.gameOverMessage = "";
        
        return true;
    }

    private int[][] copyBoard(int[][] original) {
        int[][] newBoard = new int[ROW_COUNT][COLUMN_COUNT];
        for (int r = 0; r < ROW_COUNT; r++) {
            System.arraycopy(original[r], 0, newBoard[r], 0, COLUMN_COUNT);
        }
        return newBoard;
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
    
    // Getters cho animation
    public int getLastMoveRow() { return lastMoveRow; }
    public int getLastMoveCol() { return lastMoveCol; }
    public int[][] getWinningLine() { return winningLine; }
    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }
}