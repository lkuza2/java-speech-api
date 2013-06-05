package com.darkprograms.speech.recognizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that holds the response and confidence of a Google recognizer request
 *
 * @author Luke Kuza, Duncan Jauncey, Víctor Martín Molina
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
     * List that holds other possible responses for this request.
     */
    private List<String> otherPossibleResponses = new ArrayList(20);

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

    /**
     * Get other possible responses for this request.
     * @return
     */
    public List<String> getOtherPossibleResponses() {
        return otherPossibleResponses;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GoogleResponse)) {
            return false;
        }
        GoogleResponse googleResponse = (GoogleResponse) obj;
        if (!response.equals(googleResponse.response)) {
            return false;
        }
        if (!confidence.equals(googleResponse.confidence)) {
            return false;
        }
        if (!otherPossibleResponses.equals(googleResponse.otherPossibleResponses)) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.response != null ? this.response.hashCode() : 0);
        hash = 59 * hash + (this.confidence != null ? this.confidence.hashCode() : 0);
        hash = 59 * hash + (this.otherPossibleResponses != null ? this.otherPossibleResponses.hashCode() : 0);
        return hash;
    }
    
}
