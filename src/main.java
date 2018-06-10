import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

final public class main {

    public static void main(String[] args) {
        main emulation = new main();
        emulation.run();
    }

    public void run() {

        System.out.println("a Chip-8 Emulator for Java");
        System.out.println("Vish Anand 2018\n");

        this.mainWin = new JFrame();
        JFileChooser filePicker = new JFileChooser();
        filePicker.setCurrentDirectory(new File("./ROM/"));

        int result = filePicker.showOpenDialog(this.mainWin);

        this.interpreter = new chip8();
        if (result != JFileChooser.APPROVE_OPTION || !this.interpreter
                .loadProgram(filePicker.getSelectedFile().toString())) {
            System.out.println("Terminating!");
            return;
        }
        this.mainWin.setTitle(
                filePicker.getSelectedFile().getName() + " - jCHIP-8");

        this.setupGUI();
        this.setupKeypad();
        this.setupAudio();

        // game loop
        this.delay = 2;
        for (int i = 0;; i++) {
            this.interpreter.runCycle();
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.interpreter.shouldWeRedraw()) {
                this.canvas.repaint();
            }

            this.playAudio();
            this.cpuInfo.setText(this.interpreter.getCPUInfo());
        }
    }

    private static int convertKey(char key) {
        key = Character.toLowerCase(key);
        if (key == '1') {
            return 1;
        } else if (key == '2') {
            return 2;
        } else if (key == '3') {
            return 3;
        } else if (key == '4') {
            return 0xC;
        } else if (key == 'q') {
            return 4;
        } else if (key == 'w') {
            return 5;
        } else if (key == 'e') {
            return 6;
        } else if (key == 'r') {
            return 0xD;
        } else if (key == 'a') {
            return 7;
        } else if (key == 's') {
            return 8;
        } else if (key == 'd') {
            return 9;
        } else if (key == 'f') {
            return 0xE;
        } else if (key == 'z') {
            return 0xA;
        } else if (key == 'x') {
            return 0;
        } else if (key == 'c') {
            return 0xB;
        } else if (key == 'v') {
            return 0xF;
        } else {
            return -1;
        }
    }

    private void setupGUI() {
        // setup drawing canvas
        this.canvas = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (int i = 0; i < 64; i++) {
                    for (int j = 0; j < 32; j++) {
                        g.setColor(Color.BLACK);
                        if (main.this.interpreter.display.getPixel(i, j)) {
                            g.setColor(Color.WHITE);
                        }
                        g.fillRect(i * 10, j * 10, 10, 10);
                    }
                }
            }
        };
        this.canvas.setSize(640, 320);

        // setup cpu display
        this.cpuInfo = new JLabel();
        this.cpuInfo.setSize(640, 40);
        this.cpuInfo.setHorizontalTextPosition(SwingConstants.LEFT);

        // setup main window
        this.topPane = new JPanel();
        this.topPane
                .setLayout(new BoxLayout(this.topPane, BoxLayout.PAGE_AXIS));
        this.topPane.add(this.canvas);
        this.topPane.add(this.cpuInfo);
        this.mainWin.add(this.topPane);
        this.canvas.repaint();
        this.mainWin.setSize(640, 380);
        this.mainWin.setResizable(false);
        this.mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainWin.setVisible(true);
    }

    private void setupKeypad() {
        // keyboard input
        Action keyPressed = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                int buttonNum = convertKey(e.getActionCommand().charAt(0));
                main.this.interpreter.pressKey(buttonNum);
            }
        };
        Action keyReleased = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                int buttonNum = convertKey(e.getActionCommand().charAt(0));
                main.this.interpreter.releaseKey(buttonNum);
            }
        };
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed 1"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released 1"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed 2"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released 2"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed 3"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released 3"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed 4"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released 4"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed Q"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released Q"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed W"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released W"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed E"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released E"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed R"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released R"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed A"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released A"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed S"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released S"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed D"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released D"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed F"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released F"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed Z"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released Z"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed X"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released X"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed C"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released C"), "released");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed V"), "pressed");
        this.topPane.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released V"), "released");
        this.topPane.getRootPane().getActionMap().put("pressed", keyPressed);
        this.topPane.getRootPane().getActionMap().put("released", keyReleased);
    }

    private void setupAudio() {
        // setup audio
        this.midiNote = 80;
        this.soundAvail = true;
        this.soundPlaying = false;
        Synthesizer synth;
        this.channel0 = null;
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            synth.loadInstrument(
                    synth.getDefaultSoundbank().getInstruments()[0]);
            this.channel0 = synth.getChannels()[0];
        } catch (MidiUnavailableException e1) {
            System.out.println("MIDI sound not available!");
            this.soundAvail = false;
        }
    }

    private void playAudio() {
        // play sounds
        if (this.interpreter.shouldSoundPlay()) {
            if (this.soundAvail && !this.soundPlaying) {
                this.channel0.noteOn(this.midiNote, 100);
                this.soundPlaying = true;
            } else if (!this.soundPlaying) {
                Toolkit.getDefaultToolkit().beep(); // use OS beeping noise
            }
        } else if (this.soundPlaying) {
            this.channel0.noteOff(this.midiNote);
            this.soundPlaying = false;
        }
    }

    chip8 interpreter;
    JPanel topPane;
    JPanel canvas;
    JLabel cpuInfo;
    JFrame mainWin;
    boolean soundAvail;
    boolean soundPlaying;
    MidiChannel channel0;
    int midiNote;
    int delay;
}
