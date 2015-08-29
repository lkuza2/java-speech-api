package com.darkprograms.speech.recognizer;

/**
 * Occurs if something went wrong with the HTTP connection. A cause may be a wrong API key.
 * 
 * @author Jeremy Treder
 *
 */
public class GoogleHTTPException extends RuntimeException
{
	public GoogleHTTPException()
	{
		super("Something went wrong with the HTTP connection. Maybe API key wrong?");
	}
}
