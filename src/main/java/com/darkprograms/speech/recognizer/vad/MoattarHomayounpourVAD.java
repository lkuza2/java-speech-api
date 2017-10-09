package com.darkprograms.speech.recognizer.vad;

/**
 * Implementation of [https://www.researchgate.net/publication/255667085_A_simple_but_efficient_real-time_voice_activity_detection_algorithm]
 *
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * !! WARNING - this is not working correctly !!
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 *
 * TODO: need to calculate Spectral Flatness Measure
 */
public class MoattarHomayounpourVAD extends AbstractVAD {
    private static final int ENERGY_PRIMARY_THRESHOLD = 40;
    private static final int FREQUENCY_PRIMARY_THRESHOLD = 185;
    private static final int SPECTRAL_FLATNESS_PRIMARY_THRESHOLD = 5;

    private int minEnergy = Integer.MAX_VALUE;
    private int minFrequency = Integer.MAX_VALUE;
    private int minSpectralFlatness = Integer.MAX_VALUE;

    @Override
    public void run() {
        minEnergy = Integer.MAX_VALUE;
        minFrequency = Integer.MAX_VALUE;
        minSpectralFlatness = Integer.MAX_VALUE;
        super.run();
    }

    @Override
    protected boolean sampleForSpeech(byte[] audioData) {
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
            System.out.println("energy: " + energy + "\tfrequency:" + frequency);
            if (energy - minEnergy >= energyThreshold) counter++;
            if (frequency - minFrequency >= FREQUENCY_PRIMARY_THRESHOLD) counter++;
//                if (sfm - minSpectralFlatness) >= SPECTRAL_FLATNESS_PRIMARY_THRESHOLD) counter++;
        }

        if(counter > 1) {
            return true;
        } else {
            minEnergy = ((silenceCount * minEnergy) + energy) / (silenceCount + 1);
            return false;
        }
    }
}
