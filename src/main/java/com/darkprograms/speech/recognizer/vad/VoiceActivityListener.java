package com.darkprograms.speech.recognizer.vad;

import javax.sound.sampled.AudioInputStream;

public interface VoiceActivityListener {
    public void onVoiceeActivity(AudioInputStream audioInputStream);
}
