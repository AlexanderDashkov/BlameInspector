package blameinspector.unittest;

import blameinspector.*;
import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.vcs.VersionControlServiceException;

import org.junit.Assert;
import org.junit.Test;


import java.io.IOException;

public class DuplicateTest extends Assert {

    public DuplicateTest() throws IOException {
    }

    @Test
    public void testDuplicatesFromBlameWhoTest() throws PropertyServiceException, VersionControlServiceException, IssueTrackerException, TicketCorruptedException, IOException {
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
