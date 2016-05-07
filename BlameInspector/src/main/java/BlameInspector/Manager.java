package blameinspector;


import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.issuetracker.IssueTrackerService;
import blameinspector.vcs.VersionControlService;
import blameinspector.vcs.VersionControlServiceException;

import java.io.IOException;
import java.util.ArrayList;

public class Manager {

    private static VersionControlService versionControlService;
    private static IssueTrackerService issueTrackerService;
    private static BlameInspector blameInspector;
    private static StackTraceTree stackTraceTree;

    public Manager(PropertyService propertyService, boolean isParsingCode) throws VersionControlServiceException, IssueTrackerException {
        versionControlService = ServicesFactory.getVersionControlService(propertyService.getVersionControl(),
                propertyService.getPathToRepo(),
                propertyService.getIssueTracker(), isParsingCode);
        issueTrackerService = ServicesFactory.getIssueTrackerService(propertyService.getUserName(),
                propertyService.getPassword(),
                versionControlService.getRepositoryOwner(),
                propertyService.getProjectName(),
                propertyService.getIssueTracker());
        blameInspector = new BlameInspector(versionControlService, issueTrackerService, isParsingCode);
        stackTraceTree = new StackTraceTree(propertyService.getProjectName());
    }

    public void handleTicket(final int ticketNumber) throws VersionControlServiceException, BlameInspectorException, TicketCorruptedException {
        TicketInfo ticketInfo = blameInspector.handleTicket(ticketNumber);
        if (ticketInfo.isAssigned()) {
            ArrayList<Integer> duples = stackTraceTree.addTicket(ticketInfo.getStackTrace(), ticketNumber);
            ticketInfo.setDupplicates(duples);
        }

    }

    public ArrayList<TicketInfo> getResults(){
        return blameInspector.getResults();
    }

    public ArrayList<ArrayList<Integer>> getDuplicates(){
        return stackTraceTree.getDuplicates();
    }

    public int getNumberOfTickets() {
        return blameInspector.getNumberOfTickets();
    }

    public void setAssignee() throws IssueTrackerException {
        blameInspector.setAssignee();
    }

    public void refresh() {
        blameInspector.refresh();
    }

    public boolean isAssigned(int i) {
        return blameInspector.isAssigned(i);
    }

    public String properAssignee(int i) throws IOException {
        return blameInspector.properAssignee(i);
    }

    public BlameInspector getBlameInspector(){
        return blameInspector;
    }

    public IssueTrackerService getIssueTrackerService(){
        return issueTrackerService;
    }
}
