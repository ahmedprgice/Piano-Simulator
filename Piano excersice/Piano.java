import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.*;

public class Piano implements ChangeListener, KeyListener {
    private JButton[] w = new JButton[14]; // 2 octaves of white keys
    private JButton[] b = new JButton[10]; // 2 octaves of black keys (minus breaks)
    private Synthesizer synth;
    private MidiChannel[] mChannels;
    private JComboBox<String> instrumentList;
    private int[] whiteNotes = {60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83}; // White key notes for 2 octaves
    private int[] blackNotes = {61, 63, 66, 68, 70, 73, 75, 78, 80, 82}; // Black key notes for 2 octaves
    private Color defaultWhiteKeyColor = Color.WHITE;
    private Color defaultBlackKeyColor = Color.BLACK;

    Piano() {
        JFrame frame = new JFrame("MIDI Piano");

        // Instrument selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel selectLabel = new JLabel("Select Instrument:");
        selectLabel.setFont(new Font("Arial", Font.BOLD, 14));
        String[] instruments = {"Piano", "Guitar", "Trumpet", "Violin"};
        instrumentList = new JComboBox<>(instruments);
        instrumentList.setFont(new Font("Arial", Font.PLAIN, 14));
        instrumentList.addActionListener(e -> changeInstrument(instrumentList.getSelectedIndex()));
        topPanel.add(selectLabel);
        topPanel.add(instrumentList);
        frame.add(topPanel, BorderLayout.NORTH);

        JLayeredPane panel = new JLayeredPane();
        frame.add(panel, BorderLayout.CENTER);

        // White keys
        for (int i = 0; i < 14; i++) {
            w[i] = new JButton();
            w[i].setBackground(defaultWhiteKeyColor);
            w[i].setName(String.valueOf(whiteNotes[i]));
            w[i].setLocation(i * 75, 0);
            w[i].setSize(75, 300);
            w[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            w[i].addChangeListener(this);
            panel.add(w[i], JLayeredPane.DEFAULT_LAYER);

            // Add note labels to white keys
            JLabel label = new JLabel(getNoteLabel(i), SwingConstants.CENTER);
            label.setBounds(i * 75, 270, 75, 30);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            panel.add(label, JLayeredPane.PALETTE_LAYER);
        }

        // Black keys
        for (int i = 0, blackKeyPos = 0; i < 14; i++) {
            if (i % 7 == 2 || i % 7 == 6) continue; // Skip positions without black keys
            b[blackKeyPos] = new JButton();
            b[blackKeyPos].setBackground(defaultBlackKeyColor);
            b[blackKeyPos].setName(String.valueOf(blackNotes[blackKeyPos]));
            b[blackKeyPos].setLocation(55 + i * 75, 0);
            b[blackKeyPos].setSize(50, 180);
            b[blackKeyPos].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            b[blackKeyPos].addChangeListener(this);
            panel.add(b[blackKeyPos], JLayeredPane.PALETTE_LAYER);
            blackKeyPos++;
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 400);
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.addKeyListener(this);
        frame.setVisible(true);

        // Initialize MIDI
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            mChannels = synth.getChannels();
        } catch (MidiUnavailableException e) {
            JOptionPane.showMessageDialog(null, "Unable to open MIDI.");
        }

        changeInstrument(0); // Set default instrument to Piano
    }

    private void changeInstrument(int instrumentIndex) {
        int[] instruments = {0, 24, 56, 40}; // 0 = Acoustic Grand Piano, 24 = Acoustic Guitar (nylon), 56 = Trumpet, 40 = Violin
        if (mChannels != null) {
            mChannels[0].programChange(instruments[instrumentIndex]); // Change instrument based on the selected index
        } else {
            System.err.println("MIDI channels are not initialized.");
        }
    }

    // Handle button press/release using ChangeListener
    @Override
    public void stateChanged(ChangeEvent e) {
        JButton button = (JButton) e.getSource();
        int note = Integer.parseInt(button.getName());

        if (mChannels != null) {
            if (button.getModel().isPressed()) {
                mChannels[0].noteOn(note, 127); // Play note when pressed
                button.setBackground(Color.YELLOW); // Highlight pressed key
            } else {
                mChannels[0].noteOff(note); // Stop note when released
                if (isBlackKey(button)) {
                    button.setBackground(defaultBlackKeyColor); // Restore black key color
                } else {
                    button.setBackground(defaultWhiteKeyColor); // Restore white key color
                }
            }
        } else {
            System.err.println("MIDI channels are not initialized.");
        }
    }

    // Key listener methods
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

    // Play or stop a note based on key press/release
    private void playNoteForKey(char key, boolean pressed) {
        int note = -1;
        switch (key) {
            case 'a': note = whiteNotes[0]; break;
            case 's': note = whiteNotes[1]; break;
            case 'd': note = whiteNotes[2]; break;
            case 'f': note = whiteNotes[3]; break;
            case 'g': note = whiteNotes[4]; break;
            case 'h': note = whiteNotes[5]; break;
            case 'j': note = whiteNotes[6]; break;
            case 'k': note = whiteNotes[7]; break;
            case 'l': note = whiteNotes[8]; break;
            case ';': note = whiteNotes[9]; break;
            case '\'': note = whiteNotes[10]; break;
            case 'q': note = blackNotes[0]; break;
            case 'w': note = blackNotes[1]; break;
            case 'e': note = blackNotes[2]; break;
            case 'r': note = blackNotes[3]; break;
            case 't': note = blackNotes[4]; break;
        }

        if (note != -1) {
            if (mChannels != null) {
                if (pressed) {
                    mChannels[0].noteOn(note, 127);
                } else {
                    mChannels[0].noteOff(note);
                }
            } else {
                System.err.println("MIDI channels are not initialized.");
            }
        }
    }

    // Helper method to check if the key is black
    private boolean isBlackKey(JButton button) {
        for (JButton blackKey : b) {
            if (blackKey == button) return true;
        }
        return false;
    }

    // Helper method to label white keys with musical notes
    private String getNoteLabel(int index) {
        String[] noteLabels = {"C", "D", "E", "F", "G", "A", "B", "C", "D", "E", "F", "G", "A", "B"};
        return noteLabels[index];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Piano::new);
    }
}
