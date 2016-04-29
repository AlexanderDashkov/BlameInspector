package blameinspector;

import com.jmolly.stacktraceparser.NStackTrace;
import com.jmolly.stacktraceparser.StackTraceParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.antlr.runtime.RecognitionException;

import java.io.IOException;

public class DuplicateTest extends TestCase {

    public DuplicateTest(String testName) throws IOException {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(DuplicateTest.class);
    }

    public void testDuplicatesFromBlameWhoTest() {
        StackTraceTree stackTraceTree = new StackTraceTree("BlameWhoTest");
        assertTrue(stackTraceTree.addTicket(parseTicket(Storage.testDuplicate1), 1).size() == 1);
        assertTrue(stackTraceTree.addTicket(parseTicket(Storage.testDuplicate2), 2).size() == 1);
        assertTrue(stackTraceTree.addTicket(parseTicket(Storage.testDuplicate3), 3).size() == 1);
        assertTrue(stackTraceTree.addTicket(parseTicket(Storage.testDuplicate4), 4).size() == 2);
        assertTrue(stackTraceTree.addTicket(parseTicket(Storage.testDuplicate5), 5).size() == 2);
    }

    private NStackTrace parseTicket(final String testString) {
        try {
            return StackTraceParser.parse(testString);
        } catch (RecognitionException e) {
            return null;
        }
    }

}
