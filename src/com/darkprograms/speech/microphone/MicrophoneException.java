package com.darkprograms.speech.microphone;

/**
 * @author Jeremy Treder
 *
 */
public class MicrophoneException extends Exception
{
	public MicrophoneException(Throwable cause)
	{
		super("Can not initialize microphone.", cause);
	}
}
