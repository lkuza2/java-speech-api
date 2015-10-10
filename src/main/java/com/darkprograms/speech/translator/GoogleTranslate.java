package com.darkprograms.speech.translator;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Locale;

/***************************************************************************************************************
 * An API for a Google Translation service in Java. 
 * Please Note: This API is unofficial and is not supported by Google. Subject to breakage at any time.
 * The translator allows for language detection and translation. 
 * Recommended for translation of user interfaces or speech commands.
 * All translation services provided via Google Translate
 * @author Aaron Gokaslan (Skylion)
 ***************************************************************************************************************/
public final class GoogleTranslate { //Class marked as final since all methods are static
	
	/**
	 * URL to query for Translation
	 */
	private final static String GOOGLE_TRANSLATE_URL = "http://translate.google.com/translate_a/t?client=t";

	/**
	 * Private to prevent instantiation
	 */
	private GoogleTranslate(){};
	
	/**
	 * Converts the ISO-639 code into a friendly language code in the user's default language
	 * For example, if the language is English and the default locale is French, it will return "anglais"
	 * Useful for UI Strings
	 * @param languageCode The ISO639-1 
	 * @return The language in the user's default language
	 */
	public static String getDisplayLanguage(String languageCode){
		return (new Locale(languageCode)).getDisplayLanguage();
	}
	
	/**
	 * Automatically determines the language of the original text
	 * @param text represents the text you want to check the language of
	 * @return The ISO-639 code for the language
	 * @throws IOException if it cannot complete the request
	 */
	public static String detectLanguage(String text) throws IOException{
		String encoded = URLEncoder.encode(text, "UTF-8"); //Encodes the string
		URL url = new URL(GOOGLE_TRANSLATE_URL + "&text=" + encoded); //Generates URL
		String rawData = urlToText(url);//Gets text from Google
		return findLanguage(rawData);
	}
	
	
	/**
	 * Automatically translates text to a system's default language according to its locale
	 * Useful for creating international applications as you can translate UI strings
	 * @param text The text you want to translate
	 * @return The translated text
	 * @throws IOException if cannot complete request
	 */
	public static String translate(String text) throws IOException{
		return translate(Locale.getDefault().getLanguage(), text);
	}
	
	/**
	 * Automatically detects language and translate to the targetLanguage
	 * @param targetLanguage The language you want to translate into in ISO-639 format
	 * @param text The text you actually want to translate 
	 * @return The translated text.
	 * @throws IOException if it cannot complete the request 	
	 */
	public static String translate(String targetLanguage, String text) throws IOException{
		return translate("auto",targetLanguage, text);
	}
	
	/**
	 * Translate text from sourceLanguage to targetLanguage
	 * Specifying the sourceLanguage greatly improves accuracy over short Strings
	 * @param sourceLanguage The language you want to translate from in ISO-639 format
	 * @param targetLanguage The language you want to translate into in ISO-639 format
	 * @param text The text you actually want to translate 
	 * @return the translated text.
	 * @throws IOException if it cannot complete the request
	 */
	public static String translate(String sourceLanguage, String targetLanguage, String text) throws IOException{
		String encoded = URLEncoder.encode(text, "UTF-8"); //Encode
		//Generates URL
		URL url = new URL(GOOGLE_TRANSLATE_URL + "&sl=" + sourceLanguage + "&tl=" + targetLanguage +  "&text=" + encoded); 
		String rawData = urlToText(url);//Gets text from Google
		if(rawData==null){
			return null;
		}
		String[] raw =  rawData.split("\"");//Parses the JSON
		if(raw.length<2){
			return null;
		}
		return raw[1];//Returns the translation
	}
	
	/**
	 * Converts a URL to Text
	 * @param url that you want to generate a String from
	 * @return The generated String
	 * @throws IOException if it cannot complete the request
	 */
	private static String urlToText(URL url) throws IOException{
		URLConnection urlConn = url.openConnection(); //Open connection
		//Adding header for user agent is required. Otherwise, Google rejects the request
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0) Gecko/20100101 Firefox/4.0"); 
		Reader r = new java.io.InputStreamReader(urlConn.getInputStream(), Charset.forName("UTF-8"));//Gets Data Converts to string
		StringBuilder buf = new StringBuilder();
		while (true) {//Reads String from buffer
			int ch = r.read();
			if (ch < 0)
				break;
			buf.append((char) ch);
		}
		String str = buf.toString();
		return str;
	}


	/**
	 * Searches RAWData for Language
	 * @param RAWData the raw String directly from Google you want to search through
	 * @return The language parsed from the rawData or en-US (English-United States) if Google cannot determine it.
	 */
	private static String findLanguage(String rawData){
		for(int i = 0; i+5<rawData.length(); i++){
			boolean dashDetected = rawData.charAt(i+4)=='-';
			if(rawData.charAt(i)==','  && rawData.charAt(i+1)== '"' 
					&& ((rawData.charAt(i+4)=='"' && rawData.charAt(i+5)==',')
							|| dashDetected)){
				if(dashDetected){
					int lastQuote = rawData.substring(i+2).indexOf('"');
					if(lastQuote>0)
							return rawData.substring(i+2,i+2+lastQuote);
				}
				else{
					String possible = rawData.substring(i+2,i+4);
					if(containsLettersOnly(possible)){//Required due to Google's inconsistent formatting.
						return possible;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Checks if all characters in text are letters.  
	 * @param text The text you want to determine the validity of.
	 * @return True if all characters are letter, otherwise false.
	 */
	private static boolean containsLettersOnly(String text){
		for(int i = 0; i<text.length(); i++){
			if(!Character.isLetter(text.charAt(i))){
				return false;
			}
		}
		return true;
	}
	
	
}
