package editormanager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Vector;
import editormanager.MovementMath.*;

public class UIManager extends JPanel implements MouseMotionListener{
    private UIPanel uiPanel;

    private boolean mouseInPanel = false;
    public Vector2 mousePos;

    private float windowWidth;
    private int windowHeight;
    private float windowRatioWH;
    private BufferedImage panelimg;
    public double deltaTime = 0;
    private Instant beginTime = Instant.now();

    public UIManager(int ww, int wh) {
        mousePos = new Vector2();

        windowWidth = ww;
        windowHeight = wh;
        windowRatioWH = ((float)ww)/wh;

        uiPanel = new UIPanel(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);

        deltaTime = 1000000000/(float)Duration.between(beginTime, Instant.now()).toNanos();
        beginTime = Instant.now();

        if(!mouseInPanel&&mousePos.x>(int)uiPanel.panelRawPos.x) {
            mouseInPanel = true;
        } else if (mouseInPanel&&mousePos.x<(int)uiPanel.panelRawPos.x) {
            mouseInPanel = false;
        }

        //components
        uiPanel.render(g);

        repaint(1);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos.set(e.getPoint().x,e.getPoint().y);
    }

    public class UIPanel extends JPanel {
        private UIManager parent;
        public Vector2 panelTargetPos;
        public Vector2 panelRawPos;
        public Vector2 panelWH;

        private FileChooser fileChooser;
        private UITextField widthTF;
        private UITextField heightTF;

        public UIPanel(UIManager p) {
            parent = p;

            panelTargetPos = new Vector2((windowWidth*.95f), 0);
            panelRawPos = new Vector2((windowWidth*.95f), 0);
            panelWH = new Vector2(0,0);

            panelimg = getAsset("UIPanel.png");

            fileChooser = new FileChooser(this);
            widthTF = new UITextField("3",2, -20, 80, this);
            heightTF = new UITextField("3",2, 20, 80, this);
        }

        public void render(Graphics g) {
            //panel
            parent.mousePos.set(MouseInfo.getPointerInfo().getLocation().x,MouseInfo.getPointerInfo().getLocation().y);
            panelRawPos.set(MovementMath.lerp(panelRawPos.x,panelTargetPos.x,4*(1f/(float)deltaTime)),MovementMath.lerp(panelRawPos.y,panelTargetPos.y,1f));

            float ratio = (float)windowHeight/panelimg.getHeight();
            if(mouseInPanel) panelTargetPos.set((windowWidth*.75f), 0);
            else panelTargetPos.set((windowWidth*.95f), 0);
            panelWH.set((int)(ratio*panelimg.getWidth()), (windowHeight));
            g.drawImage(panelimg,(int)panelRawPos.x,(int)panelRawPos.y,(int)panelWH.x,(int)panelWH.y,null);

            /*
            for (int i = 0; i < importedImages.size(); i++) {
                g.drawImage(importedImages.get(i), (int)(panel.panelPos.x+ parent.windowRatioWH/2), (int)(panel.panelPos.y+10), null);
            }
            */

            fileChooser.render(g);
            widthTF.render(g);
            heightTF.render(g);
        }
    }
    public static class FileChooser implements ActionListener {
        private UIPanel panel;
        private JButton button;
        private final int w = 100;
        private final int h = 25;
        private ArrayList<BufferedImage> importedImages;

        public FileChooser(UIPanel p1) {
            panel = p1;

            importedImages = new ArrayList<>();

            button = new JButton("Select File");
            button.addActionListener(this);

            panel.parent.add(button);
        }

        public void render(Graphics g) {
            button.setBounds((int)(panel.panelRawPos.x + panel.panelWH.x/2-w/2), (int)(panel.panelRawPos.y+20-h/2),w,h);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == button) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));

                int response = fileChooser.showOpenDialog(null); //select file to open

                if (response == JFileChooser.APPROVE_OPTION) {
                    File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                    try {
                        importedImages.add(ImageIO.read(file));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }
    public static class UITextField extends JTextField implements KeyListener {
        private UIPanel panel;
        public String lastText = "";
        private int addx;
        private int addy;
        public UITextField(String text, int cols, int posx, int posy, UIPanel p) {
            super(text, cols);
            panel = p;
            panel.parent.add(this);

            addx = posx;
            addy = posy;
        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            Boolean entered = e.getKeyCode() == KeyEvent.VK_ENTER;
            if (entered) {
                lastText = getText();
                System.out.println(lastText);
                setText("  ");
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}

        public void render(Graphics g) {
            setBounds((int)(panel.panelRawPos.x + addx + panel.panelWH.x/2-super.getWidth()/2), (int)(panel.panelRawPos.y + addy -super.getHeight()/2),super.getWidth(),super.getHeight());
        }
    }
    public static BufferedImage getAsset(String path){
        try {
            return ImageIO.read(new File("assets/"+path));
        } catch (IOException ex) {
            return null;
        }
    }
}