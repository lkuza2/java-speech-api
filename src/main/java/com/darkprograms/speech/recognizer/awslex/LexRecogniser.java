package com.darkprograms.speech.recognizer.awslex;

import com.amazonaws.services.lexruntime.AmazonLexRuntime;
import com.amazonaws.services.lexruntime.AmazonLexRuntimeClientBuilder;
import com.amazonaws.services.lexruntime.model.PostContentRequest;
import com.amazonaws.services.lexruntime.model.PostContentResult;
import com.amazonaws.services.lexruntime.model.PostTextRequest;
import com.darkprograms.speech.recognizer.RecognitionResult;

import javax.sound.sampled.AudioInputStream;
import java.util.Map;

public class LexRecogniser {
    private String userId;
    private AmazonLexRuntime lex;

    LexRecogniser(String userId) {
        this.userId = userId;
        lex = AmazonLexRuntimeClientBuilder.standard()
//                .withClientConfiguration()
//                .withCredentials()
//                .withRegion()
                .build();
    }

    /**
     * @param stream
     * @param sessionAttributes The value must be map (keys and values must be strings) that is JSON serialized and then base64 encoded
     * @return
     */
    public RecognitionResult getRecognizedDataForStream(AudioInputStream stream, String sessionAttributes) {
        PostContentRequest request = new PostContentRequest()
                .withBotName("idear")
                .withBotAlias("PROD")
                .withInputStream(stream)
                .withUserId(userId)
                .withSessionAttributes(sessionAttributes);

        PostContentResult result = lex.postContent(request);
        return new LexResponse(result);
    }
}
