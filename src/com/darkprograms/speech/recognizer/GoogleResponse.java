package com.darkprograms.speech.recognizer;

/**
 * Class that holds the response and confidence of a Google recognizer request
 *
 * @author Luke Kuza
 */
public class GoogleResponse {

    /**
     * Variable that holds the response
     */
    private String response;
    /**
     * Variable that holds the confidence score
     */
    private String confidence;

    /**
     * Constructor
     */
    public GoogleResponse() {

    }


    /**
     * Gets the response text of what was said in the submitted Audio to Google
     *
     * @return String representation of what was said
     */
    public String getResponse() {
        return response;
    }

    /**
     * Set the response
     *
     * @param response The response
     */
    protected void setResponse(String response) {
        this.response = response;
    }

    /**
     * Gets the confidence score for the specific request
     *
     * @return The confidence score, ex .922343324323
     */
    public String getConfidence() {
        return confidence;
    }

    /**
     * Set the confidence score for this request
     *
     * @param confidence The confidence score
     */
    protected void setConfidence(String confidence) {
        this.confidence = confidence;
    }


}
