package org.amplexus.speechdemo;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.*;
import java.io.File;
import net.sourceforge.javaflacencoder.FLACFileWriter;

import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.recognizer.Recognizer;
import com.darkprograms.speech.recognizer.GoogleResponse;

 /**
   * Jarvis Speech API Tutorial
   * @author Aaron Gokaslan (Skylion)
   *
   */
public class HelloWorld {

  public static void main (String[]args) {

    // Mixer.Info[] infoArray = AudioSystem.getMixerInfo();
    // for(Mixer.Info info : infoArray) {
    //    System.out.println("info: " + info.toString());
    // } 
    AudioFileFormat.Type[] typeArray = AudioSystem.getAudioFileTypes();
    for(AudioFileFormat.Type type : typeArray) {
       System.out.println("type: " + type.toString());
    } 

    Microphone mic = new Microphone(FLACFileWriter.FLAC);
    File file = new File ("/tmp/testfile2.flac");	//Name your file whatever you want
    try {
      mic.captureAudioToFile (file);
    } catch (Exception ex) {
      //Microphone not available or some other error.
      System.out.println ("ERROR: Microphone is not availible.");
      ex.printStackTrace ();
    }

    /* User records the voice here. Microphone starts a separate thread so do whatever you want
     * in the mean time. Show a recording icon or whatever.
     */
    try {
      System.out.println ("Recording...");
      Thread.sleep (5000);	//In our case, we'll just wait 5 seconds.
      mic.close ();
    } catch (InterruptedException ex) {
      ex.printStackTrace ();
    }

    mic.close ();		//Ends recording and frees the resources
    System.out.println ("Recording stopped.");

    Recognizer recognizer = new Recognizer (Recognizer.Languages.ENGLISH_US, System.getProperty("google-api-key"));
    //Although auto-detect is available, it is recommended you select your region for added accuracy.
    try {
      int maxNumOfResponses = 4;
      System.out.println("Sample rate is: " + (int) mic.getAudioFormat().getSampleRate());
      GoogleResponse response = recognizer.getRecognizedDataForFlac (file, maxNumOfResponses, (int) mic.getAudioFormat().getSampleRate ());
      System.out.println ("Google Response: " + response.getResponse ());
      System.out.println ("Google is " + Double.parseDouble (response.getConfidence ()) * 100 + "% confident in" + " the reply");
      System.out.println ("Other Possible responses are: ");
      for (String s:response.getOtherPossibleResponses ()) {
	  System.out.println ("\t" + s);
      }
    }
    catch (Exception ex) {
      // TODO Handle how to respond if Google cannot be contacted
      System.out.println ("ERROR: Google cannot be contacted");
      ex.printStackTrace ();
    }

    file.deleteOnExit ();	//Deletes the file as it is no longer necessary.
  }
}
