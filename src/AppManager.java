import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ArrayList;

import editormanager.UIManager;
import editormanager.UIManager.*;

public class AppManager {
    public static final int WINDOW_W = 800;
    public static final int WINDOW_H = 550;
    public static final int HEADER_H = 28;
    public static JFrame frame;
    public static void main(String[] args) {
        frame = new JFrame("Computer Science Final");
        UIManager uiManager = new UIManager(WINDOW_W, WINDOW_H);
        Container container = frame.getContentPane();

        frame.setSize(WINDOW_W,WINDOW_H+HEADER_H);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        container.add(uiManager);

        frame.setVisible(true);
    }
}