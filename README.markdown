#J.A.R.V.I.S. (Java-Speech-API)

J.A.R.V.I.S. Java Speech API: Just A Reliable Vocal Interpreter & Synthesizer. 
This is a project for the Java Speech API. The program interprets vocal inputs into text and synthesizes voices from text input.
The program supports dozens of languages and even has the ability to auto-detect languages! 

## Description
The J.A.R.V.I.S. Speech API is designed to be simple and efficient, using the speech engines created by Google  
to provide functionality for parts of the API. Essentially, it is an API written in Java,  
including a recognizer, synthesizer, and a microphone capture utility.  The project uses  
Google services for the synthesizer and recognizer.  While this requires an Internet  
connection, it provides a complete, modern, and fully functional speech API in Java.

##Features
The API currently provides the following functionality,

  * Microphone Capture API (Wrapped around the current Java API for simplicity)
  * A speech recognizer using Google's recognizer service
      * Converts WAVE files from microphone input to FLAC (using existing API, see CREDITS)
      * Retrieves Response from Google, including confidence score and text
  * A speech synthesiser using Google's synthesizer service
      * Retrieves synthesized text in an InputStream (MP3 data ready to be played)
  * Wave to FLAC API (Wrapped around the used API in the project, javaFlacEncoder, see CREDITS)
  * A translator using Google Translate (courtesy of Skylion's Google Toolkit)

##Changelog
See CHANGELOG.markdown for Version History/Changelog

##Credits
See CREDITS.markdown for Credits
