package com.darkprograms.speech.recognizer;

import net.sourceforge.javaflacencoder.FLACEncoder;
import net.sourceforge.javaflacencoder.FLACFileOutputStream;
import net.sourceforge.javaflacencoder.StreamConfiguration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*************************************************************************************************************
 * Class that contains methods to encode Wave files to FLAC files
 * THIS IS THANKS TO THE javaFlacEncoder Project created here: http://sourceforge.net/projects/javaflacencoder/
 ************************************************************************************************************/
public class FlacEncoder {

    /**
     * Constructor
     */
    public FlacEncoder() {

    }

    /**
     * Converts a wave file to a FLAC file(in order to POST the data to Google and retrieve a response) <br>
     * Sample Rate is 8000 by default
     *
     * @param inputFile  Input wave file
     * @param outputFile Output FLAC file
     */
    public void convertWaveToFlac(File inputFile, File outputFile) {


        StreamConfiguration streamConfiguration = new StreamConfiguration();
        streamConfiguration.setSampleRate(8000);
        streamConfiguration.setBitsPerSample(16);
        streamConfiguration.setChannelCount(1);


        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputFile);
            AudioFormat format = audioInputStream.getFormat();

            int frameSize = format.getFrameSize();

            FLACEncoder flacEncoder = new FLACEncoder();
            FLACFileOutputStream flacOutputStream = new FLACFileOutputStream(outputFile);

            flacEncoder.setStreamConfiguration(streamConfiguration);
            flacEncoder.setOutputStream(flacOutputStream);

            flacEncoder.openFLACStream();

            int frameLength = (int) audioInputStream.getFrameLength();
            if(frameLength <= AudioSystem.NOT_SPECIFIED){
            	frameLength = 16384;//Arbitrary file size
            }
            int[] sampleData = new int[frameLength];
            byte[] samplesIn = new byte[frameSize];

            int i = 0;

            while (audioInputStream.read(samplesIn, 0, frameSize) != -1) {
                if (frameSize != 1) {
                    ByteBuffer bb = ByteBuffer.wrap(samplesIn);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    short shortVal = bb.getShort();
                    sampleData[i] = shortVal;
                } else {
                    sampleData[i] = samplesIn[0];
                }

                i++;
            }

            sampleData = truncateNullData(sampleData, i);
            
            flacEncoder.addSamples(sampleData, i);
            flacEncoder.encodeSamples(i, false);
            flacEncoder.encodeSamples(flacEncoder.samplesAvailableToEncode(), true);

            audioInputStream.close();
            flacOutputStream.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Converts a wave file to a FLAC file(in order to POST the data to Google and retrieve a response) <br>
     * Sample Rate is 8000 by default
     *
     * @param inputFile  Input wave file
     * @param outputFile Output FLAC file
     */
    public void convertWaveToFlac(String inputFile, String outputFile) {
        convertWaveToFlac(new File(inputFile), new File(outputFile));
    }

    /**
     * Used for when the frame length is unknown to shorten the array to prevent huge blank end space 
     * @param sampleData The int[] array you want to shorten
     * @param index The index you want to shorten it to
     * @return The shortened array
     */
    private int[] truncateNullData(int[] sampleData, int index){
    	if(index == sampleData.length) return sampleData;
    	int[] out = new int[index];
    	for(int i = 0; i<index; i++){
    		out[i] = sampleData[i];
    	}
    	return out;
    }

}
