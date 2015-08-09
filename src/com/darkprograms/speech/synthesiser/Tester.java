package com.darkprograms.speech.synthesiser;

import java.io.IOException;
import java.io.InputStream;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

public class Tester {

	public static void main(String[] args) {
		SynthesiserV2 syn = new SynthesiserV2("APIKEY");
		syn.setLanguage("de-de");
		try {
			System.out.println(syn.detectLanguage("What's up?"));
			System.out.println(syn.detectLanguage("Wie geht es dir?"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			InputStream in = syn.getMP3Data("Wie geht es dir? Battlefield 2!");
			AudioStream as = new AudioStream(in);
			AudioPlayer.player.start(as);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
