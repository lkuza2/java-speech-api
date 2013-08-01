/* 
 *
 * This file is free software; you can redistribute it and/or modify it under the terms of GNU
 * General Public License as published by the Free Sortware Foundation Inc. (59 TEmple Place, Suite 330,
 * Boston, MA 02111-1307 USA); either version 2 of the License, or
 * (at your option) any later version.
 *
 * This file is distributed in the hope that it will be useful,but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for mor details.
 *
 */

package com.darkprograms.speech.recognizer;

import java.io.File;
import java.io.FileNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Víctor Martín Molina
 */
public class RecognizerTest {
    
    public RecognizerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of setProfanityFilter method, of class Recognizer.
     */
    @Test
    public void testSetProfanityFilter() {
        System.out.println("setProfanityFilter");
        boolean profanityFilter = false;
        Recognizer instance = new Recognizer();
        instance.setProfanityFilter(profanityFilter);
        assertFalse(instance.isProfanityFilter());
    }

    /**
     * Test of setLanguage method, of class Recognizer.
     */
    @Test
    public void testSetLanguage() {
        System.out.println("setLanguage");
        String language = "";
        Recognizer instance = new Recognizer();
        instance.setLanguage(language);
        assertEquals("", instance.getLanguage());
    }

    /**
     * Test of getRecognizedDataForWave method, of class Recognizer.
     */
    @Test(expected = NullPointerException.class)
    public void testGetRecognizedDataForWave_File_int() throws Exception {
        System.out.println("getRecognizedDataForWave");
        File waveFile = null;
        int maxResults = 0;
        Recognizer instance = new Recognizer();
        instance.getRecognizedDataForWave(waveFile, maxResults);
    }

    /**
     * Test of getRecognizedDataForWave method, of class Recognizer.
     */
    @Test(expected = FileNotFoundException.class)
    public void testGetRecognizedDataForWave_String_int() throws Exception {
        System.out.println("getRecognizedDataForWave");
        String waveFile = "";
        int maxResults = 0;
        Recognizer instance = new Recognizer();
        instance.getRecognizedDataForWave(waveFile, maxResults);
    }

    /**
     * Test of getRecognizedDataForFlac method, of class Recognizer.
     */
    @Test(expected = NullPointerException.class)
    public void testGetRecognizedDataForFlac_File_int() throws Exception {
        System.out.println("getRecognizedDataForFlac");
        File flacFile = null;
        int maxResults = 0;
        Recognizer instance = new Recognizer();
        instance.getRecognizedDataForFlac(flacFile, maxResults);
    }

    /**
     * Test of getRecognizedDataForFlac method, of class Recognizer.
     */
    @Test(expected = FileNotFoundException.class)
    public void testGetRecognizedDataForFlac_String_int() throws Exception {
        System.out.println("getRecognizedDataForFlac");
        String flacFile = "";
        int maxResults = 0;
        Recognizer instance = new Recognizer();        
        instance.getRecognizedDataForFlac(flacFile, maxResults);
    }

    /**
     * Test of getRecognizedDataForWave method, of class Recognizer.
     */
    @Test(expected = NullPointerException.class)
    public void testGetRecognizedDataForWave_File() throws Exception {
        System.out.println("getRecognizedDataForWave");
        File waveFile = null;
        Recognizer instance = new Recognizer();
        instance.getRecognizedDataForWave(waveFile);
    }

    /**
     * Test of getRecognizedDataForWave method, of class Recognizer.
     */
    @Test(expected = FileNotFoundException.class)
    public void testGetRecognizedDataForWave_String() throws Exception {
        System.out.println("getRecognizedDataForWave");
        String waveFile = "";
        Recognizer instance = new Recognizer();
        instance.getRecognizedDataForWave(waveFile);
    }

    /**
     * Test of getRecognizedDataForFlac method, of class Recognizer.
     */
    @Test(expected = NullPointerException.class)
    public void testGetRecognizedDataForFlac_File() throws Exception {
        System.out.println("getRecognizedDataForFlac");
        File flacFile = null;
        Recognizer instance = new Recognizer();        
        instance.getRecognizedDataForFlac(flacFile);
    }

    /**
     * Test of getRecognizedDataForFlac method, of class Recognizer.
     */
    @Test(expected = FileNotFoundException.class)
    public void testGetRecognizedDataForFlac_String() throws Exception {
        System.out.println("getRecognizedDataForFlac");
        String flacFile = "";
        Recognizer instance = new Recognizer();        
        instance.getRecognizedDataForFlac(flacFile);
    }

    @Test
    public void parseResponseNullRawResponse() {
        Recognizer instance = new Recognizer();        
        GoogleResponse googleResponse = new GoogleResponse();
        instance.parseResponse(null, googleResponse);
        assertNull(googleResponse.getResponse());
        assertNull(googleResponse.getConfidence());
        assertTrue(googleResponse.getOtherPossibleResponses().isEmpty());
    }
    
    @Test(expected = NullPointerException.class)
    public void parseResponseNullGoogleResponse() {
        Recognizer instance = new Recognizer();                
        instance.parseResponse("{status=0,hypotheses=[{utterance:\"hello world\",confidence:0.9}]}", null);
    }
    
    @Test
    public void parseResponse() {
        Recognizer instance = new Recognizer();      
        GoogleResponse actual = new GoogleResponse();
        instance.parseResponse("{status=0,hypotheses=[{utterance:\"hello world\",confidence:0.9}, {utterance:\"hello gold\"}]}", actual);
        GoogleResponse expected = new GoogleResponse();
        expected.setConfidence("0.9");
        expected.setResponse("hello world");
        expected.getOtherPossibleResponses().add("hello gold");
        assertEquals(expected, actual);        
    }
}