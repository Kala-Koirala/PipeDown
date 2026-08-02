import javax.sound.midi.*;

public class SoundManager {
    private Synthesizer synth;
    private MidiChannel channel;
    private boolean soundEnabled = true;

    public SoundManager() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            MidiChannel[] channels = synth.getChannels();
            if (channels != null && channels.length > 0) {
                // Use channel 0 for sound effects
                channel = channels[0];
                // MIDI Program 80 is "Lead 1 (square)" which sounds like a retro square wave synth
                channel.programChange(80);
            }
        } catch (Exception e) {
            System.err.println("Could not initialize MIDI Synthesizer: " + e.getMessage());
            channel = null;
        }
    }

    public void playJump() {
        if (!soundEnabled || channel == null) return;
        new Thread(() -> {
            try {
                // Rising jump pitch (C5 to G5 quickly)
                channel.noteOn(60, 80);
                Thread.sleep(40);
                channel.noteOff(60);
                channel.noteOn(67, 90);
                Thread.sleep(80);
                channel.noteOff(67);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    public void playScore() {
        if (!soundEnabled || channel == null) return;
        new Thread(() -> {
            try {
                // Upbeat score sound (C6 to E6)
                channel.noteOn(72, 90);
                Thread.sleep(80);
                channel.noteOff(72);
                channel.noteOn(76, 100);
                Thread.sleep(150);
                channel.noteOff(76);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    public void playGameOver() {
        if (!soundEnabled || channel == null) return;
        new Thread(() -> {
            try {
                // Melancholy descending death notes (D5 -> Bb4 -> G4)
                channel.noteOn(62, 90);
                Thread.sleep(150);
                channel.noteOff(62);
                channel.noteOn(58, 90);
                Thread.sleep(150);
                channel.noteOff(58);
                channel.noteOn(55, 100);
                Thread.sleep(350);
                channel.noteOff(55);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    // Plays when the player beats their personal best score.
    // This is just like playScore(), but with more notes so it feels
    // more exciting/rewarding than the normal "you passed a pipe" sound.
    public void playHighScore() {
        if (!soundEnabled || channel == null) return;
        new Thread(() -> {
            try {
                // Ascending 4-note "fanfare" (C5 -> E5 -> G5 -> C6)
                int[] notes = {60, 64, 67, 72};
                for (int note : notes) {
                    channel.noteOn(note, 100);
                    Thread.sleep(90);
                    channel.noteOff(note);
                }
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    // A short, neutral "blip" for menu actions, like opening/closing
    // the pause menu. Doesn't need its own thread since it's a single
    // quick note and won't noticeably freeze the game.
    public void playMenuSelect() {
        if (!soundEnabled || channel == null) return;
        new Thread(() -> {
            try {
                channel.noteOn(69, 70); // A4, played softly
                Thread.sleep(60);
                channel.noteOff(69);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    // A tiny confirmation "click" — used when the player turns sound
    // back ON, so they get feedback that the button press worked.
    // (We never call this while muting, since that would be pointless!)
    public void playClick() {
        if (!soundEnabled || channel == null) return;
        new Thread(() -> {
            try {
                channel.noteOn(84, 60); // High, short, and quiet
                Thread.sleep(30);
                channel.noteOff(84);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    // A quick rising "ding" to tell the player the game just got harder.
    // Called from FlappyBird.java whenever the difficulty tier changes.
    public void playLevelUp() {
        if (!soundEnabled || channel == null) return;
        new Thread(() -> {
            try {
                channel.noteOn(65, 90); // F5
                Thread.sleep(60);
                channel.noteOff(65);
                channel.noteOn(69, 100); // A5
                Thread.sleep(80);
                channel.noteOff(69);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    public void toggleSound() {
        soundEnabled = !soundEnabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void shutdown() {
        if (synth != null && synth.isOpen()) {
            synth.close();
        }
    }
}
