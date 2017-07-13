package com.darkprograms.speech.recognizer.awslex;

import com.amazonaws.services.lexruntime.AmazonLexRuntime;
import com.amazonaws.services.lexruntime.model.PostContentRequest;
import com.amazonaws.services.lexruntime.model.PostContentResult;
import com.darkprograms.speech.recognizer.RecognitionResult;
import org.json.JSONObject;

import javax.sound.sampled.AudioInputStream;
import java.util.Base64;
import java.util.Map;

/**
 * example:
 * <pre>
 LexRecogniser lex = new LexRecogniser(AmazonLexRuntimeClientBuilder.defaultClient(), "MyLexBot", "PROD", "auser");
 MicrophoneAnalyzer mic = new MicrophoneAnalyzer(null);
 VoiceActivityDetector vad = new VoiceActivityDetector();

 vad.detectVoiceActivity(mic, audioInputStream -> {
    PostContentResult result = lex.getRecognizedDataForStream(audioInputStream, myApp.getSessionAttributes()).getResult();
    System.out.println(result.message);
 });
 * </pre>
 */
public class LexRecogniser {
    private AmazonLexRuntime lex;
    private String botName;
    private String botAlias;
    private String userId;

    public LexRecogniser(AmazonLexRuntime lex, String botName, String botAlias, String userId) {
        this.lex = lex;
        this.botName = botName;
        this.botAlias = botAlias;
        this.userId = userId;
    }

    public RecognitionResult getRecognizedDataForStream(AudioInputStream stream, Map<String, String> sessionAttributes) {
        String json;
        if (sessionAttributes == null || sessionAttributes.isEmpty()) {
            json = null;
        } else {
            StringBuilder str = null;

            for (Map.Entry<String, String> entry : sessionAttributes.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (str == null) {
                    str = new StringBuilder("{");
                } else {
                    str.append(",");
                }

                str.append("\"").append(key).append("\":");
                if (value == null) {
                    str.append("null");
                } else {
                    str.append(JSONObject.quote(value));
                }
            }

            json = str.append("}").toString();
        }

        return getRecognizedDataForStream(stream, new String(Base64.getEncoder().encode(json.getBytes())));
    }

    public RecognitionResult getRecognizedDataForStreamWithObjects(AudioInputStream stream, Map<String, Object> sessionAttributes) {
        String json;
        if (sessionAttributes == null || sessionAttributes.isEmpty()) {
            json = null;
        } else {
            StringBuilder str = null;

            for (Map.Entry<String, Object> entry : sessionAttributes.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (str == null) {
                    str = new StringBuilder("{");
                } else {
                    str.append(",");
                }

                str.append("\"").append(key).append("\":");
//                if (value == null) {
//                    str.append("null");
//                } else if (value instanceof Number) {
//                    str.append(JSONObject.numberToString((Number)value));
//                } else if (value instanceof Boolean) {
//                    str.append(((Boolean)value).toString());
//                } else if (value instanceof String) {
//                    str.append("\"").append((String)value).append("\"");
//                } else {
                    str.append(JSONObject.valueToString(value));
//                }
            }

            json = str.append("}").toString();
        }

        return getRecognizedDataForStream(stream, new String(Base64.getEncoder().encode(json.getBytes())));
    }

    /**
     * @param stream
     * @param sessionAttributes The value must be map (keys and values must be strings) that is JSON serialized and then base64 encoded
     * @return
     */
    public RecognitionResult getRecognizedDataForStream(AudioInputStream stream, String sessionAttributes) {
        PostContentRequest request = new PostContentRequest()
                .withBotName(botName)
                .withBotAlias(botAlias)
                .withInputStream(stream)
                .withUserId(userId)
                .withSessionAttributes(sessionAttributes);

        PostContentResult result = lex.postContent(request);
        return new LexResponse(result);
    }
}
