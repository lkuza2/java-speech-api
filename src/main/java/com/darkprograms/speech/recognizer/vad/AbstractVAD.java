package com.darkprograms.speech.recognizer.vad;

import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.microphone.MicrophoneAnalyzer;

import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public abstract class AbstractVAD implements VoiceActivityDetector, Runnable {
    private static final int WINDOW_MILLIS = 16;
    private static final int IGNORE_SILENCE_WINDOWS = 10;
    private static final int IGNORE_SPEECH_WINDOWS = 5;
    /** maximum ms between words */
    private static final int MAX_SILENCE_MILLIS = 4;
    /** minimum duration of speech to recognise */
    private static final int MIN_SPEECH_MILLIS = 200;
    private static final double WINDOW_SECONDS = (double)WINDOW_MILLIS / 1000;
    /** Google does not allow recordings over 1 minute, but 10 seconds should be ample */
    private static final int MAX_SPEECH_MILLIS = 10_000;
    private static final int MAX_SILENCE_WINDOWS = MAX_SILENCE_MILLIS / WINDOW_MILLIS;
    private static final int MIN_SPEECH_WINDOWS = MIN_SPEECH_MILLIS / WINDOW_MILLIS;

    protected AudioInputStream audio;
    MicrophoneAnalyzer mic;
    private VoiceActivityListener listener;
    private VadState state;
    private Thread thread;

    private int maxSpeechMs;
    private int maxSpeechWindows;
    int silenceCount;
    private int speechCount;

    private int offset;
    private int bufferSize;
    private ByteArrayOutputStream outBuffer;

    // TODO: optionally provide PipedInputStream to support streaming recogntion on Google
    public void detectVoiceActivity(MicrophoneAnalyzer mic, VoiceActivityListener listener) {
        detectVoiceActivity(mic, MAX_SPEECH_MILLIS, listener);
    }

    /** Initialise the VAD and start a thread */
    @Override
    public void detectVoiceActivity(MicrophoneAnalyzer mic, int maxSpeechMs, VoiceActivityListener listener) {
        this.listener = listener;
        this.maxSpeechMs = maxSpeechMs;
        maxSpeechWindows = maxSpeechMs / WINDOW_MILLIS;

        if (this.mic != null) {
            if (this.mic == mic) {
                // re-open the same mic
                if (mic.getState() == Microphone.CaptureState.CLOSED) {
                    mic.open();
                }
                return;
            } else {
                // swap mics
                this.audio = mic.captureAudioToStream();
                this.mic.close();
            }
        } else {
            this.audio = mic.captureAudioToStream();
        }

        this.mic = mic;
    }

    @Override
    public void setVoiceActivityListener(VoiceActivityListener listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        thread = new Thread(this, "JARVIS-VAD");
        thread.start();
    }

    @Override
    public void terminate() {
//        state = VadState.CLOSED;
        thread.interrupt();
    }

    /**
     * Continuously reads "windows" of audio into a buffer and delegates to {@link #sampleForSpeech(byte[])}
     * and {@link #incrementSpeechCounter(boolean, int, byte[])}.
     * {@link #emitVoiceActivity(ByteArrayOutputStream)} will be called when an utterance has been captured.
     */
    @Override
    public void run() {
        int bytesToRead = mic.getNumOfBytes(WINDOW_SECONDS);
        byte[] audioData = new byte[bytesToRead];
        bufferSize = maxSpeechMs * this.mic.getNumOfBytes(0.001);
        silenceCount = 0;
        speechCount = 0;
        offset = 0;
        outBuffer = new ByteArrayOutputStream(bufferSize);

        state = VoiceActivityDetector.VadState.LISTENING;

        while (state != VadState.CLOSED) {
            try {
                int bytesRead = this.audio.read(audioData, 0, bytesToRead);
                boolean speechDetected = sampleForSpeech(audioData);
                incrementSpeechCounter(speechDetected, bytesRead, audioData);
            } catch (Exception e) {
                e.printStackTrace();
                state = VadState.CLOSED;
                return;
            }
        }
    }

    /**
     * Executed from within the VAD thread
     * @param audioData
     * @return
     */
    protected abstract boolean sampleForSpeech(byte[] audioData);

    protected void incrementSpeechCounter(boolean speechDetected, int bytesRead, byte[] audioData) {
        if (speechDetected) {
            speechCount++;
            // Ignore speech runs less than 5 successive frames.
            if (state != VoiceActivityDetector.VadState.DETECTED_SPEECH && speechCount >= IGNORE_SPEECH_WINDOWS) {
                state = VoiceActivityDetector.VadState.DETECTED_SPEECH;
                silenceCount = 0;
            }

            if (offset + bytesRead < bufferSize) {
                outBuffer.write(audioData, 0, bytesRead);
                offset += bytesRead;

                if (speechCount >= maxSpeechWindows) {
                    System.out.println("in theory, this should be handled by the following end of buffer handler");
                    emitVoiceActivity(outBuffer);
                }
            } else {
                System.out.println("Reached the end of the buffer! Send what we've captured so far");
                bytesRead = bufferSize - offset;
                outBuffer.write(audioData, 0, bytesRead);
                emitVoiceActivity(outBuffer);
            }
        } else {
            // silence
            silenceCount++;

            //   Ignore silence runs less than 10 successive frames.
            if (state == VoiceActivityDetector.VadState.DETECTED_SPEECH && silenceCount >= IGNORE_SILENCE_WINDOWS) {
                if (silenceCount >= MAX_SILENCE_WINDOWS && speechCount >= MIN_SPEECH_WINDOWS) {
                    System.out.println("We have silence after a chunk of speech worth processing");
                    emitVoiceActivity(outBuffer);
                } else {
                    state = VoiceActivityDetector.VadState.DETECTED_SILENCE_AFTER_SPEECH;
                }

                speechCount = 0;
            }
        }
    }

    protected void emitVoiceActivity(ByteArrayOutputStream outBuffer) {
        listener.onVoiceActivity(createVoiceActivityStream(outBuffer));
        outBuffer.reset();
        offset = 0;
        state = VadState.LISTENING;
    }

    protected AudioInputStream createVoiceActivityStream(ByteArrayOutputStream outBuffer) {
        System.out.println("speech: " + mic.getAudioFormat().getFrameSize() * mic.getNumOfFrames(outBuffer.size()));
        return new AudioInputStream(new ByteArrayInputStream(outBuffer.toByteArray()), audio.getFormat(), mic.getNumOfFrames(outBuffer.size()));
    }
}
