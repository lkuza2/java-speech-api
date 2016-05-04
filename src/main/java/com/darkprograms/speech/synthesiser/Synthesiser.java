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



/*******************************************************************************
 * Synthesiser class that connects to Google's unoffical API to retrieve data
 *
 * @author Luke Kuza, Aaron Gokaslan (Skylion)
 *******************************************************************************/
public class Synthesiser extends BaseSynthsiser {

	/**
	 * URL to query for Google synthesiser
	 */
	private final static String GOOGLE_SYNTHESISER_URL = "http://translate.google.com/translate_tts";

	/**
	 * language of the Text you want to translate
	 */
	private String languageCode; 

	/**
	 * LANG_XX_XXXX Variables are language codes. 
	 */
	public static final String LANG_AU_ENGLISH = "en-AU";
	public static final String LANG_US_ENGLISH = "en-US";
	public static final String LANG_UK_ENGLISH = "en-GB";
	public static final String LANG_ES_SPANISH = "es";
	public static final String LANG_FR_FRENCH = "fr";
	public static final String LANG_DE_GERMAN = "de";
	public static final String LANG_PT_PORTUGUESE = "pt-pt";
	public static final String LANG_PT_BRAZILIAN = "pt-br";
	//Please add on more regional languages as you find them. Also try to include the accent code if you can can.

	/**
	 * Constructor
	 */
	public Synthesiser() {
		languageCode = "auto";
	}

	/**
	 * Constructor that takes language code parameter. Specify to "auto" for language autoDetection 
	 */
	public Synthesiser(String languageCode){
		this.languageCode = languageCode;
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

	@Override
	public InputStream getMP3Data(String synthText) throws IOException{

		String languageCode = this.languageCode;//Ensures retention of language settings if set to auto

		if(languageCode == null || languageCode.equals("") || languageCode.equalsIgnoreCase("auto")){
			languageCode = detectLanguage(synthText);//Detects language
			/* NOTE: Detect language relies on an entirely seperate endpoint.
			 * If the GoogleTranslate API stops working, do not use the auto parameter
			 * and switch to something else or a best guess.
			 */
			if(languageCode == null){
				languageCode = "en-us";//Reverts to Default Language if it can't detect it.
				//Throw an error message here eventually
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

		StringBuilder sb = new StringBuilder();
		sb.append(GOOGLE_SYNTHESISER_URL); //The base URL prefixed by the query parameter.
		sb.append("?tl=");
		sb.append(languageCode); //The query parameter to specify the language code.
		sb.append("&q=");
		sb.append(encoded); //We encode the String using URL Encoder
		sb.append("&ie=UTF-8&total=1&idx=0"); //Some unknown parameters needed to make the URL work
		sb.append("&textlen=");
		sb.append(synthText.length()); //We need some String length now.
		sb.append("&client=tw-ob"); //Once again, a weird parameter.
		//Client=t no longer works as it requires a token, but client=tw-ob seems to work just fine.

		URL url = new URL(sb.toString());
		// Open New URL connection channel.
		URLConnection urlConn = url.openConnection(); //Open connection

		//Adding header for user agent is required
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0) "
				+ "Gecko/20100101 Firefox/4.0");

		return urlConn.getInputStream();
	}
}

