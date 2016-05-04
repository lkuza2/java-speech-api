package com.darkprograms.speech.recognizer;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.ws.http.HTTPException;

import com.darkprograms.speech.util.StringUtil;

/**
 * This class uses Google's V2 Hook. The class is returns a chunked respones so listeners must be used.
 * The class also requires an API-Key (see Constructor) for details. This class is experimental and 
 * subject to change as we restructure the API.
 * @author Aaron Gokaslan (Skylion)
 */
public class RecognizerChunked {
	
	/**
	 * Google's API V2 URL
	 */
	private static final String GOOGLE_SPEECH_URL_V2 = "https://www.google.com/speech-api/v2/recognize";
	
	/**
	 * API-Key used for requests
	 */
	private final String API_KEY;
	
	/**
	 * The language code Google uses to determine the language
	 * Default value is "auto"
	 */
	private String language;
	
	/**
	 * Stores the Response Listeners
	 */
	private List<GSpeechResponseListener> responseListeners = new ArrayList<GSpeechResponseListener>();
	
	/**
	 * Constructor
	 * @param API_KEY The API-Key for Google's Speech API. An API key can be obtained by requesting
	 * one by following the process shown at this 
	 * <a href="http://www.chromium.org/developers/how-tos/api-keys">url</a>.
	 */
	public RecognizerChunked(String API_KEY){
		this.API_KEY = API_KEY;
		this.language = "auto";
	}
	
	/**
	 * Constructor
	 * @param API_KEY The API-Key for Google's Speech API. An API key can be obtained by requesting
	 * one by following the process shown at this 
	 * <a href="http://www.chromium.org/developers/how-tos/api-keys">url</a>.
	 * @param language The language you want to use (Iso code)
	 * Note: This function will most likely be deprecated.
	 */
	public RecognizerChunked(String API_KEY, String language){
		this(API_KEY);
		this.language = language;
	}
	
	/**
	 * The current language the Recognizer is set to use. Returns the ISO-Code otherwise,
	 * it may return "auto."
	 * @return The ISO-Code or auto if the language the is not specified.
	 */
	public String getLanguage(){
		return language;
	}
	
	/**
	 * Sets the language that the file should return.
	 * @param language The language as an ISO-Code
	 */
	public void setLanguage(String language){
		this.language = language;
	}
	
	/**
	 * Analyzes the file for speech
	 * @param infile The file you want to analyze for speech.
	 * @param sampleRate The sample rate of the audioFile.
	 * @throws IOException if something goes wrong reading the file.
	 */
	public void getRecognizedDataForFlac(File infile, int sampleRate) throws IOException{
		byte[] data = mapFileIn(infile);
		getRecognizedDataForFlac(data, sampleRate);
	}
	
	/**
	 * Analyzes the file for speech
	 * @param inFile The file you want to analyze for speech.
	 * @param sampleRate The sample rate of the audioFile.
	 * @throws IOException if something goes wrong reading the file.
	 */
	public void getRecognizedDataForFlac(String inFile, int sampleRate) throws IOException{
		getRecognizedDataForFlac(new File(inFile), sampleRate);
	}
	
	/**
	 * Recognizes the byte data.
	 * @param data
	 * @param sampleRate
	 */
	public void getRecognizedDataForFlac(byte[] data, int sampleRate){
		StringBuilder sb = new StringBuilder(GOOGLE_SPEECH_URL_V2);
		sb.append("?output=json");
		sb.append("&client=chromium");
		sb.append("&lang=" + language);
		sb.append("&key=" + API_KEY);
		String url = sb.toString();
		
		openHttpsPostConnection(url, data, sampleRate);
	}
	
	/**
	 * Opens a chunked response HTTPS line to the specified URL
	 * @param urlStr The URL string to connect for chunking
	 * @param data The data you want to send to Google. Speech files under 15 seconds long recommended.
	 * @param sampleRate The sample rate for your audio file.
	 */
	private void openHttpsPostConnection(final String urlStr, final byte[] data, final int sampleRate) {
		new Thread () {
			public void run() {
				HttpsURLConnection httpConn = null;
				ByteBuffer buff = ByteBuffer.wrap(data);
				byte[] destdata = new byte[2048];
				int resCode = -1;
				OutputStream out = null;
				try {
					URL url = new URL(urlStr);
					URLConnection urlConn = url.openConnection();
					if (!(urlConn instanceof HttpsURLConnection)) {
						throw new IOException ("URL must be HTTPS");
					}
					httpConn = (HttpsURLConnection)urlConn;
					httpConn.setAllowUserInteraction(false);
					httpConn.setInstanceFollowRedirects(true);
					httpConn.setRequestMethod("POST");
					httpConn.setDoOutput(true);
					httpConn.setChunkedStreamingMode(0); //TransferType: chunked
					httpConn.setRequestProperty("Content-Type", "audio/x-flac; rate=" + sampleRate);
					// this opens a connection, then sends POST & headers.
					out = httpConn.getOutputStream();
					//beyond 15 sec duration just simply writing the file
					// does not seem to work. So buffer it and delay to simulate
					// bufferd microphone delivering stream of speech
					// re: net.http.ChunkedOutputStream.java
					while(buff.remaining() >= destdata.length){
						buff.get(destdata);
						out.write(destdata);
					};
					byte[] lastr = new byte[buff.remaining()];
					buff.get(lastr, 0, lastr.length);
					out.write(lastr);
					out.close();
					resCode = httpConn.getResponseCode();
					if(resCode >= HttpURLConnection.HTTP_UNAUTHORIZED){//Stops here if Google doesn't like us/
						throw new HTTPException(HttpURLConnection.HTTP_UNAUTHORIZED);//Throws
					}
					String line;//Each line that is read back from Google.
					BufferedReader br =	new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
					while ((line = br.readLine( )) != null) {
						if(line.length()>19 && resCode > 100 && resCode < HttpURLConnection.HTTP_UNAUTHORIZED){
							GoogleResponse gr = new GoogleResponse();
							parseResponse(line, gr);
							fireResponseEvent(gr);
						}
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally {
					if(httpConn != null) {
						httpConn.disconnect();
					}
				}
			}
		}.start();
	}
	
	/**
	 * Converts the file into a byte[].
	 * @param infile The File you want to specify
	 * @return a byte array
	 * @throws IOException if something goes wrong reading the file.
	 */
	private byte[] mapFileIn(File infile) throws IOException{
		FileInputStream fis = new FileInputStream(infile);
		try{
			FileChannel fc = fis.getChannel(); // Get the file's size and then map it into memory
			int sz = (int)fc.size();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
			byte[] data2 = new byte[bb.remaining()];
			bb.get(data2);
			return data2;
		}
		finally{//Ensures resources are closed regardless of whether the action suceeded
			fis.close();
		}
	}

	/**
	 * Parses the response into a Google Response
	 * @param rawResponse The raw String you want to parse
	 * @param gr The GoogleResponse you want to parse into ti.
	 */
	private void parseResponse(String rawResponse, GoogleResponse gr){
		if(rawResponse == null || !rawResponse.contains("\"result\"")){ return; }
		if(rawResponse.contains("\"confidence\":")){
			String confidence = StringUtil.substringBetween(rawResponse, "\"confidence\":", "}");
			gr.setConfidence(confidence);
		}
		else{
			gr.setConfidence(String.valueOf(1d));
		}
		String array = StringUtil.trimString(rawResponse, "[", "]");
		if(array.contains("[")){
			array = StringUtil.trimString(array, "[", "]");
		}
		String[] parts = array.split(",");
		gr.setResponse(parseTranscript(parts[0]));
		for(int i = 1; i<parts.length; i++){
			gr.getOtherPossibleResponses().add(parseTranscript(parts[i]));
		}
	}
	
	/**
	 * Cleans up the transcript portion of the String
	 * @param s The string you want to process.
	 * @return The reformated string.
	 */
	private String parseTranscript(String s){
		String tmp = s.substring(s.indexOf(":")+1);
		if(s.endsWith("}")){
			tmp = tmp.substring(0, tmp.length()-1);
		}
		tmp = StringUtil.stripQuotes(tmp);
		return tmp;
	}

	/**
	 * Adds responseListener that triggers when a response from Google is recieved
	 * @param rl The response listener you want to add
	 */
	public synchronized void addResponseListener(GSpeechResponseListener rl){
		responseListeners.add(rl);
	}

	/**
	 * Removes the specified response listener
	 * @param rl The response listener
	 */
	public synchronized void removeResponseListener(GSpeechResponseListener rl){
		responseListeners.remove(rl);
	}
	
	/**
	 * Fires the response listener
	 * @param gr The GoogleResponse as the event object.
	 */
	private synchronized void fireResponseEvent(GoogleResponse gr){
		for(GSpeechResponseListener gl: responseListeners){
			gl.onResponse(gr);
		}
	}
	
}
