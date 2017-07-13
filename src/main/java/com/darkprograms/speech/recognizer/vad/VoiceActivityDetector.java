package com.darkprograms.speech.recognizer.vad;

import com.darkprograms.speech.microphone.MicrophoneAnalyzer;
import com.darkprograms.speech.util.FFT;

import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implementation of [https://www.researchgate.net/publication/255667085_A_simple_but_efficient_real-time_voice_activity_detection_algorithm]
 *
 * @see [https://github.com/Sciss/SpeechRecognitionHMM/blob/master/src/main/java/org/ioe/tprsa/audio/preProcessings/EndPointDetection.java]
 */
public class VoiceActivityDetector implements Runnable {
    private static final int WINDOW_MILLIS = 10;
    private static final double WINDOW_SECONDS = (double)WINDOW_MILLIS / 1000;
    private static final int IGNORE_SILENCE_WINDOWS = 10;
    private static final int IGNORE_SPEECH_WINDOWS = 5;
    /** maximum ms between words */
    private static final int MAX_SILENCE_MILLIS = 4;
    /** minimum duration of speech to recognise */
    private static final int MIN_SPEECH_MILLIS = 200;
    private static final int MAX_SPEECH_MILLIS = 60_000;
    private static final int MAX_SILENCE_WINDOWS = MAX_SILENCE_MILLIS / WINDOW_MILLIS;
    private static final int MIN_SPEECH_WINDOWS = MIN_SPEECH_MILLIS / WINDOW_MILLIS;
    private static final int MAX_SPEECH_WINDOWS = MAX_SPEECH_MILLIS / WINDOW_MILLIS;
    private static final int ENERGY_PRIMARY_THRESHOLD = 40;
    private static final int FREQUENCY_PRIMARY_THRESHOLD = 185;
    private static final int SPECTRAL_FLATNESS_PRIMARY_THRESHOLD = 5;

    private AudioInputStream audio;
    private MicrophoneAnalyzer mic;
    private VoiceActivityListener listener;
    private VadState state;

    private enum VadState {
        LISTENING,
        DETECTED_SPEECH,
        DETECTED_SILENCE_AFTER_SPEECH
    }

    // TODO: optionally provide PipedInputStream to support streaming recogntion on Google
    public void detectVoiceActivity(MicrophoneAnalyzer mic, VoiceActivityListener listener) {
        this.listener = listener;
        this.mic = mic;
        this.audio = mic.captureAudioToStream();
        new Thread(this, "JARVIS-VAD").start();
    }

    public void run() {
        byte[] audioData = new byte[mic.getNumOfBytes(WINDOW_SECONDS)];
        int offset = 0;
        int bufferSize = MAX_SPEECH_MILLIS * this.mic.getNumOfBytes(0.001);
        int silenceCount = 0;
        int speechCount = 0;
        int minEnergy = Integer.MAX_VALUE;
        int minFrequency = Integer.MAX_VALUE;
        int minSpectralFlatness = Integer.MAX_VALUE;
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream(bufferSize);

        state = VadState.LISTENING;

        while (true) {
            try {
                int bytesRead = this.audio.read(audioData);

                int counter = 0;
                int energy = mic.calculateRMSLevel(audioData);
                int frequency = mic.getFrequency(audioData);

                // ignore frequencies above 400hz (and below 50Hz?)
                if (frequency < 400) {
                    //     3-2-2- Compute the abstract value of Spectral Flatness Measure SFM(i)
// TODO        https://github.com/filipeuva/SoundBites/blob/master/src/uk/co/biogen/SoundBites/analysis/AnalysisInterface.java#L264

                    //   3-3- Supposing that some of the first 30 frames are silence, find the minimum value for E, F & SF
                    minEnergy = Math.min(minEnergy, energy);
                    minFrequency = Math.min(minFrequency, frequency);
//                minSpectralFlatness = Math.min(minSpectralFlatness, energy);

                    double energyThreshold = ENERGY_PRIMARY_THRESHOLD * Math.log(minEnergy);

                    if (energy - minEnergy >= energyThreshold) counter++;
                    if (frequency - minFrequency >= FREQUENCY_PRIMARY_THRESHOLD) counter++;
//                if (sfm - minSpectralFlatness) >= SPECTRAL_FLATNESS_PRIMARY_THRESHOLD) counter++;
                }

                if (counter > 1) {
                    // speech
                    speechCount++;
                    // Ignore speech runs less than 5 successive frames.
                    if (state != VadState.DETECTED_SPEECH && speechCount >= IGNORE_SPEECH_WINDOWS) {
                        state = VadState.DETECTED_SPEECH;
                        silenceCount = 0;
                    }

                    if (offset + bytesRead < bufferSize) {
                        outBuffer.write(audioData, offset, bytesRead);
                        offset += bytesRead;

                        if (speechCount >= MAX_SPEECH_WINDOWS) {
                            // in theory, this should be handled by the following end of buffer handler
                            emitVoiceActivity(outBuffer);
                            offset = 0;
                        }
                    } else {
                        // Reached the end of the buffer! Send what we've captured so far
                        bytesRead = bufferSize - offset;
                        outBuffer.write(audioData, offset, bytesRead);
                        emitVoiceActivity(outBuffer);
                        offset = 0;
                    }
                } else {
                    // silence
                    silenceCount++;
                    minEnergy = ((silenceCount * minEnergy) + energy) / (silenceCount + 1);
                    //   Ignore silence runs less than 10 successive frames.
                    if (state == VadState.DETECTED_SPEECH && silenceCount >= IGNORE_SILENCE_WINDOWS) {
                        if (silenceCount >= MAX_SILENCE_WINDOWS && speechCount >= MIN_SPEECH_WINDOWS) {
                            // We have silence after a chunk of speech worth processing
                            emitVoiceActivity(outBuffer);
                            offset = 0;
                        }

                        state = VadState.DETECTED_SILENCE_AFTER_SPEECH;
                        speechCount = 0;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void emitVoiceActivity(ByteArrayOutputStream outBuffer) {
        listener.onVoiceActivity(createVoiceActivityStream(outBuffer));
        outBuffer.reset();
        state = VadState.LISTENING;
    }

    private AudioInputStream createVoiceActivityStream(ByteArrayOutputStream outBuffer) {
        return new AudioInputStream(new ByteArrayInputStream(outBuffer.toByteArray()), audio.getFormat(), mic.getNumOfFrames(outBuffer.size()));
    }
}
