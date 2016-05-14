package blameinspector;


import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.issuetracker.IssueTrackerService;
import blameinspector.vcs.VersionControlService;
import blameinspector.vcs.VersionControlServiceException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
    private static boolean isParsingCode;
    private static ArrayList<TicketInfo> results;
    private static int nThreads;

    public Manager(PropertyService propertyService, boolean isParsingCode, boolean useDb) throws VersionControlServiceException, IssueTrackerException {
        nThreads = 0;
        projectName = propertyService.getProjectName();
        this.isParsingCode = isParsingCode;
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
        if (useDb && readObjectFromFile(projectName + RESULT_FILE) != null) {
            this.results = (ArrayList<TicketInfo>) readObjectFromFile(projectName + RESULT_FILE);
            stackTraceTree = (StackTraceTree) readObjectFromFile(projectName + stTreeFile);
            areReadyResults = true;
            return;
        }
        this.results = new ArrayList<>();
        stackTraceTree = new StackTraceTree(propertyService.getProjectName());
    }

    public void setNThread(final int n){
        this.nThreads = n;
    }

    public void handleTicket(final int ticketNumber) throws VersionControlServiceException, BlameInspectorException, TicketCorruptedException {
        if (areReadyResults) {
            return;
        }
        TicketInfo ticketInfo = blameInspector.handleTicket(ticketNumber);
        if (ticketInfo.isAssigned()) {
            ArrayList<Integer> duples = stackTraceTree.addTicket(ticketInfo.getStackTrace(), ticketNumber);
            ticketInfo.setDupplicates(duples);
        }
    }



    public void proccesTickets(int startBound, int endBound) throws VersionControlServiceException, BlameInspectorException, IssueTrackerException {
        nThreads = (nThreads == 0) ? 2 : nThreads;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        List<FutureTask> taskList = new ArrayList<>();
        for (int i = startBound; i <= endBound; i++) {
            final int ticketNumber = i;
            FutureTask<String> task = new FutureTask<String>(new Callable<String>(){
                @Override
                public String call(){
                    try{
                        BlameInspector blamer = new BlameInspector(versionControlService, issueTrackerService, isParsingCode);
                        blamer.setResults(results);
                        blamer.handleTicket(ticketNumber);
                        //System.out.println(ticketNumber);
                    if (Main.setAssignee()) {
                        setAssignee();
                    }
                    refresh();
                    } catch (Exception e){}
                    return String.valueOf(ticketNumber);
                }
            });
            taskList.add(task);
            executorService.execute(task);
        }
        String result = "";
        for (int j = 0; j < taskList.size(); j++) {
            FutureTask<Integer> task = taskList.get(j);
            try {
                result += task.get();
            }catch (ExecutionException | InterruptedException e){}
        }
        //System.out.println(result);
        executorService.shutdown();
    }

    public ArrayList<TicketInfo> getResults() {
        return results;
    }

    public ArrayList<ArrayList<Integer>> getDuplicates() {
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

    public BlameInspector getBlameInspector() {
        return blameInspector;
    }

    public IssueTrackerService getIssueTrackerService() {
        return issueTrackerService;
    }

    public void storeData() {
        if (areReadyResults) {
            return;
        }
        //System.out.println("size : " + blameInspector.getResults().size());
        //writeObjectToFile(projectName + blameInspectorFile, blameInspector);
        writeObjectToFile(projectName + stTreeFile, stackTraceTree);
        writeObjectToFile(projectName + RESULT_FILE, results);
    }

    private void writeObjectToFile(final String fileName, Object o) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(o);
            oos.close();
        } catch (IOException e) {
            // do something wise here
            // or throw manager exception
        }
    }

    private Object readObjectFromFile(final String fileName) {
        try {
            FileInputStream streamIn = new FileInputStream(fileName);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            Object object = objectinputstream.readObject();
            return object;
        } catch (ClassNotFoundException e) {
            // do something wise here
            // or throw manager exception
        } catch (IOException e) {
            // do something wise here
            // or throw manager exception
        }
        return null;
    }
}
