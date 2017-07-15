package com.darkprograms.speech.recognizer.vad;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Useful for debugging & testing microphone
 */
public class RecordingListener implements VoiceActivityListener {
    private VoiceActivityListener nextListener;

    @Override
    public void onVoiceActivity(AudioInputStream audioInputStream) {
        String fileName = new Date().toString() + ".wav";
        File out = new File("/tmp", fileName);

        try {
            System.out.println("Saving recoring to " + out.getAbsolutePath());
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (nextListener != null) {
            nextListener.onVoiceActivity(audioInputStream);
        }
    }

    public RecordingListener withNextListener(VoiceActivityListener nextListener) {
        this.nextListener = nextListener;
        return this;
    }

    public void setNextListener(VoiceActivityListener nextListener) {
        this.nextListener = nextListener;
    }
}
