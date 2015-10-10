# java-speech-api-demo

A sample app demonstrating the Google V2 speech API

Once built, you should be able to run it as follows (on Linux at least):

    java -Dgoogle-api-key="<Your Google API key here>" -cp ./target/hello-world-1.0.0-SNAPSHOT.jar:$HOME/.m2/repository/com/darkprograms/speech/java-speech-api/1.13.0-SNAPSHOT/java-speech-api-1.13.0-SNAPSHOT.jar:$HOME/.m2/repository/net/sourceforge/javaflacencoder/java-flac-encoder/0.3.7/java-flac-encoder-0.3.7.jar:$HOME/.m2/repository/org/json/json/20150729/json-20150729.jar org.amplexus.speechdemo.HelloWorld

The Google API key is obtained per instructions here: https://stackoverflow.com/questions/26485531/google-speech-api-v2

It will listen on your microphone for a few seconds before sending whatever it hears to Google for recognition, and will display Google's response.

Planned Example Features:
TODO: Rebuild a nice GUI application to increase the accessibility of the API.
