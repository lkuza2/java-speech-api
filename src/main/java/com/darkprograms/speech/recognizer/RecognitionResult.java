package com.darkprograms.speech.recognizer;

public interface RecognitionResult {
    /** @return String representation of what was said */
    String getResponse();
    boolean isFinalResponse();
}
