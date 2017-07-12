package com.darkprograms.speech.recognizer.vad;

import com.darkprograms.speech.microphone.MicrophoneAnalyzer;

import javax.sound.sampled.AudioInputStream;

/**
 * @see https://www.researchgate.net/publication/255667085_A_simple_but_efficient_real-time_voice_activity_detection_algorithm
 */
public class VoiceActivityDetector {

//        MicrophoneAnalyzer mic = new MicrophoneAnalyzer(null);
//        mic.captureAudioToStream(16_000F);
    public void detect(AudioInputStream audio) {
        // E: short-term energy
        // SFM: Spectral Flatness Measure - a measure of the noisiness of spectrum and is a good feature in Voiced/Unvoiced/Silence detection.
        //    SFM = 10 log10(Gm/Am)
        //    Am: arithmetic mean
        //    Gm: geometric mean
        // F: most dominant frequency component of the speech frame spectrum


        // 1- Set  Frame _ Size 10ms=and compute number of frames (FramesOfNum __ )(no frame overlap is required)
        // 2- Set one primary threshold for each feature {These thresholds are the only parameters that are set externally}
        //  • Primary Threshold for Energy (Energy_PrimThresh)
        //  • Primary Threshold for F (F_PrimThresh)
        //  • Primary Threshold for SFM  (SF_PrimThresh)
        // 3- for i from 1 to numOfFrames
        //   3-1- Compute frame energy E(i)
        //   3-2- Apply FFT on each speech frame.
        //     3-2-1- Find kF(i) = arg max(S(k)) as the most dominant frequency component.
        //     3-2-2- Compute the abstract value of Spectral Flatness Measure SFM(i)
        //         3-3- Supposing that some of the first 30 frames are silence, find the minimum value for E(minE) , F(minF) and SFM (minSF)
        //   3-4- Set Decision threshold forE, F and SFM
        //     • threshE = energyPrimThresh * log(minE)
        //     • threshF = fPrimThresh
        //     • threshSF = sfPrimThresh
        //   3-5- Set 0=Counter
        //     • if ((E(i) - minE) >= threshE) then ++Counter
        //     • if ((F(i) - minF) >= threshF) then ++Counter
        //     • if ((SFM(i) - minSF) >= threshSF) then ++Counter
        //   3-6- If Counter > mark the current frame as speech else mark it as silence.
        //   3-7- If current frame is marked as silence, update the energy minimum value:
        //      minE = (silenceCount * minE) + E(i)) / (silenceCount + 1)
        //   3-8- threshE = energyPrimThresh * log(minE)
        //   4- Ignore silence run less than 10 successive frames.
        //   5- Ignore speech run less than 5 successive frames.

    }
}
