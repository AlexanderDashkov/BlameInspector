package blameinspector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

public class PropertyServiceTest extends TestCase {


    private static final String POSITIVE_PATH = "src\\test\\resources\\schemas\\positive\\";
    private static final String NEGATIVE_PATH = "src\\test\\resources\\schemas\\negative\\";

    private static final String CONTENT_NOT_COMPLETE = "The content of element ''project'' is not complete." +
            " One of {0} is expected.";
    private static final String PROJECT_NOT_FOUND = "Project with such name wasn't found in file.";
    private static final String WRONG_VAL_VCS = "Value '' is not facet-valid with respect to enumeration '[git, svn]'." +
            " It must be a value from the enumeration.";
    private static final String MISS_CLOSE_TAG = "The element type \"projects\" must be terminated by" +
            " the matching end-tag \"</projects>\".";
    private static final String NO_NAME_ATTR_FOUND = "No name Attribute in project tag!";

    private ArrayList<String> positiveTests;
    private ArrayList<Pair> negativeTests;

    public PropertyServiceTest( String testName ) throws IOException {
        super( testName );
        positiveTests = new ArrayList<>();
        negativeTests = new ArrayList<>();
        positiveTests.add("Basic.xml");
        positiveTests.add("Long.xml");
        positiveTests.add("Disorder.xml");
        negativeTests.add(new Pair("Basic.xml", PROJECT_NOT_FOUND));
        negativeTests.add(new Pair("MissingGit.xml", MessageFormat.format(CONTENT_NOT_COMPLETE, "'{vcs}'")));
        negativeTests.add(new Pair("MissingIT.xml", MessageFormat.format(CONTENT_NOT_COMPLETE, "'{issueTracker}'")));
        negativeTests.add(new Pair("MissingName.xml", MessageFormat.format(CONTENT_NOT_COMPLETE, "'{userName}'")));
        negativeTests.add(new Pair("MissingPassword.xml", MessageFormat.format(CONTENT_NOT_COMPLETE, "'{password}'")));
        negativeTests.add(new Pair("MissingPath.xml", MessageFormat.format(CONTENT_NOT_COMPLETE, "'{pathToRepo}'")));
        negativeTests.add(new Pair("MissingProjectName.xml", MessageFormat.format(CONTENT_NOT_COMPLETE, "'{projectName}'")));
        negativeTests.add(new Pair("MissingValueVCS.xml", WRONG_VAL_VCS));
        negativeTests.add(new Pair("NoAttr.xml", NO_NAME_ATTR_FOUND));
        negativeTests.add(new Pair("Corrupted.xml", MISS_CLOSE_TAG));
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
            assertTrue(message.contains(test.second));
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
