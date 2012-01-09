package com.darkprograms.speech.recognizer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class that submits FLAC audio and retrieves recognized text
 */
public class Recognizer {

    /**
     * URL to POST audio data and retrieve results
     */
    private static final String GOOGLE_RECOGNIZER_URL = "https://www.google.com/speech-api/v1/recognize?xjerr=1&client=chromium&lang=en-US";

    /**
     * Constructor
     */
    public Recognizer() {

    }

    /**
     * Get recognized data from a Wave file.  This method will encode the wave file to a FLAC
     *
     * @param waveFile Wave file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForWave(File waveFile) throws Exception {
        FlacEncoder flacEncoder = new FlacEncoder();
        File flacFile = new File(waveFile + ".flac");

        flacEncoder.convertWaveToFlac(waveFile, flacFile);

        String response = rawRequest(flacFile);

        //Delete converted FLAC data
        flacFile.delete();

        String[] parsedResponse = parseResponse(response);


        GoogleResponse googleResponse = new GoogleResponse();


        googleResponse.setResponse(parsedResponse[0]);
        googleResponse.setConfidence(parsedResponse[1]);

        return googleResponse;
    }

    /**
     * Get recognized data from a Wave file.  This method will encode the wave file to a FLAC
     *
     * @param waveFile Wave file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForWave(String waveFile) throws Exception {
        return getRecognizedDataForWave(new File(waveFile));
    }

    /**
     * Get recognized data from a FLAC file.
     *
     * @param flacFile FLAC file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(File flacFile) throws Exception {
        String response = rawRequest(flacFile);
        String[] parsedResponse = parseResponse(response);

        GoogleResponse googleResponse = new GoogleResponse();


        googleResponse.setResponse(parsedResponse[0]);
        googleResponse.setConfidence(parsedResponse[1]);

        return googleResponse;
    }

    /**
     * Get recognized data from a FLAC file.
     *
     * @param flacFile FLAC file to recognize
     * @return Returns a GoogleResponse, with the response and confidence score
     * @throws Exception Throws exception if something goes wrong
     */
    public GoogleResponse getRecognizedDataForFlac(String flacFile) throws Exception {
        return getRecognizedDataForFlac(new File(flacFile));
    }

    /**
     * Parses the raw response from Google
     *
     * @param rawResponse The raw, unparsed response from Google
     * @return Returns the parsed response.  Index 0 is response, Index 1 is confidence score
     */
    private String[] parseResponse(String rawResponse) {
        String[] parsedResponse = new String[2];

        String[] strings = rawResponse.split(":");

        parsedResponse[0] = strings[4].split("\"")[1];
        parsedResponse[1] = strings[5].replace("}]}", "");

        return parsedResponse;
    }

    /**
     * Performs the request to Google with a file <br>
     * Request is buffered
     *
     * @param inputFile Input files to recognize
     * @return Returns the raw, unparsed response from Google
     * @throws Exception Throws exception if something went wrong
     */
    private String rawRequest(File inputFile) throws Exception {
        URL url;
        URLConnection urlConn;
        OutputStream outputStream;
        BufferedReader br;

        // URL of Remote Script.
        url = new URL(GOOGLE_RECOGNIZER_URL);

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


}
