package com.darkprograms.speech.microphone;

import javax.sound.sampled.AudioFileFormat;
import com.darkprograms.speech.utility.*;

/********************************************************************************************
 * Microphone Analyzer class, detects pitch and volume while extending the microphone class.
 * Implemented as a precursor to a Voice Activity Detection (VAD) algorithm.
 * Currently can be used for audio data analysis.
 * Dependencies: FFT.java & Complex.java. Both found in the utility package.
 * @author Aaron Gokaslan
 ********************************************************************************************/

public class MicrophoneAnalyzer extends Microphone {

	/**
	 * Constructor
	 * @param fileType The file type you want to save in. FLAC recommended.
	 */
	public MicrophoneAnalyzer(AudioFileFormat.Type fileType){
		super(fileType);
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
    	byte[] data = getBytes(numOfBytes);
    	if(data==null)
    		return -1;
    	return calculateRMSLevel(data);
    }
    
    /**
     * Calculates the volume of AudioData which may be buffered data from a data-line.
     * @param audioData The byte[] you want to determine the volume of
     * @return the calculated volume of audioData
     */
	public static int calculateRMSLevel(byte[] audioData){
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
	 * Returns the a byte[] containing the specified number of bytes
	 * @param numOfBytes The length of the returned array.
	 * @return The specified array or null if it cannot.
	 */
	private byte[] getBytes(int numOfBytes){
		if(getTargetDataLine()!=null){
    		byte[] data = new byte[numOfBytes];
    		this.getTargetDataLine().read(data, 0, numOfBytes);
    		return data;
		}
		return null;//If data cannot be read, returns a null array.
	}
	

	/**
	 * Calculates the fundamental frequency. In other words, it calculates pitch,
	 * except pitch is far more subjective and subtle. Also note, that readings may occasionally,
	 * be in error due to the complex nature of sound. This feature is in Beta
	 * @return The frequency of the sound in Hertz.
	 */
	public int getFrequency(){
		try {
			return getFrequency(2048);
		} catch (Exception e) {
			//This will never happen. Ever...
			return -666;
		}
	}

	/**
	 * Calculates the frequency based off of the number of bytes. 
	 * CAVEAT: THE NUMBER OF BYTES MUST BE A MULTIPLE OF 2!!!
	 * @param numOfBytes The number of bytes which must be a multiple of 2!!!
	 * @return The calculated frequency in Hertz.
	 */
	public int getFrequency(int numOfBytes) throws Exception{
		if(getTargetDataLine() == null){
			return -1;
		}
		byte[] data = new byte[numOfBytes+1];//One byte is lost during conversion
    	this.getTargetDataLine().read(data, 0, numOfBytes);
		return getFrequency(data);
	}
	
	/**
	 * Calculates the frequency based off of the byte array,
	 * @param bytes The audioData you want to analyze
	 * @return The calculated frequency in Hertz.
	 */
	private int getFrequency(byte[] bytes){//This method requires an AudioFormat and cannot be static.
		double[] audioData = this.bytesToDoubleArray(bytes);
		Complex[] complex = new Complex[audioData.length];
		for(int i = 0; i<complex.length; i++){
			complex[i] = new Complex(audioData[i], 0);
		}
		Complex[] fftTransformed = FFT.fft(complex);
		return calculateFundamentalFrequency(fftTransformed);
	}
	
	/**
	 * Iterates through the transformed data to calculate the frequency
	 * This data is only as accurate as the bin size. (See getBinSize(int))
	 * Fundamental Frequency = index of max magnitude (that isn't a harmotic) * bin size
	 * @param fftData The data you want to analyze
	 * @return The frequency in Hertz
	 */
	private int calculateFundamentalFrequency(Complex[] fftData){
		int index = -1;
		double max = Double.MIN_VALUE;
		for(int i = 0; i<fftData.length/2; i++){
			Complex complex = fftData[i];
			double tmp = complex.getMagnitude();
			if(tmp>max && !isHarmonic(i,index)){
				max = tmp;
				index = i;
			}
		}
		return index*getFFTBinSize(fftData.length);
	}
	
	/**
	 * Determines whether or not a specific index constitutes a harmonic of a previous instance.
	 * Science: A harmonic frequency is a multiple of the fundamental frequency caused by interference.
	 * Note: Frequencies of an index 1 won't be treated as such since its frequency is so low.
	 * @param currentIndex The suspected harmonic frequency
	 * @param proposedIndex The suspected fundamental frequency
	 * @return True if it is a haromonic, false if it's not.
	 */
	private boolean isHarmonic(int currentIndex, int proposedIndex){
		return (currentIndex>2 && proposedIndex>2 && currentIndex%proposedIndex==0);
	}
	
	/**
	 * Calculates the FFTbin size based off the length of the the array
	 * Each FFTBin size represents the range of frequencies treated as one.
	 * For example, if the bin size is 5 then the algorithm is precise to within 5hz.
	 * Precondition: length cannot be 0.
	 * @param fftDataLength The length of the array used to feed the FFT algorithm
	 * @return FFTBin size
	 */
	private int getFFTBinSize(int fftDataLength){
		return (int)(getAudioFormat().getSampleRate()/fftDataLength+.5);
	}
	
	/**
	 * Converts bytes from a TargetDataLine into a double[] allowing the information to be read.
	 * NOTE: One byte is lost in the conversion so don't expect the arrays to be the same length!
	 * @param bufferData The buffer read in from the target data line
	 * @return The double[] that the buffer has been converted into.
	 */
	private double[] bytesToDoubleArray(byte[] bufferData){
	    final int bytesRecorded = bufferData.length;
		final int bytesPerSample = getAudioFormat().getSampleSizeInBits()/8; 
	    final double amplification = 100.0; // choose a number as you like
	    double[] micBufferData = new double[bytesRecorded - bytesPerSample +1];
	    for (int index = 0, floatIndex = 0; index < bytesRecorded - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
	        double sample = 0;
	        for (int b = 0; b < bytesPerSample; b++) {
	            int v = bufferData[index + b];
	            if (b < bytesPerSample - 1 || bytesPerSample == 1) {
	                v &= 0xFF;
	            }
	            sample += v << (b * 8);
	        }
	        double sample32 = amplification * (sample / 32768.0);
	        micBufferData[floatIndex] = sample32;
	        
	    }
	    return micBufferData;
	}
	
}
