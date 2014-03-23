#Java-Speech-API Changelog

##Changelog
Changelog corresponds with a tagged and signed Git commit.  This marks the changes.
A tagged commit may or may not have a corresponding binary version available.
Format:  Tag: `<Corresponding Tag>`

* Version 1.15
	* Optimized synthesiser class. Massive speed improvements on long input strings!
	* Added experimental Duplex API in preparation for version 1.2 .

* Version 1.11 (Tag V1.100)
	* Fixed major bug in Recognizer

* Version 1.10 (Tag v1.100)
    * Added new Microphone Analyzer class.
    * Added volume and frequency detection and frame work for (Voice Activity Detection) 
    * Microphone API updated to make it more usable.
    * API re-branded as J.A.R.V.I.S. (Just A Reliable Vocal Interpreter & Synthesiser)

* Version 1.06 (Tag v1.016)
    * Added support for synthesiser for strings longer than 100 characters (Credits to @Skylion007)
    * Added support for synthesiser for multiple languages, accents, and voices. (Credits to @Skylion007)
    * Added support for auto-detection of language within synthesiser. (Credits to @Skylion007)

* Version 1.05 (Tag: v1.015)
    * Improved language support for recognizer (Credits to @duncanj)
    * Add support for multiple responses for recognizer (Credits to @duncanj)
    * Add profanity filter toggle support for recognizer (Credits to @duncanj)

* Version 1.01 (Tag: v1.01)
    * Fixed state functions for Microphones
    * Fixed encoding single byte frames
    * Support Multiple Languages

* Version 1.00 (Tag: v1.00)
    * Initial Release
