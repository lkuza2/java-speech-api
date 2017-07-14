package com.darkprograms.speech.recognizer.vad;

/**
 * Adapted from https://stackoverflow.com/questions/18815235/can-i-use-google-speech-recognition-api-in-my-desktop-application
 */
public class SimpleVAD extends AbstractVAD {
    private int threshold = 10;
    private int ambientVolume;
    private int speakingVolume;
    private boolean speaking;

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void run() {
        speakingVolume = -2;
        speaking = false;
        ambientVolume = mic.getAudioVolume();
        super.run();
    }

    @Override
    protected boolean sampleForSpeech(byte[] audioData) {
        int volume = mic.calculateRMSLevel(audioData);
System.out.println(volume);
        if (volume > ambientVolume + threshold) {
            speakingVolume = volume;
            speaking = true;
        }
        if (speaking && volume + threshold < speakingVolume) {
            speaking = false;
        }
        return speaking;
    }
}
