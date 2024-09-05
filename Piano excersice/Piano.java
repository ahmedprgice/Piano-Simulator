import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.*;

public class Piano implements ChangeListener, KeyListener {
    private static final int NUM_WHITE_KEYS = 14;
    private static final int NUM_BLACK_KEYS = 10;
    private static final int WHITE_KEY_WIDTH = 75;
    private static final int WHITE_KEY_HEIGHT = 300;
    private static final int BLACK_KEY_WIDTH = 50;
    private static final int BLACK_KEY_HEIGHT = 180;
    private static final Color DEFAULT_WHITE_KEY_COLOR = Color.WHITE;
    private static final Color DEFAULT_BLACK_KEY_COLOR = Color.BLACK;
    private static final Color PRESSED_KEY_COLOR = Color.YELLOW;

    private JButton[] whiteKeys = new JButton[NUM_WHITE_KEYS];
    private JButton[] blackKeys = new JButton[NUM_BLACK_KEYS];
    private Synthesizer synthesizer;
    private MidiChannel[] midiChannels;
    private JComboBox<String> instrumentList;

    private static final int[] WHITE_NOTES = {60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83}; // White key notes for 2 octaves
    private static final int[] BLACK_NOTES = {61, 63, 66, 68, 70, 73, 75, 78, 80, 82}; // Black key notes for 2 octaves

    private static final String[] INSTRUMENTS = {"Piano", "Guitar", "Trumpet", "Violin", "Flute", "Saxophone"};
    private static final int[] INSTRUMENT_PROGRAMS = {0, 24, 56, 40, 73, 65}; // Program numbers for instruments

    public Piano() {
        JFrame frame = new JFrame("MIDI Piano");

        // Instrument selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel selectLabel = new JLabel("Select Instrument:");
        selectLabel.setFont(new Font("Arial", Font.BOLD, 14));
        instrumentList = new JComboBox<>(INSTRUMENTS);
        instrumentList.setFont(new Font("Arial", Font.PLAIN, 14));
        instrumentList.addActionListener(e -> changeInstrument(instrumentList.getSelectedIndex()));
        topPanel.add(selectLabel);
        topPanel.add(instrumentList);
        frame.add(topPanel, BorderLayout.NORTH);

        JLayeredPane panel = new JLayeredPane();
        frame.add(panel, BorderLayout.CENTER);

        initializeWhiteKeys(panel);
        initializeBlackKeys(panel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 400);
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.addKeyListener(this);
        frame.setVisible(true);

        initializeMIDI();
        changeInstrument(0); // Set default instrument to Piano
    }

    private void initializeWhiteKeys(JLayeredPane panel) {
        for (int i = 0; i < NUM_WHITE_KEYS; i++) {
            whiteKeys[i] = new JButton();
            whiteKeys[i].setBackground(DEFAULT_WHITE_KEY_COLOR);
            whiteKeys[i].setName(String.valueOf(WHITE_NOTES[i]));
            whiteKeys[i].setLocation(i * WHITE_KEY_WIDTH, 0);
            whiteKeys[i].setSize(WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT);
            whiteKeys[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            whiteKeys[i].addChangeListener(this);
            panel.add(whiteKeys[i], JLayeredPane.DEFAULT_LAYER);

            JLabel label = new JLabel(getNoteLabel(i), SwingConstants.CENTER);
            label.setBounds(i * WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT - 30, WHITE_KEY_WIDTH, 30);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            panel.add(label, JLayeredPane.PALETTE_LAYER);
        }
    }

    private void initializeBlackKeys(JLayeredPane panel) {
        for (int i = 0, pos = 0; i < NUM_WHITE_KEYS; i++) {
            if (i % 7 == 2 || i % 7 == 6) continue; // Skip positions without black keys
            blackKeys[pos] = new JButton();
            blackKeys[pos].setBackground(DEFAULT_BLACK_KEY_COLOR);
            blackKeys[pos].setName(String.valueOf(BLACK_NOTES[pos]));
            blackKeys[pos].setLocation(55 + i * WHITE_KEY_WIDTH, 0);
            blackKeys[pos].setSize(BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT);
            blackKeys[pos].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            blackKeys[pos].addChangeListener(this);
            panel.add(blackKeys[pos], JLayeredPane.PALETTE_LAYER);
            pos++;
        }
    }

    private void initializeMIDI() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            midiChannels = synthesizer.getChannels();
        } catch (MidiUnavailableException e) {
            JOptionPane.showMessageDialog(null, "Unable to open MIDI.");
        }
    }

    private void changeInstrument(int instrumentIndex) {
        if (midiChannels != null) {
            midiChannels[0].programChange(INSTRUMENT_PROGRAMS[instrumentIndex]);
        } else {
            System.err.println("MIDI channels are not initialized.");
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JButton button = (JButton) e.getSource();
        int note = Integer.parseInt(button.getName());

        if (midiChannels != null) {
            if (button.getModel().isPressed()) {
                midiChannels[0].noteOn(note, 127); // Play note when pressed
                button.setBackground(PRESSED_KEY_COLOR); // Highlight pressed key
            } else {
                midiChannels[0].noteOff(note); // Stop note when released
                button.setBackground(isBlackKey(button) ? DEFAULT_BLACK_KEY_COLOR : DEFAULT_WHITE_KEY_COLOR);
            }
        } else {
            System.err.println("MIDI channels are not initialized.");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        char key = e.getKeyChar();
        playNoteForKey(key, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        char key = e.getKeyChar();
        playNoteForKey(key, false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No action needed
    }

    private void playNoteForKey(char key, boolean pressed) {
        int note = getNoteFromKey(key);
        if (note != -1 && midiChannels != null) {
            if (pressed) {
                midiChannels[0].noteOn(note, 127);
            } else {
                midiChannels[0].noteOff(note);
            }
        }
    }

    private int getNoteFromKey(char key) {
        switch (key) {
            case 'a': return WHITE_NOTES[0];
            case 's': return WHITE_NOTES[1];
            case 'd': return WHITE_NOTES[2];
            case 'f': return WHITE_NOTES[3];
            case 'g': return WHITE_NOTES[4];
            case 'h': return WHITE_NOTES[5];
            case 'j': return WHITE_NOTES[6];
            case 'k': return WHITE_NOTES[7];
            case 'l': return WHITE_NOTES[8];
            case ';': return WHITE_NOTES[9];
            case '\'': return WHITE_NOTES[10];
            case 'q': return BLACK_NOTES[0];
            case 'w': return BLACK_NOTES[1];
            case 'e': return BLACK_NOTES[2];
            case 'r': return BLACK_NOTES[3];
            case 't': return BLACK_NOTES[4];
            default: return -1;
        }
    }

    private boolean isBlackKey(JButton button) {
        for (JButton blackKey : blackKeys) {
            if (blackKey == button) return true;
        }
        return false;
    }

    private String getNoteLabel(int index) {
        String[] noteLabels = {"C", "D", "E", "F", "G", "A", "B", "C", "D", "E", "F", "G", "A", "B"};
        return noteLabels[index];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Piano::new);
    }
}
