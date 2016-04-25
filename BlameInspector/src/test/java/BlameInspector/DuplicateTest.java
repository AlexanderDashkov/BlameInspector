package blameinspector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

public class DuplicateTest extends TestCase{

    public DuplicateTest(String testName) throws IOException {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(DuplicateTest.class);
    }

    public void testDuplicatesFromBlameWhoTest(){

    }

}
