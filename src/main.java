import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class main {

    public static void main(String[] args) {
        System.out.println("a Chip-8 Emulator for Java");
        System.out.println("Vish Anand 2018\n");

        chip8 interpreter = new chip8();
        if (!interpreter.loadProgram("IBM Logo.ch8")) {
            System.out.println("Terminating!");
            return;
        }

        JFrame data = new JFrame();

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (int i = 0; i < 64; i++) {
                    for (int j = 0; j < 32; j++) {
                        g.setColor(Color.BLACK);
                        if (interpreter.display.getPixel(i, j)) {
                            g.setColor(Color.WHITE);
                        }
                        g.fillRect(i * 10, j * 10, 10, 10);
                    }
                }
            }
        };
        data.add(canvas);
        canvas.repaint();
        data.setTitle("System Memory");
        data.setSize(640, 320);
        data.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        data.setVisible(true);

        for (int i = 0; i < 100; i++) {
            interpreter.runCycle();
            if (interpreter.shouldWeRedraw()) {
                canvas.repaint();
            }
        }
    }

}
