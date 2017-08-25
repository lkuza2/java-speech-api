package com.darkprograms.speech.recognizer;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.json.*;

/***************************************************************
 * Class that submits FLAC audio and retrieves recognized text
 *
 * @author Luke Kuza, Duncan Jauncey, Aaron Gokaslan
 **************************************************************/
public class Recognizer {

    /**
     * URL to POST audio data and retrieve results
     */
    // "http://www.google.com/speech-api/v2/recognize?lang=en-us&key=ADD_YOUR_KEY_HERE&output=json" 
    private static final String GOOGLE_RECOGNIZER_URL = "http://www.google.com/speech-api/v2/recognize?client=chromium&output=json";

    private boolean profanityFilter = true;
    private String language = null;
    private String apikey = null;
    
    /**
     * Constructor
     * @param language
     * @param apikey
     */
    public Recognizer(String language, String apikey) {
        this.language = language; 
        this.apikey = apikey;
    }
    
    /**
     * Constructor
     * @param language The Languages class for the language you want to designate
     * @param apikey
     */
     public Recognizer(Languages language, String apikey){
     	this.language = language.getLanguageCode();
        this.apikey = apikey;
     }
    
    /**
     * Constructor
     * @param profanityFilter
     * @param apikey
     */
    public Recognizer(boolean profanityFilter, String apikey){
    	this.profanityFilter = profanityFilter;
        this.apikey = apikey;
    }
    
    /**
     * Constructor
     * @param language
     * @param profanityFilter
     * @param apikey
     */
    public Recognizer(String language, boolean profanityFilter, String apikey){
    	this.language = language;
    	this.profanityFilter = profanityFilter;
        this.apikey = apikey;
    }
    
   /**
     * Constructor
     * @param language
     * @param profanityFilter
     * @param apikey
     */
     public Recognizer(Languages language, boolean profanityFilter, String apikey){
     	this.language = language.getLanguageCode();
     	this.profanityFilter = profanityFilter;
        this.apikey = apikey;
     }
    
    /**
     * Language: Contains all supported languages for Google Speech to Text. 
     * Setting this to null will make Google use it's own language detection.
     * This value is null by default.
     * @param language
     */
    public void setLanguage(Languages language) {
        this.language = language.getLanguageCode();
    }
    
    /**Language code.  This language code must match the language of the speech to be recognized. ex. en-US ru-RU
     * This value is null by default.
     * @param language The language code.
     */
    public void setLanguage(String language) {
    	this.language = language;
    }
    
    /**
     * Returns the state of profanityFilter
     * which enables/disables Google's profanity filter (on by default).
     * @return profanityFilter
     */
    public boolean getProfanityFilter(){
    	return profanityFilter;
    }
    
    /**
     * Language code.  This language code must match the language of the speech to be recognized. ex. en-US ru-RU
     * This value is null by default.
     * @return language the Google language
     */
    public String getLanguage(){
    	return language;
    }

    public String getApiKey() {
        return apikey;
    }

    public void setApiKey(String apikey) {
        this.apikey = apikey;
    }

    /**
     * Get recognized data from a Wave file.  This method will encode the wave file to a FLAC file
     *
     * @param waveFile Wave file to recognize
     * @param maxResults Maximum number of results to return in response
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws IOException Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForWave(File waveFile, int maxResults) throws IOException{
        FlacEncoder flacEncoder = new FlacEncoder();
        File flacFile = new File(waveFile + ".flac");

        flacEncoder.convertWaveToFlac(waveFile, flacFile);
        
        GoogleResponse googleResponse = getRecognizedDataForFlac(flacFile, maxResults, 8000);
        
        //Delete converted FLAC data
        flacFile.delete();
        return googleResponse;
    }

    /**
     * Get recognized data from a Wave file.  This method will encode the wave file to a FLAC
     *
     * @param waveFile Wave file to recognize
     * @param maxResults the maximum number of results to return in the response
     * NOTE: Sample rate of file must be 8000 unless a custom sample rate is specified.
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws IOException Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForWave(String waveFile, int maxResults) throws IOException {
        return getRecognizedDataForWave(new File(waveFile), maxResults);
    }

    /**
     * Get recognized data from a FLAC file.
     *
     * @param flacFile FLAC file to recognize
     * @param maxResults the maximum number of results to return in the response
     * NOTE: Sample rate of file must be 8000 unless a custom sample rate is specified.
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws IOException Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(File flacFile, int maxResults) throws IOException {
    	return getRecognizedDataForFlac(flacFile, maxResults, 8000);
    }

    /**
     * Get recognized data from a FLAC file.
     *
     * @param flacFile FLAC file to recognize
     * @param maxResults the maximum number of results to return in the response
     * @param sampleRate The sampleRate of the file. Default is 8000.
     * @return GoogleResponse with the response and confidence score
     * @throws IOException if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(File flacFile, int maxResults, int sampleRate) throws IOException{
        String [] response = rawRequest(flacFile, maxResults, sampleRate);
        GoogleResponse googleResponse = new GoogleResponse();
        parseResponse(response, googleResponse);
        return googleResponse;
    }

    /**
     * Get recognized data from a FLAC file.
     *
     * @param flacFile FLAC file to recognize
     * @param maxResults the maximum number of results to return in the response
     * @param sampleRate The sampleRate of the file. Default is 8000.
     * @return GoogleResponse, with the response and confidence score
     * @throws IOException if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(String flacFile, int maxResults, int sampleRate) throws IOException{
    	return getRecognizedDataForFlac(new File(flacFile), maxResults, sampleRate);
    }
    
    /**
     * Get recognized data from a FLAC file.
     *
     * @param flacFile FLAC file to recognize
     * @param maxResults the maximum number of results to return in the response
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws IOException Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(String flacFile, int maxResults) throws IOException {
        return getRecognizedDataForFlac(new File(flacFile), maxResults);
    }

    /**
     * Get recognized data from a Wave file.  This method will encode the wave file to a FLAC.
     * This method will automatically set the language to en-US, or English
     *
     * @param waveFile Wave file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws IOException Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForWave(File waveFile) throws IOException {
        return getRecognizedDataForWave(waveFile, 1);
    }

    /**
     * Get recognized data from a Wave file.  This method will encode the wave file to a FLAC.
     * This method will automatically set the language to en-US, or English
     *
     * @param waveFile Wave file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws IOException Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForWave(String waveFile) throws IOException {
        return getRecognizedDataForWave(waveFile, 1);
    }

    /**
     * Get recognized data from a FLAC file.
     * This method will automatically set the language to en-US, or English
     *
     * @param flacFile FLAC file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws IOException Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(File flacFile) throws IOException {
        return getRecognizedDataForFlac(flacFile, 1);
    }

    /**
     * Get recognized data from a FLAC file.
     * This method will automatically set the language to en-US, or English
     *
     * @param flacFile FLAC file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws IOException Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(String flacFile) throws IOException {
        return getRecognizedDataForFlac(flacFile, 1);
    }

    /**
     * Parses the raw response from Google
     *
     * @param rawResponse The raw, unparsed response from Google
     * @return Returns the parsed response in the form of a Google Response.
     */
    private void parseResponse(String[] rawResponse, GoogleResponse googleResponse) {

        for(String s : rawResponse) {
            JSONObject jsonResponse = new JSONObject(s);
            JSONArray jsonResultArray = jsonResponse.getJSONArray("result");
            for(int i = 0; i < jsonResultArray.length(); i++) {
                JSONObject jsonAlternativeObject = jsonResultArray.getJSONObject(i);
                JSONArray jsonAlternativeArray = jsonAlternativeObject.getJSONArray("alternative");
                double prevConfidence = 0;
                for(int j = 0; j < jsonAlternativeArray.length(); j++) {
                    JSONObject jsonTranscriptObject = jsonAlternativeArray.getJSONObject(j);
                    String transcript = jsonTranscriptObject.optString("transcript", "");
                    double confidence = jsonTranscriptObject.optDouble("confidence", 0.0);
                    if(confidence > prevConfidence) {
                        googleResponse.setResponse(transcript);
                        googleResponse.setConfidence(String.valueOf(confidence));
                        prevConfidence = confidence;
                    } else
                        googleResponse.getOtherPossibleResponses().add(transcript);
                }
            }
        }
    }

    /**
     * Performs the request to Google with a file <br>
     * Request is buffered
     *
     * @param inputFile Input files to recognize
     * @return Returns the raw, unparsed response from Google
     * @throws IOException Throws exception if something went wrong
     */
    private String[] rawRequest(File inputFile, int maxResults, int sampleRate) throws IOException{
        URL url;
        URLConnection urlConn;
        OutputStream outputStream;
        BufferedReader br;

        StringBuilder sb = new StringBuilder(GOOGLE_RECOGNIZER_URL);
        if( language != null ) {
            sb.append("&lang=");
            sb.append(language);
        }
        else{
        	sb.append("&lang=auto");
        }
        if(apikey != null) {
                sb.append("&key=");
                sb.append(apikey);
        }

        if( !profanityFilter ) {
            sb.append("&pfilter=0");
        }
        sb.append("&maxresults=");
        sb.append(maxResults);

        // URL of Remote Script.
        url = new URL(sb.toString());
        // System.out.println("Recognizer.rawRequest(): url=" + url);

        // Open New URL connection channel.
        urlConn = url.openConnection();

        // we want to do output.
        urlConn.setDoOutput(true);

        // No caching
        urlConn.setUseCaches(false);

        // Specify the header content type.
        urlConn.setRequestProperty("Content-Type", "audio/x-flac; rate=" + sampleRate);
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) "
        		+ "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.52 Safari/537.36");

        // Send POST output.
        outputStream = urlConn.getOutputStream();

        FileInputStream fileInputStream = new FileInputStream(inputFile);

        byte[] buffer = new byte[256];

        while ((fileInputStream.read(buffer, 0, 256)) != -1) {
            outputStream.write(buffer, 0, 256);
        }

        fileInputStream.close();
        outputStream.close();

        // Get response data.
        br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), Charset.forName("UTF-8")));

        List<String> completeResponse = new ArrayList<String>();
        String response = br.readLine();
        while(response != null) {
            completeResponse.add(response);
            response = br.readLine();
        }

        br.close();

        // System.out.println("Recognizer.rawRequest() -> completeResponse = " + completeResponse);
        return completeResponse.toArray(new String[completeResponse.size()]);
    }
}
