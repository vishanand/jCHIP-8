import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

final public class main {

    public static void main(String[] args) {

        System.out.println("a Chip-8 Emulator for Java");
        System.out.println("Vish Anand 2018\n");

        int midiNote = 80;

        chip8 interpreter = new chip8();
        if (!interpreter.loadProgram("ROM/BRIX")) {
            System.out.println("Terminating!");
            return;
        }

        // setup drawing canvas
        JPanel canvas = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
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

        // action listener
        class KeypadListener implements MouseListener {

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent e) {
                //System.out.println(e.getComponent().getName());

                if (e.getComponent().getName().charAt(0) == 'b') {
                    int buttonNum = Integer
                            .parseInt(e.getComponent().getName().substring(1));
                    interpreter.pressKey(buttonNum);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                interpreter.pressKey(-1);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }
        }
        KeypadListener listener = new KeypadListener();

        // setup input keypad
        JPanel keyPane = new JPanel();
        keyPane.setLayout(new GridLayout(4, 4));
        JButton keys[] = new JButton[16];
        for (int i = 0; i <= 0xF; i++) {
            keys[i] = new JButton(Integer.toHexString(i).toUpperCase());
            keys[i].setName("b" + i);
            keys[i].addMouseListener(listener);
        }
        keyPane.add(keys[1]);
        keyPane.add(keys[2]);
        keyPane.add(keys[3]);
        keyPane.add(keys[0xC]);
        keyPane.add(keys[4]);
        keyPane.add(keys[5]);
        keyPane.add(keys[6]);
        keyPane.add(keys[0xD]);
        keyPane.add(keys[7]);
        keyPane.add(keys[8]);
        keyPane.add(keys[9]);
        keyPane.add(keys[0xE]);
        keyPane.add(keys[0xA]);
        keyPane.add(keys[0]);
        keyPane.add(keys[0xB]);
        keyPane.add(keys[0xF]);

        // setup main window
        JPanel topPane = new JPanel();
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
        topPane.add(canvas);
        topPane.add(keyPane);
        JFrame mainWin = new JFrame();
        mainWin.add(topPane);
        canvas.repaint();
        mainWin.setTitle("jCHIP-8");
        mainWin.setSize(640, 800);
        mainWin.setResizable(false);
        mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWin.setVisible(true);

        // setup audio
        boolean soundAvail = true;
        boolean soundPlaying = false;
        Synthesizer synth;
        MidiChannel channel0 = null;
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            synth.loadInstrument(
                    synth.getDefaultSoundbank().getInstruments()[0]);
            channel0 = synth.getChannels()[0];
        } catch (MidiUnavailableException e1) {
            System.out.println("MIDI sound not available!");
            soundAvail = false;
        }

        // game loop
        for (int i = 0;; i++) {
            interpreter.runCycle();
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (interpreter.shouldWeRedraw()) {
                canvas.repaint();
            }

            // play sounds
            if (interpreter.playSound()) {
                if (soundAvail && !soundPlaying) {
                    channel0.noteOn(midiNote, 100);
                    soundPlaying = true;
                } else if (!soundPlaying) {
                    Toolkit.getDefaultToolkit().beep(); // use OS beeping noise
                }
            } else if (soundPlaying) {
                channel0.noteOff(midiNote);
                soundPlaying = false;
            }
        }
    }
}
