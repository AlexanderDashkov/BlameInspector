package blameinspector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.ArrayList;

public class PropertyServiceTest extends TestCase {


    private static final String POSITIVE_PATH = "src\\test\\resources\\schemas\\positive\\";
    private static final String NEGATIVE_PATH = "src\\test\\resources\\schemas\\negative\\";

    private ArrayList<String> positiveTests;
    private ArrayList<Pair> negativeTests;

    public PropertyServiceTest( String testName ) throws IOException {
        super( testName );
        positiveTests = new ArrayList<>();
        negativeTests = new ArrayList<>();
        positiveTests.add("Basic.xml");
        positiveTests.add("Long.xml");
        negativeTests.add(new Pair("Basic.xml", "Project with such name wasn't found in file."));
    }

    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testPositive() throws PropertyServiceException {
        for (String fileName : positiveTests){
            PropertyService propertyService = new PropertyService("BlameWhoTest", POSITIVE_PATH + fileName);
        }
    }

    public void testNegative() throws PropertyServiceException {
        for (Pair test : negativeTests){
            String message = null;
            try {
                PropertyService propertyService = new PropertyService("BlameWhoTest", NEGATIVE_PATH + test.first);
            }catch (PropertyServiceException e){
                message = e.getMessage();
            }
            assertEquals(test.second, message);
        }
    }

    private class Pair{
        private String first, second;
        private Pair(final String a, final String b){
            first = a;
            second = b;
        }
    }

}
