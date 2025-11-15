package view; // Thêm dòng này

import javax.swing.*;
import java.awt.*;

// Import các lớp từ các package khác
import model.Connect4Model;
import controller.Connect4Controller;
// GamePanel và MenuPanel không cần import vì chúng CÙNG package 'view'

/**
 * Lớp View chính, chứa hàm main
 */
public class Connect4View {
    
    // Hằng số đồ họa
    public static final int SQUARE_SIZE = 100;
    public static final int SCREEN_WIDTH = Connect4Model.COLUMN_COUNT * SQUARE_SIZE;// kích thước màng hình
    public static final int SCREEN_HEIGHT = (Connect4Model.ROW_COUNT + 1) * SQUARE_SIZE;// kích thước màng hình
    
    public static final Color COLOR_BLUE = new Color(0, 100, 200);
    public static final Color COLOR_BLACK = Color.BLACK;
    public static final Color COLOR_RED = new Color(200, 0, 0);
    public static final Color COLOR_YELLOW = new Color(200, 200, 0);
    public static final Color COLOR_WHITE = Color.WHITE;

    private final JFrame frame;
    private final Connect4Controller controller;
    
    public Connect4View() {
        frame = new JFrame("Connect 4 AI (Java Swing) - MVC");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // nhấn nút x để đóng
        frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT + 37); // kích thước của sổ
        frame.setLocationRelativeTo(null); // tự động xuất hiện ở giữa
        frame.setResizable(false);
        
        this.controller = new Connect4Controller(this);
        
        showMenu();
        
        frame.setVisible(true);
    }

    public void showMenu() {
        frame.getContentPane().removeAll();// xóa nội dung người dùng vừa chơi xong
        frame.add(new MenuPanel(controller)); // Cùng package 'view'
        frame.revalidate();
        frame.repaint();
    }

    public void showGame(Connect4Model model, Connect4Controller controller) {
        frame.getContentPane().removeAll();
        GamePanel gamePanel = new GamePanel(model, controller); // Tạo một GamePanel để controller ba người dung click vào đâu và model vẽ bàn cờ xử lý
        frame.add(gamePanel);
        
        controller.gamePanel = gamePanel; 
        
        frame.revalidate();
        frame.repaint();
    }
    
    public static void main(String[] args) {
        // Chạy GUI trên luồng sự kiện
        SwingUtilities.invokeLater(Connect4View::new);
    }
}