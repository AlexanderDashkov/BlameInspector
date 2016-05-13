package blameinspector;


import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.issuetracker.IssueTrackerService;
import blameinspector.vcs.VersionControlService;
import blameinspector.vcs.VersionControlServiceException;

import java.io.*;
import java.util.ArrayList;

public class Manager {

    private static final String blameInspectorFile = "blameInspector.ser";
    private static final String itsFile = "its.ser";
    private static final String stTreeFile = "stTree.ser";
    private static final String RESULT_FILE = "result.ser";

    private static VersionControlService versionControlService;
    private static IssueTrackerService issueTrackerService;
    private static BlameInspector blameInspector;
    private static StackTraceTree stackTraceTree;
    private static boolean areReadyResults;
    private static String projectName;

    public Manager(PropertyService propertyService, boolean isParsingCode, boolean useDb) throws VersionControlServiceException, IssueTrackerException {
        projectName = propertyService.getProjectName();
        areReadyResults = false;
        versionControlService = ServicesFactory.getVersionControlService(propertyService.getVersionControl(),
                propertyService.getPathToRepo(),
                propertyService.getIssueTracker(), isParsingCode);
        issueTrackerService = ServicesFactory.getIssueTrackerService(propertyService.getUserName(),
                propertyService.getPassword(),
                versionControlService.getRepositoryOwner(),
                propertyService.getProjectName(),
                propertyService.getIssueTracker());
        blameInspector = new BlameInspector(versionControlService, issueTrackerService, isParsingCode);
        if (useDb && readObjectFromFile(projectName + RESULT_FILE) != null){
            blameInspector.setResults((ArrayList<TicketInfo>)readObjectFromFile(projectName + RESULT_FILE));
            stackTraceTree = (StackTraceTree)readObjectFromFile(projectName + stTreeFile);
            areReadyResults = true;
            return;
        }

        stackTraceTree = new StackTraceTree(propertyService.getProjectName());
    }

    public void handleTicket(final int ticketNumber) throws VersionControlServiceException, BlameInspectorException, TicketCorruptedException {
        if (areReadyResults){
            return;
        }
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

    public void storeData(){
        if (areReadyResults){
            return;
        }
        //System.out.println("size : " + blameInspector.getResults().size());
        //writeObjectToFile(projectName + blameInspectorFile, blameInspector);
        writeObjectToFile(projectName + stTreeFile, stackTraceTree);
        writeObjectToFile(projectName + RESULT_FILE, blameInspector.getResults());
    }

    private void writeObjectToFile(final String fileName, Object o){
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(o);
            oos.close();
        } catch (IOException e){
            // do something wise here
            // or throw manager exception
        }
    }

    private Object readObjectFromFile(final String fileName){
        try {
            FileInputStream streamIn = new FileInputStream(fileName);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            Object object = objectinputstream.readObject();
            return object;
        } catch (ClassNotFoundException e){
            // do something wise here
            // or throw manager exception
        }catch (IOException e){
            // do something wise here
            // or throw manager exception
        }
        return null;
    }
}
