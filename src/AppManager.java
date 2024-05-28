import java.awt.*;
import javax.swing.JFrame;
import editormanager.EditorManager;

public class AppManager {
    public static final int WINDOW_W = 800;
    public static final int WINDOW_H = 550;
    public static final int HEADER_H = 28;
    public static JFrame frame;
    public static void main(String[] args) {
        frame = new JFrame("Computer Science Final");
        Container container = frame.getContentPane();

        frame.setSize(WINDOW_W,WINDOW_H+HEADER_H);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EditorManager uiManager = new EditorManager(WINDOW_W, WINDOW_H, HEADER_H, frame);

        container.add(uiManager);
        frame.setVisible(true);
    }
}