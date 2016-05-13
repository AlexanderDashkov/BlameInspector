package blameinspector;

import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.vcs.VersionControlServiceException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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

    public void testDuplicatesFromBlameWhoTest() throws PropertyServiceException, VersionControlServiceException, IssueTrackerException, TicketCorruptedException {
        StackTraceTree stackTraceTree = new StackTraceTree("BlameWhoTest");
        PropertyService propertyService = new PropertyService("BlameWhoTest", "config.properties");
        Manager manager = new Manager(propertyService ,false, false);
        BlameInspector blameInspector = manager.getBlameInspector();
        assertTrue(stackTraceTree.addTicket(blameInspector.parseIssueBody(Storage.testDuplicate1, 1), 1).size() == 1);
        assertTrue(stackTraceTree.addTicket(blameInspector.parseIssueBody(Storage.testDuplicate2, 2), 2).size() == 1);
        assertTrue(stackTraceTree.addTicket(blameInspector.parseIssueBody(Storage.testDuplicate3, 3), 3).size() == 1);
        assertTrue(stackTraceTree.addTicket(blameInspector.parseIssueBody(Storage.testDuplicate4, 4), 4).size() == 2);
        assertTrue(stackTraceTree.addTicket(blameInspector.parseIssueBody(Storage.testDuplicate5, 5), 5).size() == 2);
    }




}
