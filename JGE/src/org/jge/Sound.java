package org.jge;

import javax.sound.sampled.*;

import java.io.*;

public class Sound extends Resource {

    public final File file;

    private Clip clip;
    private FloatControl gain;

    public Sound(File file) throws Exception {
        this.file = file;
        AudioInputStream stream = AudioSystem.getAudioInputStream(file);
        clip = AudioSystem.getClip();
        clip.open(stream);
        gain = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
        stream.close();
    }

    public void setVolume(float volume) {
        if(gain != null) {
            volume = Math.max(0, Math.min(1, volume));
            gain.setValue(gain.getMinimum() + volume * (gain.getMaximum() - gain.getMinimum()));
        }
    }

    public void play(boolean looping) {
        if(!clip.isActive()) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
            if(looping) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.loop(0);
            }
        }
    }

    public void stop() {
        clip.stop();
    }

    @Override
    public void destroy() throws Exception {
        clip.stop();
        clip.close();
        super.destroy();
    }
}
