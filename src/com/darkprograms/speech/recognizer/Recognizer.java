package com.darkprograms.speech.recognizer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class that submits FLAC audio and retrieves recognized text
 *
 * @author Luke Kuza, Duncan Jauncey
 */
public class Recognizer {

    /**
     * URL to POST audio data and retrieve results
     */
    private static final String GOOGLE_RECOGNIZER_URL = "https://www.google.com/speech-api/v1/recognize?xjerr=1&client=chromium";

    private boolean profanityFilter = true;
    private String language = null;

    public static final String LANG_US_ENGLISH = "en-US";
    public static final String LANG_UK_ENGLISH = "en-GB";

    /**
     * Constructor
     */
    public Recognizer() {
    }

    /**
     * Enable/disable Google's profanity filter (on by default).
     * @param profanityFilter
     */
    public void setProfanityFilter(boolean profanityFilter) {
        this.profanityFilter = profanityFilter;
    }

    /**
     * Language code.  This language code must match the language of the speech to be recognized. ex. en-US ru-RU
     * Setting this to null will make Google use it's own language detection.
     * This value is null by default.
     * @param language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Get recognized data from a Wave file.  This method will encode the wave file to a FLAC
     *
     * @param waveFile Wave file to recognize
     * @param maxResults Maximum number of results to return in response
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForWave(File waveFile, int maxResults) throws Exception {
        FlacEncoder flacEncoder = new FlacEncoder();
        File flacFile = new File(waveFile + ".flac");

        flacEncoder.convertWaveToFlac(waveFile, flacFile);

        String response = rawRequest(flacFile, maxResults);

        //Delete converted FLAC data
        flacFile.delete();

        GoogleResponse googleResponse = new GoogleResponse();
        parseResponse(response, googleResponse);
        return googleResponse;
    }

    /**
     * Get recognized data from a Wave file.  This method will encode the wave file to a FLAC
     *
     * @param waveFile Wave file to recognize
     * @param maxResults the maximum number of results to return in the response
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForWave(String waveFile, int maxResults) throws Exception {
        return getRecognizedDataForWave(new File(waveFile), maxResults);
    }

    /**
     * Get recognized data from a FLAC file.
     *
     * @param flacFile FLAC file to recognize
     * @param maxResults the maximum number of results to return in the response
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(File flacFile, int maxResults) throws Exception {
        String response = rawRequest(flacFile, maxResults);
        GoogleResponse googleResponse = new GoogleResponse();
        parseResponse(response, googleResponse);
        return googleResponse;
    }

    /**
     * Get recognized data from a FLAC file.
     *
     * @param flacFile FLAC file to recognize
     * @param maxResults the maximum number of results to return in the response
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(String flacFile, int maxResults) throws Exception {
        return getRecognizedDataForFlac(new File(flacFile), maxResults);
    }

    /**
     * Get recognized data from a Wave file.  This method will encode the wave file to a FLAC.
     * This method will automatically set the language to en-US, or English
     *
     * @param waveFile Wave file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForWave(File waveFile) throws Exception {
        return getRecognizedDataForWave(waveFile, 1);
    }

    /**
     * Get recognized data from a Wave file.  This method will encode the wave file to a FLAC.
     * This method will automatically set the language to en-US, or English
     *
     * @param waveFile Wave file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForWave(String waveFile) throws Exception {
        return getRecognizedDataForWave(waveFile, 1);
    }

    /**
     * Get recognized data from a FLAC file.
     * This method will automatically set the language to en-US, or English
     *
     * @param flacFile FLAC file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(File flacFile) throws Exception {
        return getRecognizedDataForFlac(flacFile, 1);
    }

    /**
     * Get recognized data from a FLAC file.
     * This method will automatically set the language to en-US, or English
     *
     * @param flacFile FLAC file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(String flacFile) throws Exception {
        return getRecognizedDataForFlac(flacFile, 1);
    }

    /**
     * Parses the raw response from Google
     *
     * @param rawResponse The raw, unparsed response from Google
     * @return Returns the parsed response.  Index 0 is response, Index 1 is confidence score
     */
    private void parseResponse(String rawResponse, GoogleResponse googleResponse) {
        if (!rawResponse.contains("utterance"))
            return;

        String array = substringBetween(rawResponse, "[", "]");
        String[] parts = array.split("}");
        
        boolean first = true;
        for( String s : parts ) {
            if( first ) {
                first = false;
                String utterancePart = s.split(",")[0];
                String confidencePart = s.split(",")[1];

                String utterance = utterancePart.split(":")[1];
                String confidence = confidencePart.split(":")[1];

                utterance = stripQuotes(utterance);
                confidence = stripQuotes(confidence);

                if( utterance.equals("null") ) {
                    utterance = null;
                }
                if( confidence.equals("null") ) {
                    confidence = null;
                }

                googleResponse.setResponse(utterance);
                googleResponse.setConfidence(confidence);
            } else {
                String utterance = s.split(":")[1];
                utterance = stripQuotes(utterance);
                if( utterance.equals("null") ) {
                    utterance = null;
                }
                googleResponse.getOtherPossibleResponses().add(utterance);
            }
        }
    }

    /**
     * Performs the request to Google with a file <br>
     * Request is buffered
     *
     * @param inputFile Input files to recognize
     * @return Returns the raw, unparsed response from Google
     * @throws Exception Throws exception if something went wrong
     */
    private String rawRequest(File inputFile, int maxResults) throws Exception {
        URL url;
        URLConnection urlConn;
        OutputStream outputStream;
        BufferedReader br;

        StringBuilder sb = new StringBuilder(GOOGLE_RECOGNIZER_URL);
        if( language != null ) {
            sb.append("&lang=");
            sb.append(language);
        }
        if( !profanityFilter ) {
            sb.append("&pfilter=0");
        }
        sb.append("&maxresults=");
        sb.append(maxResults);

        // URL of Remote Script.
        url = new URL(sb.toString());


        // Open New URL connection channel.
        urlConn = url.openConnection();

        // we want to do output.
        urlConn.setDoOutput(true);

        // No caching
        urlConn.setUseCaches(false);

        // Specify the header content type.
        urlConn.setRequestProperty("Content-Type", "audio/x-flac; rate=8000");

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
        br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

        String response = br.readLine();

        br.close();

        return response;

    }

    private String substringBetween(String s, String part1, String part2) {
        String sub = null;

        int i = s.indexOf(part1);
        int j = s.indexOf(part2, i + part1.length());

        if (i != -1 && j != -1) {
            int nStart = i + part1.length();
            sub = s.substring(nStart, j);
        }

        return sub;
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


}
