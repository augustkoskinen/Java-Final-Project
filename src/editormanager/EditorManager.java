package editormanager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;
import java.awt.Graphics;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import editormanager.MovementMath.*;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Integer.parseInt;

public class EditorManager extends JPanel implements MouseMotionListener, MouseInputListener, MouseWheelListener {
    //consts
    public final int MIN_SPACE = 10;
    public final int HEADER_H = 28;

    //components
    public JFrame frame;
    private MapPanel mapPanel;
    private UIPanel uiPanel;
    private ColorPanel colorPanel;

    public int[][] intMap;
    public ImageSquare[][] imageMap;

    public static ArrayList<ImageSquare> imageSquareList;
    public ImageSquare currentIS;

    //mouse vars
    public boolean mouseDown = false;
    private boolean mouseInPanel = false;
    public Vector2 mousePos;
    public float scrollScale = 1;
    private Vector2 mouseStartPos;
    private Vector2 camStartPos;

    //cam vars
    public Camera camera;
    public Vector2 cameraPos;

    private int headerBuffer;
    private float windowWidth;
    private int windowHeight;
    private BufferedImage panelimg;
    public double deltaTime = 0;
    private Instant beginTime = Instant.now();

    public EditorManager(int ww, int wh, int buffer, JFrame frame) {
        this.frame = frame;
        mousePos = new Vector2();

        imageSquareList = new ArrayList<>();

        //errase png
        imageSquareList.add(new ImageSquare(getAsset("erase.png")));

        camera = new Camera();
        cameraPos = camera.campos;

        windowWidth = ww;
        windowHeight = wh;
        headerBuffer = buffer;

        //components
        uiPanel = new UIPanel(this);
        mapPanel = new MapPanel(this);
        colorPanel = new ColorPanel(this);

        super.addMouseMotionListener(this);
        super.addMouseWheelListener(this);
        super.addMouseListener(this);
    }

    //rendering components
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        windowWidth = frame.getWidth();
        windowHeight = frame.getHeight()-headerBuffer;

        //deltatime
        deltaTime = 1000000000/(float)Duration.between(beginTime, Instant.now()).toNanos();
        beginTime = Instant.now();

        //check if mouse is in panel
        if(!mouseInPanel&&mousePos.x>(int)uiPanel.panelRawPos.x) {
            mouseInPanel = true;
        } else if (mouseInPanel&&mousePos.x<(int)uiPanel.panelRawPos.x) {
            mouseInPanel = false;
        }

        //component drawing
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        camera.render(g);
        mapPanel.render(g, cameraPos);
        uiPanel.render(g, cameraPos);
        colorPanel.render(g, cameraPos);

        //check if painting to map
        if(mouseDown&&!mouseInPanel) {
            int mposx = (int)(((mousePos.x - cameraPos.x))/(64*scrollScale));
            int mposy = (int)(((mousePos.y - cameraPos.y - 64))/(64*scrollScale));
            if(mposx>-1&&mposy>-1&&mposx<intMap.length&&mposy<intMap[mposx].length) {
                intMap[mposx][mposy] = imageSquareList.indexOf(currentIS);
                if(imageSquareList.indexOf(currentIS)==0) //errase case
                    imageMap[mposx][mposy] = null;
                else
                    imageMap[mposx][mposy] = currentIS;
            }
        }
        //repaint every millis
        repaint(1);
    }

    //pan func
    @Override
    public void mouseDragged(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON3)
            cameraPos = new Vector2(camStartPos.x+(mousePos.x-mouseStartPos.x), camStartPos.y+(mousePos.y-mouseStartPos.y));
    }

    //scroll func
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if(e.getPreciseWheelRotation()!=0) {
            final double wheelconst = .15;
            scrollScale = (float)Math.clamp(scrollScale-(long)e.getPreciseWheelRotation()*wheelconst,.25,4);
        }
    }

    //draw func
    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON3) {
            mouseStartPos = new Vector2(mousePos.x, mousePos.y);
            camStartPos = new Vector2(cameraPos.x, cameraPos.y);
        } else if(e.getButton()==MouseEvent.BUTTON1) {
            mouseDown = true;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    //release mouse
    @Override
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
    }

    //update mouse pos
    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos.set(e.getPoint().x,e.getPoint().y);
    }

    //panel for color selection
    public class ColorPanel extends JPanel {
        private EditorManager parent;
        public Vector2 panelRawPos = new Vector2();
        public Vector2 panelWH = new Vector2();
        public int chosen = 0;

        public ColorPanel(EditorManager p) {
            parent = p;
        }

        public void render(Graphics g, Vector2 cameraPos) {
            //draw each color

            for(int i = 0; i < imageSquareList.size(); i++) {
                if(chosen == i)
                    g.setColor(Color.WHITE);
                else
                    g.setColor(Color.GRAY);
                g.drawRect(10,20+80*i,64,64);
                if(imageSquareList.get(i).type==0) {
                    g.setColor(new Color(imageSquareList.get(i).r,imageSquareList.get(i).g,imageSquareList.get(i).b));
                    g.fillRect(11, 21+80*i, 62, 62);
                } else {
                    g.drawImage(imageSquareList.get(i).image,11, 21+80*i, 62, 62,null);
                }

                if(mousePos.x-parent.frame.getLocationOnScreen().x>10&&mousePos.x-parent.frame.getLocationOnScreen().x<74&&mousePos.y-parent.frame.getLocationOnScreen().y-HEADER_H>20+80*i&&mousePos.y-parent.frame.getLocationOnScreen().y-HEADER_H<84+80*i&&mouseDown) {
                    mouseDown = false;
                    currentIS = imageSquareList.get(i);
                    chosen = i;
                }
            }
        }
    }

    //class for color/images to draw
    public class ImageSquare {
        public int r = 0;
        public int g = 0;
        public int b = 0;
        public BufferedImage image = null;
        public int type = -1;

        public ImageSquare(int r, int g, int b) {
            type = 0;
            this.r = r;
            this.g = g;
            this.b = b;
        }
        public ImageSquare(BufferedImage image) {
            type = 1;
            this.image = image;
        }
    }

    //class for ui panel + buttons
    public class UIPanel extends JPanel {
        private EditorManager parent;
        public Vector2 panelTargetPos;
        public Vector2 panelRawPos;
        public Vector2 panelWH;

        //buttons
        public ArrayList<UIComponent> componentList;
        public ArrayList<UIComponentTextField> TFcomponentList;
        private FileChooser fileChooser;
        private UIComponentColorPicker colorChooser;
        private ExportButton exportButton;
        private UIComponentTextField widthTF;
        private UIComponentTextField heightTF;
        public ClearMap clearMapButton;

        public UIPanel(EditorManager p) {
            parent = p;
            panelimg = getAsset("UIPanel.png");

            componentList = new ArrayList<>();
            TFcomponentList = new ArrayList<>();

            panelWH = new Vector2((int)((float)windowHeight/panelimg.getHeight()*panelimg.getWidth()), (windowHeight));
            panelTargetPos = new Vector2((windowWidth - (int)(panelWH.x*.25)), 0);
            panelRawPos = new Vector2((windowWidth - (int)(panelWH.x*.25)), 0);

            //create components
            fileChooser = new FileChooser(this);
            colorChooser = new UIComponentColorPicker(this);
            exportButton = new ExportButton(this);
            clearMapButton = new ClearMap(this);
            widthTF = new UIComponentTextField("3","Width", 2, -20, 80, 0, this);
            heightTF = new UIComponentTextField("3","Height", 2, 20, 80, 1, this);

            //add componenets to be drawn
            componentList.add(exportButton);
            componentList.add(fileChooser);
            componentList.add(colorChooser);
            componentList.add(clearMapButton);
            TFcomponentList.add(widthTF);
            TFcomponentList.add(heightTF);
        }

        //draw components
        public void render(Graphics g, Vector2 cpos) {
            parent.mousePos.set(MouseInfo.getPointerInfo().getLocation().x,MouseInfo.getPointerInfo().getLocation().y);
            panelRawPos.set(MovementMath.lerp(panelRawPos.x,panelTargetPos.x,4*(1f/(float)deltaTime)),MovementMath.lerp(panelRawPos.y,panelTargetPos.y,1f));

            //draw panel
            float ratio = (float)windowHeight/panelimg.getHeight();
            if(mouseInPanel) panelTargetPos.set((windowWidth - panelWH.x), 0);
            else panelTargetPos.set((windowWidth - (int)(panelWH.x*.25)), 0);
            panelWH.set((int)(ratio*panelimg.getWidth()), (windowHeight));
            g.drawImage(panelimg,(int)(panelRawPos.x),(int)(panelRawPos.y),(int)panelWH.x,(int)panelWH.y,null);

            //draw componenets
            fileChooser.render(g, cpos, componentList.indexOf(fileChooser));
            colorChooser.render(g, cpos, componentList.indexOf(colorChooser));
            exportButton.render(g, cpos, componentList.indexOf(exportButton));
            clearMapButton.render(g, cpos, componentList.indexOf(clearMapButton));
            widthTF.render(g, cpos, TFcomponentList.indexOf(widthTF));
            heightTF.render(g, cpos, TFcomponentList.indexOf(heightTF));
        }
    }

    //general class for ui components
    public static class UIComponent {
        public int x;
        public int y;
        public int width;
        public int height;
        public String name;

        public UIComponent() {
            x = 0;
            y = 0;
            width = 0;
            height = 0;
            name = "";
        }

        public UIComponent(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    //class for file chooser button
    public class FileChooser extends UIComponent implements ActionListener {
        private UIPanel panel;
        private JButton button;
        private ArrayList<BufferedImage> importedImages;

        public FileChooser(UIPanel p1) {
            super("Select File",0,0,100,25);
            panel = p1;

            importedImages = new ArrayList<>();

            button = new JButton("Select File");
            button.addActionListener(this);

            panel.parent.add(button);
        }

        //draw button
        public void render(Graphics g, Vector2 cpos, int index) {
            int addy = panel.parent.MIN_SPACE;
            for (int i = 0; i < index; i++) {
                addy += panel.componentList.get(i).height + panel.parent.MIN_SPACE;
            }

            button.setBounds((int)(panel.panelRawPos.x + panel.parent.MIN_SPACE-7), panel.parent.MIN_SPACE + addy-7,width,height);
        }

        //file selection
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == button) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));

                int response = fileChooser.showOpenDialog(null); //select file to open

                //add file to image list
                if (response == JFileChooser.APPROVE_OPTION) {
                    File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                    try {
                        importedImages.add(ImageIO.read(file));
                        panel.parent.imageSquareList.add(new ImageSquare(importedImages.getLast()));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    //class for a map clearing button
    public class ClearMap extends UIComponent {
        private UIPanel panel;
        private JButton button;

        public ClearMap(UIPanel p1) {
            super("Clear Map",0,0,100,25);
            panel = p1;

            button = new JButton("Clear Map");

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    intMap = new int[intMap.length][intMap[0].length];
                    imageMap = new ImageSquare[intMap.length][intMap[0].length];
                }
            });

            panel.parent.add(button);
        }

        //draw the button
        public void render(Graphics g, Vector2 cpos, int index) {
            int addy = panel.parent.MIN_SPACE;
            for (int i = 0; i < index; i++) {
                addy += panel.componentList.get(i).height + panel.parent.MIN_SPACE;
            }

            button.setBounds((int)(panel.panelRawPos.x + panel.parent.MIN_SPACE-7), panel.parent.MIN_SPACE + addy-7,width,height);
        }
    }

    //class for text field
    public static class UIComponentTextField extends JTextField implements KeyListener {
        private UIPanel panel;
        private String name;
        private int addx;
        private int addy;
        private int type;
        public final int width = 100;
        public final int height = 15;

        public UIComponentTextField(String text, String n, int cols, int posx, int posy, int type, UIPanel p) {
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

        //check to adjust map size
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

        //draw component
        public void render(Graphics g, Vector2 cpos, int index) {
            int addy = panel.parent.MIN_SPACE+140;
            for (int i = 0; i < index; i++) {
                addy += panel.TFcomponentList.get(i).height+panel.parent.MIN_SPACE;
            }

            g.drawString(name, (int)(panel.panelRawPos.x + panel.parent.MIN_SPACE), panel.parent.MIN_SPACE + addy);
            setBounds((int)(panel.panelRawPos.x + panel.parent.MIN_SPACE + name.length()*7), panel.parent.MIN_SPACE - 17 + addy, super.getWidth(), super.getHeight());
        }
    }

    //button for exporting map
    public class ExportButton extends UIComponent {
        private UIPanel panel;
        private Button button;

        public ExportButton(UIPanel p) {
            super("Export",0,0,100,25);
            button = new Button("Export");
            panel = p;
            panel.parent.add(button);
        }

        //draw component
        public void render(Graphics g, Vector2 cpos, int index) {
            int addy = panel.parent.MIN_SPACE;
            for (int i = 0; i < index; i++) {
                addy += panel.componentList.get(i).height + panel.parent.MIN_SPACE;
            }
            button.setBounds((int)(panel.panelRawPos.x + panel.parent.MIN_SPACE-7), panel.parent.MIN_SPACE + addy-7,width,height);
        }

        //button
        private class Button extends JButton {
            public Button(String text) {
                super(text);
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        //copy map to clipboard
                        String text = "";
                        text += "{\n";
                        for(int y = 0; y < panel.parent.intMap.length; y++) {
                            text += "{";
                            for (int x = 0; x < panel.parent.intMap[0].length; x++) {
                                text += panel.parent.intMap[x][y] + (x>=panel.parent.intMap[0].length-1 ? "" : ", ");
                            }
                            text += "}"+(y>=panel.parent.intMap.length-1 ? "" : ",")+"\n";
                        }
                        text += "}";

                        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(text), null);
                    }
                });
            }
        }
    }

    //class for picking a color
    public class UIComponentColorPicker extends UIComponent {
        public UIPanel panel;
        public ColorPickerButton button1;
        public RemoveButton button2;

        public UIComponentColorPicker(UIPanel panel) {
            super("Add Color",0,0,100,25);
            this.panel = panel;
            button1 = new ColorPickerButton("Add Color", Color.black);
            button2 = new RemoveButton("Remove Color");

            panel.parent.add(button1);
            panel.parent.add(button2);
        }

        //draw component
        public void render(Graphics g, Vector2 cpos, int index) {
            int addy = panel.parent.MIN_SPACE;
            for (int i = 0; i < index; i++) {
                addy += panel.componentList.get(i).height + panel.parent.MIN_SPACE;
            }
            button1.setBounds((int)(panel.panelRawPos.x + panel.parent.MIN_SPACE-7), panel.parent.MIN_SPACE + addy-7,width,height);
            button2.setBounds((int)(panel.panelRawPos.x + 88 + panel.parent.MIN_SPACE-7), panel.parent.MIN_SPACE + addy-7,112,height);
        }

        //button to choose a color
        public class ColorPickerButton extends JButton {
            private Color current;
            private JColorChooser colorChooser;
            private JDialog dialog;

            public ColorPickerButton(String text, Color c) {
                super(text);

                setSelectedColor(c);
                colorChooser = new JColorChooser(Color.black);
                colorChooser.setPreviewPanel(new JPanel());
                dialog = JColorChooser.createDialog(panel.parent, "Choose a color", true, colorChooser, new ActionListener() {
                    //choose ok case
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //add+save color if it satisfies conditions
                        setSelectedColor(colorChooser.getColor());

                        boolean canAddColor = true;
                        for(int i = 0; i < panel.parent.imageSquareList.size(); i++)
                            if(panel.parent.imageSquareList.get(i).type==0&&
                                panel.parent.imageSquareList.get(i).r==current.getRed()&&
                                panel.parent.imageSquareList.get(i).g==current.getGreen()&&
                                panel.parent.imageSquareList.get(i).b==current.getBlue()
                            )
                                canAddColor = false;

                        if(panel.parent.imageSquareList.size() < 6&&canAddColor)
                            imageSquareList.add(new ImageSquare(getSelectedColor().getRed(),getSelectedColor().getGreen(),getSelectedColor().getBlue()));
                        dialog.setVisible(false);
                    }
                }, new ActionListener() {
                    //choose no case
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //save color
                        setSelectedColor(colorChooser.getColor());
                        dialog.setVisible(false);
                    }
                });

                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        dialog.setVisible(true);
                    }
                });
            }

            public Color getSelectedColor() {
                return current;
            }

            public void setSelectedColor(Color newColor) {
                setSelectedColor(newColor, true);
            }

            public void setSelectedColor(Color newColor, boolean notify) {
                if (newColor == null) return;

                current = newColor;
            }
        }

        //button to remove a color
        public class RemoveButton extends JButton {
            public RemoveButton(String text) {
                super(text);

                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        if(panel.parent.imageSquareList.size() > 1)
                            panel.parent.imageSquareList.removeLast();
                    }
                });
            }
        }
    }

    //get an image from a path
    public static BufferedImage getAsset(String path){
        try {
            return ImageIO.read(new File("assets/"+path));
        } catch (IOException ex) {
            return null;
        }
    }

    //class for the map
    public class MapPanel {
        private Vector2 pos;
        private EditorManager parent;

        public MapPanel(EditorManager parent) {
            mouseStartPos = new Vector2();
            camStartPos = new Vector2();

            pos = new Vector2();
            this.parent = parent;
            intMap = new int[3][3];
            imageMap = new ImageSquare[3][3];
        }

        //adjust size
        public void adjustMap(int rows, int cols) {
            intMap = new int[rows][cols];
            imageMap = new ImageSquare[rows][cols];
        }

        //draw map lines + colors/images
        public void render(Graphics g, Vector2 cpos) {
            for (int y = 0; y < imageMap[0].length; y++) {
                for (int x = 0; x < imageMap.length; x++) {
                    g.setColor(Color.WHITE);
                    if(imageMap[x][y]!=null) {
                        if(imageMap[x][y].type==0) {
                            g.setColor(new Color(imageMap[x][y].r, imageMap[x][y].g, imageMap[x][y].b));
                            g.fillRect((int) (pos.x + x * 64 * scrollScale + cpos.x), (int) (pos.y + y * 64 * scrollScale + cpos.y), (int) (64 * scrollScale), (int) (64 * scrollScale));
                        } else if(imageMap[x][y].type==1) {
                            g.drawImage(imageMap[x][y].image,(int) (pos.x + x * 64 * scrollScale + cpos.x), (int) (pos.y + y * 64 * scrollScale + cpos.y), (int) (64 * scrollScale), (int) (64 * scrollScale),null);
                        }
                        g.setColor(Color.BLACK);
                        g.drawRect((int)(pos.x+x*64*scrollScale+cpos.x),(int)(pos.y+y*64*scrollScale+cpos.y),(int)(64*scrollScale),(int)(64*scrollScale));
                    } else {
                        g.drawRect((int)(pos.x+x*64*scrollScale+cpos.x),(int)(pos.y+y*64*scrollScale+cpos.y),(int)(64*scrollScale),(int)(64*scrollScale));
                    }
                    g.setColor(Color.BLACK);
                }
            }
        }
    }

    //general class for the camera
    public class Camera {
        public Vector2 campos;
        public Camera() {
            campos = new Vector2(0,0);
        }
        public void render(Graphics g) {}
    }
}