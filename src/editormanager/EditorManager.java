package editormanager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import java.awt.Graphics;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

import editormanager.MovementMath.*;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Integer.parseInt;

public class EditorManager extends JPanel implements MouseMotionListener, MouseInputListener, MouseWheelListener {
    public final int MIN_SPACE = 10;

    public JFrame frame;
    private MapPanel mapPanel;
    private UIPanel uiPanel;

    public int[][] intMap;
    public BufferedImage[][] imageMap;

    private boolean mouseInPanel = false;
    public Vector2 mousePos;
    public float scrollScale = 1;
    private Vector2 mouseStartPos;
    private Vector2 camStartPos;

    public Camera camera;
    public Vector2 cameraPos;

    private int headerBuffer;
    private float windowWidth;
    private int windowHeight;
    private float windowRatioWH;
    private BufferedImage panelimg;
    public double deltaTime = 0;
    private Instant beginTime = Instant.now();

    public EditorManager(int ww, int wh, int buffer, JFrame frame) {
        this.frame = frame;
        mousePos = new Vector2();

        camera = new Camera();
        cameraPos =camera.campos;

        windowWidth = ww;
        windowHeight = wh;
        headerBuffer = buffer;
        windowRatioWH = ((float)ww)/wh;

        uiPanel = new UIPanel(this);
        mapPanel = new MapPanel(this);

        super.addMouseMotionListener(this);
        super.addMouseWheelListener(this);
        super.addMouseListener(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        windowWidth = frame.getWidth();
        windowHeight = frame.getHeight()-headerBuffer;

        deltaTime = 1000000000/(float)Duration.between(beginTime, Instant.now()).toNanos();
        beginTime = Instant.now();

        if(!mouseInPanel&&mousePos.x>(int)uiPanel.panelRawPos.x) {
            mouseInPanel = true;
        } else if (mouseInPanel&&mousePos.x<(int)uiPanel.panelRawPos.x) {
            mouseInPanel = false;
        }

        //components
        camera.render(g);
        mapPanel.render(g, cameraPos);
        uiPanel.render(g, cameraPos);

        repaint(1);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        cameraPos = new Vector2(camStartPos.x+(mousePos.x-mouseStartPos.x), camStartPos.y+(mousePos.y-mouseStartPos.y));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if(e.getPreciseWheelRotation()!=0) {
            scrollScale = (float)Math.clamp(scrollScale-(long)e.getPreciseWheelRotation()*.05,.25,4);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseStartPos = new Vector2(mousePos.x, mousePos.y);
        camStartPos = new Vector2(cameraPos.x,cameraPos.y);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos.set(e.getPoint().x,e.getPoint().y);
    }

    public class UIPanel extends JPanel {
        private EditorManager parent;
        public Vector2 panelTargetPos;
        public Vector2 panelRawPos;
        public Vector2 panelWH;

        public ArrayList componentList;
        private FileChooser fileChooser;
        private UITextField widthTF;
        private UITextField heightTF;

        public UIPanel(EditorManager p) {
            parent = p;
            panelimg = getAsset("UIPanel.png");

            componentList = new ArrayList<>();

            panelWH = new Vector2((int)((float)windowHeight/panelimg.getHeight()*panelimg.getWidth()), (windowHeight));
            panelTargetPos = new Vector2((windowWidth - (int)(panelWH.x*.25)), 0);
            panelRawPos = new Vector2((windowWidth - (int)(panelWH.x*.25)), 0);

            fileChooser = new FileChooser(this);
            widthTF = new UITextField("3","Width", 2, -20, 80, 0, this);
            heightTF = new UITextField("3","Height", 2, 20, 80, 1, this);

            componentList.add(fileChooser);
            componentList.add(widthTF);
            componentList.add(heightTF);
        }

        public void render(Graphics g, Vector2 cpos) {
            //panel
            parent.mousePos.set(MouseInfo.getPointerInfo().getLocation().x,MouseInfo.getPointerInfo().getLocation().y);
            panelRawPos.set(MovementMath.lerp(panelRawPos.x,panelTargetPos.x,4*(1f/(float)deltaTime)),MovementMath.lerp(panelRawPos.y,panelTargetPos.y,1f));

            float ratio = (float)windowHeight/panelimg.getHeight();
            if(mouseInPanel) panelTargetPos.set((windowWidth - panelWH.x), 0);
            else panelTargetPos.set((windowWidth - (int)(panelWH.x*.25)), 0);
            panelWH.set((int)(ratio*panelimg.getWidth()), (windowHeight));
            g.drawImage(panelimg,(int)(panelRawPos.x),(int)(panelRawPos.y),(int)panelWH.x,(int)panelWH.y,null);

            /*
            for (int i = 0; i < importedImages.size(); i++) {
                g.drawImage(importedImages.get(i), (int)(panel.panelPos.x+ parent.windowRatioWH/2), (int)(panel.panelPos.y+10), null);
            }
            */

            componentList.indexOf(fileChooser);
            fileChooser.render(g, cpos, getInComponentList(componentList, fileChooser));
            widthTF.render(g, cpos, getInComponentList(componentList, widthTF));
            heightTF.render(g, cpos, getInComponentList(componentList, heightTF));
        }

        public int getInComponentList(ArrayList list, Object type) {
            for(int i = 0; i < list.size(); i++) {
                if(list.get(i).equals(type)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public static class FileChooser implements ActionListener {
        private UIPanel panel;
        private JButton button;
        public final int width = 100;
        public final int height = 25;
        private ArrayList<BufferedImage> importedImages;
        private int x = 0;
        private int y = 0;

        public FileChooser(UIPanel p1) {
            panel = p1;

            importedImages = new ArrayList<>();

            button = new JButton("Select File");
            button.addActionListener(this);

            panel.parent.add(button);
        }

        public void render(Graphics g, Vector2 cpos, int index) {
            int addy = panel.parent.MIN_SPACE;
            for (int i = 0; i < index; i++) {
                if(panel.componentList.get(i).getClass().equals(FileChooser.class)) {
                    addy += ((FileChooser) panel.componentList.get(i)).height;
                } else if(panel.componentList.get(i).getClass().equals(UITextField.class)) {
                    addy += ((UITextField) panel.componentList.get(i)).height;
                }
                addy += panel.parent.MIN_SPACE;
            }

            button.setBounds((int)(panel.panelRawPos.x + panel.parent.MIN_SPACE-7), panel.parent.MIN_SPACE + addy-7,width,height);
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
        private String name;
        private int addx;
        private int addy;
        private int type;
        public final int width = 100;
        public final int height = 15;

        public UITextField(String text, String n, int cols, int posx, int posy, int type, UIPanel p) {
            super(text, cols);

            name = n;
            panel = p;
            panel.parent.add(this);

            addx = posx;
            addy = posy;
            this.type = type;

            super.addKeyListener(this);
        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            boolean entered = e.getKeyCode() == KeyEvent.VK_ENTER;
            if (entered) {
                switch (type) {
                    case 0 : {
                        panel.parent.mapPanel.adjustMap(Math.max(parseInt(getText()), 1),panel.parent.intMap[0].length);
                        break;
                    }
                    case 1 : {
                        panel.parent.mapPanel.adjustMap(panel.parent.intMap.length,Math.max(parseInt(getText()), 1));
                        break;
                    }
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}

        public void render(Graphics g, Vector2 cpos, int index) {
            int addy = panel.parent.MIN_SPACE;
            for (int i = 0; i < index; i++) {
                if(panel.componentList.get(i).getClass().equals(FileChooser.class)) {
                    addy += ((FileChooser) panel.componentList.get(i)).height;
                } else if(panel.componentList.get(i).getClass().equals(UITextField.class)) {
                    addy += ((UITextField) panel.componentList.get(i)).height;
                }
                addy += panel.parent.MIN_SPACE;
            }

            g.drawString(name, (int)(panel.panelRawPos.x + panel.parent.MIN_SPACE), panel.parent.MIN_SPACE + addy);
            setBounds((int)(panel.panelRawPos.x + panel.parent.MIN_SPACE + name.length()*7), panel.parent.MIN_SPACE - 17 + addy, super.getWidth(), super.getHeight());
        }
    }
    public static BufferedImage getAsset(String path){
        try {
            return ImageIO.read(new File("assets/"+path));
        } catch (IOException ex) {
            return null;
        }
    }

    public class MapPanel {
        private Vector2 pos;
        private EditorManager parent;
        public MapPanel(EditorManager parent) {
            mouseStartPos = new Vector2();
            camStartPos = new Vector2();

            pos = new Vector2();
            this.parent = parent;
            intMap = new int[3][3];
            imageMap = new BufferedImage[3][3];
        }
        public void adjustMap(int rows, int cols) {
            intMap = new int[rows][cols];
            imageMap = new BufferedImage[rows][cols];
        }
        public void render(Graphics g, Vector2 cpos) {
            for (int y = 0; y < intMap[0].length; y++) {
                for (int x = 0; x < intMap.length; x++) {
                    g.setColor(Color.WHITE);
                    g.drawRect((int)(pos.x+x*64*scrollScale+cpos.x),(int)(pos.y+y*64*scrollScale+cpos.y),(int)(64*scrollScale),(int)(64*scrollScale));
                    g.setColor(Color.BLACK);
                }
            }
        }
    }

    public class Camera {
        public Vector2 campos;
        public Camera() {
            campos = new Vector2(0,0);
        }
        public void render(Graphics g) {
            //g.translate((int)-campos.x,(int)-campos.y);
        }
    }
}