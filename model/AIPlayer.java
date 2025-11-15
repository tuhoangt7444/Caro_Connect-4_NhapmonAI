package model; 

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/**
 * Lớp AIPlayer (nằm trong package 'model')
 */
public class AIPlayer {
    private Random random = new Random();

    public int[][] createBoard() {
        // Tham chiếu đến hằng số của Connect4Model (cùng package)
        return new int[Connect4Model.ROW_COUNT][Connect4Model.COLUMN_COUNT];
    }

    public void dropPiece(int[][] board, int row, int col, int piece) {
        board[row][col] = piece;
    }

    public boolean isValidLocation(int[][] board, int col) {
        if (col < 0 || col >= Connect4Model.COLUMN_COUNT) return false;
        return board[Connect4Model.ROW_COUNT - 1][col] == Connect4Model.EMPTY;
    }

    public int getNextOpenRow(int[][] board, int col) {
        for (int r = 0; r < Connect4Model.ROW_COUNT; r++) {
            if (board[r][col] == Connect4Model.EMPTY) {
                return r;
            }
        }
        return -1;
    }

    public boolean checkWin(int[][] board, int piece) {
        // Ngang
        for (int c = 0; c < Connect4Model.COLUMN_COUNT - 3; c++) {
            for (int r = 0; r < Connect4Model.ROW_COUNT; r++) {
                if (board[r][c] == piece && board[r][c + 1] == piece && board[r][c + 2] == piece && board[r][c + 3] == piece) {
                    return true;
                }
            }
        }
        // Dọc
        for (int c = 0; c < Connect4Model.COLUMN_COUNT; c++) {
            for (int r = 0; r < Connect4Model.ROW_COUNT - 3; r++) {
                if (board[r][c] == piece && board[r + 1][c] == piece && board[r + 2][c] == piece && board[r + 3][c] == piece) {
                    return true;
                }
            }
        }
        // Chéo dương
        for (int r = 0; r < Connect4Model.ROW_COUNT - 3; r++) {
            for (int c = 0; c < Connect4Model.COLUMN_COUNT - 3; c++) {
                if (board[r][c] == piece && board[r + 1][c + 1] == piece && board[r + 2][c + 2] == piece && board[r + 3][c + 3] == piece) {
                    return true;
                }
            }
        }
        // Chéo âm
        for (int c = 0; c < Connect4Model.COLUMN_COUNT - 3; c++) {
            for (int r = 3; r < Connect4Model.ROW_COUNT; r++) { 
                if (board[r][c] == piece && board[r - 1][c + 1] == piece && board[r - 2][c + 2] == piece && board[r - 3][c + 3] == piece) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Integer> getValidLocations(int[][] board) {
        List<Integer> validLocations = new ArrayList<>();
        for (int col = 0; col < Connect4Model.COLUMN_COUNT; col++) {
            if (isValidLocation(board, col)) {
                validLocations.add(col);
            }
        }
        return validLocations;
    }

    public boolean isTerminalNode(int[][] board) {
        return checkWin(board, Connect4Model.PLAYER_PIECE) || checkWin(board, Connect4Model.AI_PIECE) || getValidLocations(board).isEmpty();
    }

    private int evaluateWindow(int[] window, int piece) {
        int score = 0;
        int oppPiece = (piece == Connect4Model.PLAYER_PIECE) ? Connect4Model.AI_PIECE : Connect4Model.PLAYER_PIECE;
        int pieceCount = 0;
        int oppPieceCount = 0;
        int emptyCount = 0;

        for (int p : window) {
            if (p == piece) pieceCount++;
            else if (p == oppPiece) oppPieceCount++;
            else emptyCount++;
        }

        if (pieceCount == 4) score += 1000; 
        else if (pieceCount == 3 && emptyCount == 1) score += 10;
        else if (pieceCount == 2 && emptyCount == 2) score += 2;
        if (oppPieceCount == 3 && emptyCount == 1) score -= 80;
        return score;
    }

    public int scorePosition(int[][] board, int piece) {
        int score = 0;
        int centerCount = 0;
        for (int r = 0; r < Connect4Model.ROW_COUNT; r++) {
            if (board[r][Connect4Model.COLUMN_COUNT / 2] == piece) centerCount++;
        }
        score += centerCount * 3;
        
        for (int r = 0; r < Connect4Model.ROW_COUNT; r++) {
            int[] rowArray = board[r];
            for (int c = 0; c < Connect4Model.COLUMN_COUNT - 3; c++) {
                int[] window = {rowArray[c], rowArray[c + 1], rowArray[c + 2], rowArray[c + 3]};
                score += evaluateWindow(window, piece);
            }
        }
        for (int c = 0; c < Connect4Model.COLUMN_COUNT; c++) {
            for (int r = 0; r < Connect4Model.ROW_COUNT - 3; r++) {
                int[] window = {board[r][c], board[r + 1][c], board[r + 2][c], board[r + 3][c]};
                score += evaluateWindow(window, piece);
            }
        }
        for (int r = 0; r < Connect4Model.ROW_COUNT - 3; r++) {
            for (int c = 0; c < Connect4Model.COLUMN_COUNT - 3; c++) {
                int[] window = {board[r][c], board[r + 1][c + 1], board[r + 2][c + 2], board[r + 3][c + 3]};
                score += evaluateWindow(window, piece);
            }
        }
        for (int r = 3; r < Connect4Model.ROW_COUNT; r++) {
            for (int c = 0; c < Connect4Model.COLUMN_COUNT - 3; c++) {
                int[] window = {board[r][c], board[r - 1][c + 1], board[r - 2][c + 2], board[r - 3][c + 3]};
                score += evaluateWindow(window, piece);
            }
        }
        return score;
    }

    public Object[] getBestMoveAndAllScores(int[][] board, int depth, boolean maximizingPlayer) {
        Map<Integer, Integer> scores = new HashMap<>();
        List<Integer> validLocations = getValidLocations(board);
        if (validLocations.isEmpty()) return new Object[]{-1, scores};
        int bestColumn = validLocations.get(random.nextInt(validLocations.size()));
        int bestValue;

        if (maximizingPlayer) {
            bestValue = Integer.MIN_VALUE;
            for (int col : validLocations) {
                int row = getNextOpenRow(board, col);
                int[][] tempBoard = copyBoard(board);
                dropPiece(tempBoard, row, col, Connect4Model.AI_PIECE);
                int newScore = minimax(tempBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false)[1];
                scores.put(col, newScore);
                if (newScore > bestValue) {
                    bestValue = newScore;
                    bestColumn = col;
                }
            }
        } else { 
            bestValue = Integer.MAX_VALUE;
            for (int col : validLocations) {
                int row = getNextOpenRow(board, col);
                int[][] tempBoard = copyBoard(board);
                dropPiece(tempBoard, row, col, Connect4Model.PLAYER_PIECE); 
                int newScore = minimax(tempBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true)[1];
                scores.put(col, newScore);
                if (newScore < bestValue) { 
                    bestValue = newScore;
                    bestColumn = col;
                }
            }
        }
        return new Object[]{bestColumn, scores};
    }

    public int[] minimax(int[][] board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        List<Integer> validLocations = getValidLocations(board);
        boolean isTerminal = isTerminalNode(board);

        if (depth == 0 || isTerminal) {
            if (isTerminal) {
                if (checkWin(board, Connect4Model.AI_PIECE)) return new int[]{-1, 100000000};
                else if (checkWin(board, Connect4Model.PLAYER_PIECE)) return new int[]{-1, -100000000};
                else return new int[]{-1, 0};
            } else { 
                return new int[]{-1, scorePosition(board, Connect4Model.AI_PIECE)};
            }
        }
        if (maximizingPlayer) { 
            int value = Integer.MIN_VALUE;
            int column = validLocations.get(random.nextInt(validLocations.size())); 
            for (int col : validLocations) {
                int row = getNextOpenRow(board, col);
                int[][] tempBoard = copyBoard(board);
                dropPiece(tempBoard, row, col, Connect4Model.AI_PIECE);
                int newScore = minimax(tempBoard, depth - 1, alpha, beta, false)[1];
                if (newScore > value) {
                    value = newScore;
                    column = col;
                }
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break;
            }
            return new int[]{column, value};
        } else { 
            int value = Integer.MAX_VALUE;
            int column = validLocations.get(random.nextInt(validLocations.size()));
            for (int col : validLocations) {
                int row = getNextOpenRow(board, col);
                int[][] tempBoard = copyBoard(board);
                dropPiece(tempBoard, row, col, Connect4Model.PLAYER_PIECE);
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

    private int[][] copyBoard(int[][] original) {
        int[][] newBoard = new int[Connect4Model.ROW_COUNT][Connect4Model.COLUMN_COUNT];
        for (int r = 0; r < Connect4Model.ROW_COUNT; r++) {
            System.arraycopy(original[r], 0, newBoard[r], 0, Connect4Model.COLUMN_COUNT);
        }
        return newBoard;
    }
}