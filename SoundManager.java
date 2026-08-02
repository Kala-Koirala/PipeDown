import javax.sound.midi.*;

public class SoundManager {
    private Synthesizer synth;
    private MidiChannel channel; // "lead" channel — used for jump/score/game over/menu
    private MidiChannel bellChannel; // "sparkle" channel — used for exciting moments
    private boolean soundEnabled = true;

    public SoundManager() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            MidiChannel[] channels = synth.getChannels();
            if (channels != null && channels.length > 1) {
                // Channel 0: our regular retro square-wave lead
                channel = channels[0];
                channel.programChange(80); // Lead 1 (square)

                // Channel 1: a bright bell/chime sound, kept separate so it
                // can play AT THE SAME TIME as channel 0 without fighting
                // over the same instrument. Program 9 = Glockenspiel.
                bellChannel = channels[1];
                bellChannel.programChange(9);
            } else if (channels != null && channels.length > 0) {
                // Fallback: only one channel available, share it
                channel = channels[0];
                channel.programChange(80);
                bellChannel = channel;
            }
        } catch (Exception e) {
            System.err.println("Could not initialize MIDI Synthesizer: " + e.getMessage());
            channel = null;
            bellChannel = null;
        }
    }

    public void playJump() {
        if (!soundEnabled || channel == null)
            return;
        new Thread(() -> {
            try {
                // Quick 3-note upward chirp instead of 2 — feels snappier
                channel.noteOn(60, 90); // C5
                Thread.sleep(35);
                channel.noteOff(60);
                channel.noteOn(64, 100); // E5
                Thread.sleep(35);
                channel.noteOff(64);
                channel.noteOn(69, 110); // A5
                Thread.sleep(70);
                channel.noteOff(69);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    public void playScore() {
        if (!soundEnabled || channel == null)
            return;
        new Thread(() -> {
            try {
                // Wider, punchier interval (C6 -> G6 instead of C6 -> E6)
                channel.noteOn(72, 100);
                Thread.sleep(70);
                channel.noteOff(72);
                channel.noteOn(79, 110);
                Thread.sleep(130);
                channel.noteOff(79);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    public void playGameOver() {
        if (!soundEnabled || channel == null)
            return;
        new Thread(() -> {
            try {
                // Descending notes, same idea as before but a touch snappier
                channel.noteOn(62, 90);
                Thread.sleep(130);
                channel.noteOff(62);
                channel.noteOn(58, 90);
                Thread.sleep(130);
                channel.noteOff(58);
                // Final note is a CHORD: two notes played at once (root + fifth)
                // for a fuller, more "final" sounding thud
                channel.noteOn(55, 100);
                channel.noteOn(43, 90); // one octave + a fifth lower
                Thread.sleep(400);
                channel.noteOff(55);
                channel.noteOff(43);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    // Plays when the player beats their personal best score.
    // Uses the bell channel for extra sparkle, plus a chord at the end.
    public void playHighScore() {
        if (!soundEnabled || bellChannel == null)
            return;
        new Thread(() -> {
            try {
                int[] notes = { 60, 64, 67, 72 }; // rising run: C5 -> E5 -> G5 -> C6
                for (int note : notes) {
                    bellChannel.noteOn(note, 110);
                    Thread.sleep(70);
                    bellChannel.noteOff(note);
                }
                // Big finishing CHORD (C6 + E6 + G6 together) for a "ta-da!" moment
                bellChannel.noteOn(72, 120);
                bellChannel.noteOn(76, 110);
                bellChannel.noteOn(79, 110);
                Thread.sleep(400);
                bellChannel.noteOff(72);
                bellChannel.noteOff(76);
                bellChannel.noteOff(79);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    public void playMenuSelect() {
        if (!soundEnabled || channel == null)
            return;
        new Thread(() -> {
            try {
                // Two quick notes instead of one — feels more like a "confirm" blip
                channel.noteOn(69, 80);
                Thread.sleep(45);
                channel.noteOff(69);
                channel.noteOn(74, 90);
                Thread.sleep(45);
                channel.noteOff(74);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    public void playClick() {
        if (!soundEnabled || channel == null)
            return;
        new Thread(() -> {
            try {
                channel.noteOn(84, 70);
                Thread.sleep(30);
                channel.noteOff(84);
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        }).start();
    }

    // A quick, cheerful rising "ding" on the bell channel — stands out from
    // the regular gameplay sounds so the player really notices it.
    public void playLevelUp() {
        if (!soundEnabled || bellChannel == null)
            return;
        new Thread(() -> {
            try {
                bellChannel.noteOn(65, 100); // F5
                Thread.sleep(50);
                bellChannel.noteOff(65);
                bellChannel.noteOn(69, 110); // A5
                Thread.sleep(50);
                bellChannel.noteOff(69);
                bellChannel.noteOn(77, 120); // F6 — jumps up an octave-ish for excitement
                Thread.sleep(120);
                bellChannel.noteOff(77);
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