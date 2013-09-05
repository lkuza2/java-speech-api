package com.darkprograms.speech.microphone;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Microphone class that contains methods to capture audio from microphone
 *
 * @author Luke Kuza, Aaron Gokaslan
 */
public class Microphone {
	
    /**
     * TargetDataLine variable to receive data from microphone
     */
    private TargetDataLine targetDataLine;

    /**
     * Enum for current Microphone state
     */
    public enum CaptureState {
        PROCESSING_AUDIO, STARTING_CAPTURE, CLOSED
    }

    /**
     * Variable for enum
     */
    CaptureState state;

    /**
     * Variable for the audios saved file type
     */
    private AudioFileFormat.Type fileType;

    /**
     * Variable that holds the saved audio file
     */
    private File audioFile;

    /**
     * Gets the current state of Microphone
     *
     * @return PROCESSING_AUDIO is returned when the Thread is recording Audio and/or saving it to a file<br>
     *         STARTING_CAPTURE is returned if the Thread is setting variables<br>
     *         CLOSED is returned if the Thread is not doing anything/not capturing audio
     */
    public CaptureState getState() {
        return state;
    }

    /**
     * Sets the current state of Microphone
     *
     * @param state State from enum
     */
    private void setState(CaptureState state) {
        this.state = state;
    }

    public File getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(File audioFile) {
        this.audioFile = audioFile;
    }

    public AudioFileFormat.Type getFileType() {
        return fileType;
    }

    public void setFileType(AudioFileFormat.Type fileType) {
        this.fileType = fileType;
    }

    public TargetDataLine getTargetDataLine() {
        return targetDataLine;
    }

    public void setTargetDataLine(TargetDataLine targetDataLine) {
        this.targetDataLine = targetDataLine;
    }
    
    
    /**
     * Constructor
     *
     * @param fileType File type to save the audio in<br>
     *                 Example, to save as WAVE use AudioFileFormat.Type.WAVE
     */
    public Microphone(AudioFileFormat.Type fileType) {
        setState(CaptureState.CLOSED);
        setFileType(fileType);
    }


    /**
     * Captures audio from the microphone and saves it a file
     *
     * @param audioFile The File to save the audio to
     * @throws Exception Throws an exception if something went wrong
     */
    public void captureAudioToFile(File audioFile) throws Exception {
        setState(CaptureState.STARTING_CAPTURE);
        setAudioFile(audioFile);

        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
        setTargetDataLine((TargetDataLine) AudioSystem.getLine(dataLineInfo));


        //Get Audio
        new Thread(new CaptureThread()).start();


    }

    /**
     * Captures audio from the microphone and saves it a file
     *
     * @param audioFile The fully path (String) to a file you want to save the audio in
     * @throws Exception Throws an exception if something went wrong
     */
    public void captureAudioToFile(String audioFile) throws Exception {
        setState(CaptureState.STARTING_CAPTURE);
        File file = new File(audioFile);
        setAudioFile(file);

        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
        setTargetDataLine((TargetDataLine) AudioSystem.getLine(dataLineInfo));


        //Get Audio
        new Thread(new CaptureThread()).start();


    }

    /**
     * Gets the volume of the microphone input
     * Interval is 100ms so allow 100ms for this method to run in your code or specify smaller interval.
     * @return The volume of the microphone input or -1 if data-line is not available
     */
    public int getAudioVolume(){
    	return getAudioVolume(100);
    }
    
    /**
     * Gets the volume of the microphone input
     * @param interval: The length of time you would like to calculate the volume over in milliseconds.
     * @return The volume of the microphone input or -1 if data-line is not available. 
     */    
    public int getAudioVolume(int interval){
    	return calculateAudioVolume(this.getNumOfBytes(interval/1000d));
    }
    
    /**
     * Gets the volume of microphone input
     * @param numOfBytes The number of bytes you want for volume interpretation
     * @return The volume over the specified number of bytes or -1 if data-line is unavailable.
     */
    private int calculateAudioVolume(int numOfBytes){
    	if(getTargetDataLine()!=null){
    		byte[] data = new byte[numOfBytes];
    		this.getTargetDataLine().read(data, 0, numOfBytes);
    		return calculateRMSLevel(data);
    	}
		else{
			return -1;
		}
    }
    
    /**
     * Calculates the volume of AudioData which may be buffered data from a data-line
     * @param audioData The byte[] you want to determine the volume of
     * @return the calculated volume of audioData
     */
	private int calculateRMSLevel(byte[] audioData){
		long lSum = 0;
		for(int i=0; i<audioData.length; i++)
			lSum = lSum + audioData[i];

		double dAvg = lSum / audioData.length;

		double sumMeanSquare = 0d;
		for(int j=0; j<audioData.length; j++)
			sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);

		double averageMeanSquare = sumMeanSquare / audioData.length;
		return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
	}
	
	/**
	 * Returns the number of bytes over interval for useful when figuring out how long to record.
	 * @param seconds The length in seconds
	 * @return the number of bytes the microphone will save.
	 */
	public int getNumOfBytes(int seconds){
		return getNumOfBytes((double)seconds);
	}
	
	/**
	 * Returns the number of bytes over interval for useful when figuring out how long to record.
	 * @param seconds The length in seconds
	 * @return the number of bytes the microphone will output over the specified time.
	 */
	public int getNumOfBytes(double seconds){
		return (int)(seconds*getAudioFormat().getSampleRate()*getAudioFormat().getFrameSize()+.5);
	}
	
    /**
     * The audio format to save in
     *
     * @return Returns AudioFormat to be used later when capturing audio from microphone
     */
    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    /**
     * Close the microphone capture, saving all processed audio to the specified file.<br>
     * If already closed, this does nothing
     */
    public void close() {
        if (getState() == CaptureState.CLOSED) {
        } else {
            getTargetDataLine().stop();
            getTargetDataLine().close();
        }
    }

    /**
     * Thread to capture the audio from the microphone and save it to a file
     */
    private class CaptureThread implements Runnable {

        /**
         * Run method for thread
         */
        public void run() {
            try {
                setState(CaptureState.PROCESSING_AUDIO);
                AudioFileFormat.Type fileType = getFileType();
                File audioFile = getAudioFile();
                getTargetDataLine().open(getAudioFormat());
                getTargetDataLine().start();
                AudioSystem.write(new AudioInputStream(getTargetDataLine()), fileType, audioFile);
                setState(CaptureState.CLOSED);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
