package com.darkprograms.speech.recognizer.awslex;

import com.amazonaws.services.lexruntime.model.PostContentResult;
import com.darkprograms.speech.recognizer.RecognitionResult;

public class LexResponse implements RecognitionResult {
    private PostContentResult result;
//    private String response;

    public LexResponse(PostContentResult result) {
        this.result = result;
//        this.response = result.getInputTranscript();

        // Close      - Fulfilled or Failed   (ReadyForFulfillment?)
        // Incomplete - ElicitIntent, ConfirmIntent, ElicitSlot
//        result.getDialogState();
//        result.getIntentName();
//        result.getMessage();
//        result.getSessionAttributes();
//        result.getSlots();
//        result.getSlotToElicit();
//        No card?!!!
    }

    public String getResponse() {
        return result.getInputTranscript();
//        return null;
    }

    public boolean isFinalResponse() {
        String state = result.getDialogState();
        return "Fulfilled".equals(state) || "Failed".equals(state);
    }

    public PostContentResult getResult() {
        return result;
    }
}
