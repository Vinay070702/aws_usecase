package org.vinay;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class DogBreedLambdaTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DogBreedLambdaTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DogBreedLambdaTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
