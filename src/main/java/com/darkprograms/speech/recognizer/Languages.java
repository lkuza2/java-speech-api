package com.darkprograms.speech.recognizer;

public enum Languages {
	
	
	AUTO_DETECT("auto"),//tells Google to auto-detect the language
	ARABIC_JORDAN("ar-JO"),
	ARABIC_LEBANON("ar-LB"),
	ARABIC_QATAR("ar-QA"),
	ARABIC_UAE("ar-AE"),
	ARABIC_MOROCCO("ar-MA"),
	ARABIC_IRAQ("ar-IQ"),
	ARABIC_ALGERIA("ar-DZ"),
	ARABIC_BAHRAIN("ar-BH"),
	ARABIC_LYBIA("ar-LY"),
	ARABIC_OMAN("ar-OM"),
	ARABIC_SAUDI_ARABIA("ar-SA"),
	ARABIC_TUNISIA("ar-TN"),
	ARABIC_YEMEN("ar-YE"),
	BASQUE("eu"),
	CATALAN("ca"),
	CZECH("cs"),
	DUTCH("nl-NL"),
	ENGLISH_AUSTRALIA("en-AU"),
	ENGLISH_CANADA("en-CA"),
	ENGLISH_INDIA("en-IN"),
	ENGLISH_NEW_ZEALAND("en-NZ"),
	ENGLISH_SOUTH_AFRICA("en-ZA"),
	ENGLISH_UK("en-GB"),
	ENGLISH_US("en-US"),
	FINNISH("fi"),
	FRENCH("fr-FR"),
	GALICIAN("gl"),
	GERMAN("de-DE"),
	HEBREW("he"),
	HUNGARIAN("hu"),
	ICELANDIC("is"),
	ITALIAN("it-IT"),
	INDONESIAN("id"),
	JAPANESE("ja"),
	KOREAN("ko"),
	LATIN("la"),
	CHINESE_SIMPLIFIED("zh-CN"),
	CHINESE_TRANDITIONAL("zh-TW"),
	CHINESE_HONGKONG("zh-HK"),
	CHINESE_CANTONESE("zh-yue"),
	MALAYSIAN("ms-MY"),
	NORWEGIAN("no-NO"),
	POLISH("pl"),
	PIG_LATIN("xx-piglatin"),
	PORTUGUESE("pt-PT"),
	PORTUGUESE_BRASIL("pt-BR"),
	ROMANIAN("ro-RO"),
	RUSSIAN("ru"),
	SERBIAN("sr-SP"),
	SLOVAK("sk"),
	SPANISH_ARGENTINA("es-AR"),
	SPANISH_BOLIVIA("es-BO"),
	SPANISH_CHILE("es-CL"),
	SPANISH_COLOMBIA("es-CO"),
	SPANISH_COSTA_RICA("es-CR"),
	SPANISH_DOMINICAN_REPUBLIC("es-DO"),
	SPANISH_ECUADOR("es-EC"),
	SPANISH_EL_SALVADOR("es-SV"),
	SPANISH_GUATEMALA("es-GT"),
	SPANISH_HONDURAS("es-HN"),
	SPANISH_MEXICO("es-MX"),
	SPANISH_NICARAGUA("es-NI"),
	SPANISH_PANAMA("es-PA"),
	SPANISH_PARAGUAY("es-PY"),
	SPANISH_PERU("es-PE"),
	SPANISH_PUERTO_RICO("es-PR"),
	SPANISH_SPAIN("es-ES"),
	SPANISH_US("es-US"),
	SPANISH_URUGUAY("es-UY"),
	SPANISH_VENEZUELA("es-VE"),
	SWEDISH("sv-SE"),
	TURKISH("tr"),
	ZULU("zu");
    
	//TODO Clean Up JavaDoc for Overloaded Methods using @link
	
    /**
     *Stores the LanguageCode
     */
    private final String languageCode;
    
    /**
     *Constructor
     */
    private Languages(final String languageCode){
    	this.languageCode = languageCode;
    }

	/**
	 * @return the languageCode
	 */
	public String getLanguageCode() {
		return languageCode;
	}
	
	@Override
    public String toString(){
    	return getLanguageCode();
    }
}
