package com.darkprograms.speech.synthesiser;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.darkprograms.speech.translator.GoogleTranslate;


/**
 * This class uses the V2 version of Google's Text to Speech API. While this class requires an API key,
 * the endpoint allows for additional specification of parameters including speed and pitch. 
 * See the constructor for instructions regarding the API_Key.
 * @author Skylion (Aaron Gokaslan)
 */
public class SynthesiserV2 extends BaseSynthsiser {

	private static final String GOOGLE_SYNTHESISER_URL = "https://www.google.com/speech-api/v2/synthesize?enc=mpeg" +
			"&client=chromium";
	
	/**
	 * API_KEY used for requests
	 */
	private final String API_KEY;

	/**
	 * language of the Text you want to translate
	 */
	private String languageCode;
	
	/**
	 * The pitch of the generated audio
	 */
	private double pitch = 1.0;
	
	/**
	 * The speed of the generated audio
	 */
	private double speed = 1.0;
	
	/**
	 * Constructor
	 * @param API_KEY The API-Key for Google's Speech API. An API key can be obtained by requesting
	 * one by following the process shown at this 
	 * <a href="http://www.chromium.org/developers/how-tos/api-keys">url</a>.
	 */
	public SynthesiserV2(String API_KEY){
		this.API_KEY = API_KEY;
	}
	
	/**
	 * Returns the current language code for the Synthesiser.
	 * Example: English(Generic) = en, English (US) = en-US, English (UK) = en-GB. and Spanish = es;
	 * @return the current language code parameter
	 */
	public String getLanguage(){
		return languageCode;
	}

	/**
	 * Note: set language to auto to enable automatic language detection.
	 * Setting to null will also implement Google's automatic language detection
	 * @param languageCode The language code you would like to modify languageCode to.
	 */
	public void setLanguage(String languageCode){
		this.languageCode = languageCode;
	}

	/**
	 * @return the pitch
	 */
	public double getPitch() {
		return pitch;
	}

	/**
	 * Sets the pitch of the audio.
	 * Valid values range from 0 to 2 inclusive.
	 * Values above 1 correspond to higher pitch, values below 1 correspond to lower pitch.
	 * @param pitch the pitch to set
	 */
	public void setPitch(double pitch) {
		this.pitch = pitch;
	}

	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Sets the speed of audio.
	 * Valid values range from 0 to 2 inclusive.
	 * Values higher than one correspond to faster and vice versa. 
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	@Override
	public InputStream getMP3Data(String synthText) throws IOException{

		String languageCode = this.languageCode;//Ensures retention of language settings if set to auto

		if(languageCode == null || languageCode.equals("") || languageCode.equalsIgnoreCase("auto")){
			try{
				languageCode = detectLanguage(synthText);//Detects language
				if(languageCode == null){
					languageCode = "en-us";//Reverts to Default Language if it can't detect it.
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
				languageCode = "en-us";//Reverts to Default Language if it can't detect it.
			}
		}

		if(synthText.length()>100){
			List<String> fragments = parseString(synthText);//parses String if too long
			String tmp = getLanguage();
			setLanguage(languageCode);//Keeps it from autodetecting each fragment.
			InputStream out = getMP3Data(fragments);
			setLanguage(tmp);//Reverts it to it's previous Language such as auto.
			return out;
		}


		String encoded = URLEncoder.encode(synthText, "UTF-8"); //Encode

		StringBuilder sb = new StringBuilder(GOOGLE_SYNTHESISER_URL);
		sb.append("&key=" + API_KEY);
		sb.append("&text=" + encoded);
		sb.append("&lang=" + languageCode);

		if(speed>=0 && speed<=2.0){
			sb.append("&speed=" + speed/2.0);
		}
		
		if(pitch>=0 && pitch<=2.0){
			sb.append("&pitch=" + pitch/2.0);
		}
		
		URL url = new URL(sb.toString()); //create url

		// Open New URL connection channel.
		URLConnection urlConn = url.openConnection(); //Open connection

		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0) Gecko/20100101 Firefox/4.0"); //Adding header for user agent is required
		
		return urlConn.getInputStream();
	}
}
