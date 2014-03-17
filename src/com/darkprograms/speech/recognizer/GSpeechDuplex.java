package com.darkprograms.speech.recognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * A class for using Google's Duplex Speech API. Allows for continuous recognition. Requires an API-Key.
 * A duplex API opens two connections. One to an upstream and one to a downstream. The system allows
 * for continuous chunking on both up and downstream. This, in turn, allows for Google to return data
 * as data is sent to it. For this reason, this class uses listeners.
 * @author Skylion (Aaron Gokaslan), Robert Rowntree.
 */
public class GSpeechDuplex{

	/**
	 * Minimum value for ID
	 */
	private static final long MIN = 10000000;

	/**
	 * Maximum value for ID
	 */
	private static final long MAX = 900000009999999L;

	/**
	 * The base URL
	 */
	private static final String GOOGLE_DUPLEX_SPEECH_BASE = "https://www.google.com/speech-api/full-duplex/v1/";

	/**
	 * Stores listeners
	 */
	private List<GSpeechResponseListener> responseListeners = new ArrayList<GSpeechResponseListener>();
	
	/**
	 * User defined API-KEY
	 */
	private String API_KEY = "";
	
	/**
	 * User-defined language
	 */
	private String language = "auto";
	
	/**
	 * Constructor
	 * @param API_KEY The API-Key for Google's Speech API. An API key can be obtained by requesting
	 * one by following the process shown at this 
	 * <a href="http://www.chromium.org/developers/how-tos/api-keys">url</a>.
	 */
	public GSpeechDuplex(String API_KEY){
		this.API_KEY = API_KEY;
	}
	
	/**
	 * Temporary will be deprecated before release
	 */
	public String getLanguage(){
		return language;
	}
	
	/**
	 * Temporary will be deprecated before release
	 */
	public void setLanguage(String language){
		this.language = language;
	}
	
	/**
	 * Send a FLAC file with the specified sampleRate to the Duplex API
	 * @param flacFile The file you wish to upload.
	 * NOTE: Segment the file if duration is greater than 15 seconds.
	 * @param sampleRate The sample rate of the file.
	 * @throws IOException If something has gone wrong with reading the file
	 */
	public void recognize(File flacFile, int sampleRate) throws IOException{
		recognize(mapFileIn(flacFile), sampleRate);
	}
	
	/**
	 * Send a byte[] to the URL with a specified sampleRate.
	 * NOTE: The byte[] should contain no more than 15 seconds of audio.
	 * @param data The byte[] you want to send.
	 * @param sampleRate The sample rate of said byte array.
	 */
	public void recognize(byte[] data, int sampleRate){

		//Generates a unique ID for the response. 
		final long PAIR = MIN + (long)(Math.random() * ((MAX - MIN) + 1L));

		//Generates the Downstream URL
		final String API_DOWN_URL = GOOGLE_DUPLEX_SPEECH_BASE + "down?maxresults=1&pair=" + PAIR;

		//Generates the Upstream URL
		final String API_UP_URL = GOOGLE_DUPLEX_SPEECH_BASE + 
				"up?lang=" + language + "&lm=dictation&client=chromium&pair=" + PAIR + 
				"&key=" + API_KEY ;

		//Opens downChannel
		this.downChannel(API_DOWN_URL);
		//Opens upChannel
		this.upChannel(API_UP_URL, data, sampleRate);
	}
	
	/**
	 * This code opens a new Thread that connects to the downstream URL. Due to threading,
	 * the best way to handle this is through the use of listeners.
	 * @param The URL you want to connect to.
	 */
	private void downChannel(String urlStr) {
		final String url = urlStr;
		new Thread () {
			public void run() {
				// handler for DOWN channel http response stream - httpsUrlConn
				// response handler should manage the connection.... ??
				// assign a TIMEOUT Value that exceeds by a safe factor
				// the amount of time that it will take to write the bytes
				// to the UPChannel in a fashion that mimics a liveStream
				// of the audio at the applicable Bitrate. BR=sampleRate * bits per sample
				// Note that the TLS session uses "* SSLv3, TLS alert, Client hello (1): "
				// to wake up the listener when there are additional bytes.
				// The mechanics of the TLS session should be transparent. Just use
				// httpsUrlConn and allow it enough time to do its work.
				Scanner inStream = openHttpsConnection(url);
				if(inStream == null){
					//ERROR HAS OCCURED
				}
				while(inStream.hasNextLine()){
					String response = inStream.nextLine();
					if(response.length()>17){//Prevents blank responses from Firing
						GoogleResponse gr = new GoogleResponse();
						parseResponse(response, gr);
						fireResponseEvent(gr);
					}
				}
			}
		}.start();
	}


	/**
	 * Used to initiate the URL chunking for the upChannel. 
	 * @param urlStr The URL string you want to upload 2
	 * @param data The data you want to send to the URL
	 * @param sampleRate The specified sample rate of the data.
	 */
	private void upChannel(String urlStr, byte[] data, int sampleRate) {
		final String murl = urlStr;
		final byte[] mdata = data;
		final int mSampleRate = sampleRate;
		new Thread () {
			public void run() {
				openHttpsPostConnection(murl, mdata, mSampleRate);
				//Google does not return data via this URL
			}
		}.start();
	}
	
	/**
	 * Opens a HTTPS connection to the specified URL string
	 * @param urlStr The URL you want to visit
	 * @return The Scanner to access aforementioned data.
	 */
	private Scanner openHttpsConnection(String urlStr) {
		int resCode = -1;
		try {
			URL url = new URL(urlStr);
			URLConnection urlConn = url.openConnection();
			if (!(urlConn instanceof HttpsURLConnection)) {
				throw new IOException ("URL is not an Https URL");
			}
			HttpsURLConnection httpConn = (HttpsURLConnection)urlConn;
			httpConn.setAllowUserInteraction(false);
			// TIMEOUT is required
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");

			httpConn.connect();
			resCode = httpConn.getResponseCode();
			if (resCode == HttpsURLConnection.HTTP_OK) {
				return new Scanner(httpConn.getInputStream());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Opens a chunked HTTPS post connection and returns a Scanner with incoming data from Google Server
	 * Used for to get UPStream
	 * Chunked HTTPS ensures unlimited file size.
	 * @param urlStr The String for the URL
	 * @param data The data you want to send the server 
	 * @param sampleRate The sample rate of the flac file.
	 * @return A Scanner to access the server response. (Probably will never be used)
	 */
	private Scanner openHttpsPostConnection(String urlStr, byte[] data, int sampleRate){
		byte[] mextrad = data;
		int resCode = -1;
		OutputStream out = null;
		// int http_status;
		try {
			URL url = new URL(urlStr);
			URLConnection urlConn = url.openConnection();
			if (!(urlConn instanceof HttpsURLConnection)) {
				throw new IOException ("URL is not an Https URL");
			}
			HttpsURLConnection httpConn = (HttpsURLConnection)urlConn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setChunkedStreamingMode(0);
			httpConn.setRequestProperty("Content-Type", "audio/x-flac; rate=" + sampleRate);
			// also worked with ("Content-Type", "audio/amr; rate=8000");
			httpConn.connect();
			try {
				// this opens a connection, then sends POST & headers.
				out = httpConn.getOutputStream();
				//Note : if the audio is more than 15 seconds
				// dont write it to UrlConnInputStream all in one block as this sample does.
				// Rather, segment the byteArray and on intermittently, sleeping thread
				// supply bytes to the urlConn Stream at a rate that approaches
				// the bitrate ( =30K per sec. in this instance ).
				out.write(mextrad); // one big block supplied instantly to the underlying chunker wont work for duration > 15 s.
				// do you need the trailer?
				// NOW you can look at the status.
				resCode = httpConn.getResponseCode();
				if (resCode / 100 != 2) {
					System.out.println("ERROR");
				}
			} catch (IOException e) {

			}
			if (resCode == HttpsURLConnection.HTTP_OK) {
				return new Scanner(httpConn.getInputStream());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Converts the file into a byte[]. Also Android compatible. :)
	 * @param The File you want to get the byte[] from.
	 * @return The byte[]
	 * @throws IOException if something goes wrong in reading the file. 
	 */
	private byte[] mapFileIn(File infile) throws IOException{
		FileInputStream fis = null;
		FileChannel fc = null;
		try{
			fis = new FileInputStream(infile);
			fc = fis.getChannel(); // Get the file's size and then map it into memory
			int sz = (int)fc.size();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
			byte[] data2 = new byte[bb.remaining()];
			bb.get(data2);
			fc.close();
			fis.close();
			return data2;
		}
		finally{//Ensures resources are closed regardless of whether the action suceeded
			try{
				fis.close();
				fc.close();
			}
			catch(Exception e){
				//Does nothing
			}
		}
	}

	/**
	 * Parses the String into a GoogleResponse object
	 * @param rawResponse The String you want to parse
	 * @param gr the GoogleResponse object to save the data into.
	 */
	private void parseResponse(String rawResponse, GoogleResponse gr){
		if(rawResponse == null || !rawResponse.contains("\"result\"")
				|| rawResponse.equals("{\"result\":[]}")){ return; }
		if(rawResponse.contains("\"confidence\":")){
			String confidence = getBetween(rawResponse, "\"confidence\":", "}");
			gr.setConfidence(confidence);
		}
		else{
			gr.setConfidence(String.valueOf(1d));
		}
		String array = getBetween(rawResponse, "[", "]");
		if(array.contains("[")){
			array = getBetween(array, "[", "]");
		}
		String[] parts = array.split(",");
		gr.setResponse(parseTranscript(parts[0]));
		for(int i = 1; i<parts.length; i++){
			gr.getOtherPossibleResponses().add(parseTranscript(parts[i]));
		}
	}
	
	/**
	 * Parses each individual "transcript" phrase
	 * @param The string fragment to parse
	 * @return The parsed String
	 */
	private String parseTranscript(String s){
		String tmp = s.substring(s.indexOf(":")+1);
		if(s.endsWith("}")){
			tmp = tmp.substring(0, tmp.length()-1);
		}
		tmp = stripQuotes(tmp);
		return tmp;
	}
	
    private String stripQuotes(String s) {
        int start = 0;
        if( s.startsWith("\"") ) {
            start = 1;
        }
        int end = s.length();
        if( s.endsWith("\"") ) {
            end = s.length() - 1;
        }
        return s.substring(start, end);
    }
	
	private String getBetween(String s, String part1, String part2){
		String tmp = s.substring(s.indexOf(part1) + part1.length() + 1);
		tmp = tmp.substring(0, tmp.lastIndexOf(part2));
		return tmp;
	}
	
	/**
	 * Adds GSpeechResponse Listeners that fire when Google sends a response.
	 * @param The Listeners you want to add
	 */
	public synchronized void addResponseListener(GSpeechResponseListener rl){
		responseListeners.add(rl);
	}

	/**
	 * Removes GSpeechResponseListeners that fire when Google sends a response.
	 * @param rl
	 */
	public synchronized void removeResponseListener(GSpeechResponseListener rl){
		responseListeners.remove(rl);
	}

	/**
	 * Fires responseListeners
	 * @param gr The Google Response (in this case the response event).
	 */
	private synchronized void fireResponseEvent(GoogleResponse gr){
		for(GSpeechResponseListener gl: responseListeners){
			gl.onResponse(gr);
		}
	}
}