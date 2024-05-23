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

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Computer Science Final");
        Container container = frame.getContentPane();

        frame.setSize(800,550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        container.add(new FileChooser());

        frame.setVisible(true);
    }

    public static class FileChooser extends JPanel implements ActionListener{
        private JButton button;
        private ArrayList<BufferedImage> importedImages;
        public FileChooser() {
            importedImages = new ArrayList<>();

            button = new JButton("Select File");
            button.addActionListener(this);

            add(button);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < importedImages.size(); i++) {
                g.drawImage(importedImages.get(i), 0, 0, null);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource()==button) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));

                int response = fileChooser.showOpenDialog(null); //select file to open

                if(response == JFileChooser.APPROVE_OPTION) {
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
}