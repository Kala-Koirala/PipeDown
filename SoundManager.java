import javax.sound.midi.*;

public class SoundManager {
    private Synthesizer synth;
    private MidiChannel channel; 
    private MidiChannel bellChannel; 
    private boolean soundEnabled = true;

    public SoundManager() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            MidiChannel[] channels = synth.getChannels();
            if (channels != null && channels.length > 1) {
                channel = channels[0];
                channel.programChange(80); 

                bellChannel = channels[1];
                bellChannel.programChange(9);
            } else if (channels != null && channels.length > 0) {
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
                channel.noteOn(60, 90); 
                Thread.sleep(35);
                channel.noteOff(60);
                channel.noteOn(64, 100); 
                Thread.sleep(35);
                channel.noteOff(64);
                channel.noteOn(69, 110); 
                Thread.sleep(70);
                channel.noteOff(69);
            } catch (Exception e) {
            }
        }).start();
    }

    public void playScore() {
        if (!soundEnabled || channel == null)
            return;
        new Thread(() -> {
            try {
                channel.noteOn(72, 100);
                Thread.sleep(70);
                channel.noteOff(72);
                channel.noteOn(79, 110);
                Thread.sleep(130);
                channel.noteOff(79);
            } catch (Exception e) {
            }
        }).start();
    }

    public void playGameOver() {
        if (!soundEnabled || channel == null)
            return;
        new Thread(() -> {
            try {
                channel.noteOn(62, 90);
                Thread.sleep(130);
                channel.noteOff(62);
                channel.noteOn(58, 90);
                Thread.sleep(130);
                channel.noteOff(58);
               
                channel.noteOn(55, 100);
                channel.noteOn(43, 90); 
                Thread.sleep(400);
                channel.noteOff(55);
                channel.noteOff(43);
            } catch (Exception e) {
            }
        }).start();
    }

    
    public void playHighScore() {
        if (!soundEnabled || bellChannel == null)
            return;
        new Thread(() -> {
            try {
                int[] notes = { 60, 64, 67, 72 }; 
                for (int note : notes) {
                    bellChannel.noteOn(note, 110);
                    Thread.sleep(70);
                    bellChannel.noteOff(note);
                }
                bellChannel.noteOn(72, 120);
                bellChannel.noteOn(76, 110);
                bellChannel.noteOn(79, 110);
                Thread.sleep(400);
                bellChannel.noteOff(72);
                bellChannel.noteOff(76);
                bellChannel.noteOff(79);
            } catch (Exception e) {
            }
        }).start();
    }

    public void playMenuSelect() {
        if (!soundEnabled || channel == null)
            return;
        new Thread(() -> {
            try {
                channel.noteOn(69, 80);
                Thread.sleep(45);
                channel.noteOff(69);
                channel.noteOn(74, 90);
                Thread.sleep(45);
                channel.noteOff(74);
            } catch (Exception e) {
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
            }
        }).start();
    }

    public void playLevelUp() {
        if (!soundEnabled || bellChannel == null)
            return;
        new Thread(() -> {
            try {
                bellChannel.noteOn(65, 100); 
                Thread.sleep(50);
                bellChannel.noteOff(65);
                bellChannel.noteOn(69, 110); 
                Thread.sleep(50);
                bellChannel.noteOff(69);
                bellChannel.noteOn(77, 120);
                Thread.sleep(120);
                bellChannel.noteOff(77);
            } catch (Exception e) {
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