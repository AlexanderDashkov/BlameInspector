package blameinspector;

import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.issuetracker.IssueTrackerService;
import blameinspector.vcs.VersionControlService;
import blameinspector.vcs.VersionControlServiceException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class Manager extends AbstractHandler {

    private static final String SER_FORMAT = ".ser";

    private static String workingDirectory = System.getProperty("user.dir");
    private static final String SER_PATH = workingDirectory + File.separator + "workspace" + File.separator + "ProjectData Files" + File.separator;

    private static VersionControlService versionControlService;
    private static IssueTrackerService issueTrackerService;
    private static BlameInspector blameInspector;
    private static StackTraceTree stackTraceTree;
    private static boolean areReadyResults;
    private static String projectName;
    private static boolean isParsingCode;
    private static ArrayList<TicketInfo> results;
    private static int nThreads;
    private static String date;

    public Manager(PropertyService propertyService, boolean isParsingCode, boolean useDb) throws VersionControlServiceException, IssueTrackerException, IOException {
        Files.createDirectories(Paths.get(SER_PATH));
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
        if (useDb && readObjectsFromFile(projectName)!= null && readObjectsFromFile(projectName).get(0) != null) {
            ArrayList<Object> objects = readObjectsFromFile(projectName);
            this.results = (ArrayList<TicketInfo>) objects.get(0);
            stackTraceTree = (StackTraceTree) objects.get(1);
            areReadyResults = true;
            return;
        }
        this.results = new ArrayList<TicketInfo>();
        stackTraceTree = new StackTraceTree(propertyService.getProjectName());
    }

    public void setNThread(final int n){
        this.nThreads = n;
    }

    public TicketInfo handleTicket(final int ticketNumber) throws VersionControlServiceException, BlameInspectorException, TicketCorruptedException {
        if (areReadyResults) {
            return null;
        }
        TicketInfo ticketInfo = blameInspector.handleTicket(ticketNumber);
        setDuplicate(ticketInfo, ticketNumber);
        return ticketInfo;
    }

    public  void setDuplicate(final TicketInfo ticketInfo, final int ticketNumber){
        //if (ticketInfo.isAssigned()) {
            ArrayList<Integer> duples = stackTraceTree.addTicket(ticketInfo.getStackTrace(), ticketNumber);
            ticketInfo.setDupplicates(duples);
        //}
    }

    public void proccesTickets() throws IssueTrackerException, ManagerException, BlameInspectorException, VersionControlServiceException {
        int startBound = results.size();
        int endBound = issueTrackerService.getNumberOfTickets();
        proccesTickets(startBound, endBound);
    }

    public void proccesTickets(int startBound, int endBound) throws VersionControlServiceException, BlameInspectorException, IssueTrackerException, ManagerException {
        if (areReadyResults) return;
        nThreads = (nThreads == 0) ? 10 : nThreads;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        List<FutureTask> taskList = new ArrayList<>();
        for (int i = startBound; i <= endBound; i++) {
            final int ticketNumber = i;
            FutureTask<TicketInfo> task = new FutureTask<TicketInfo>(new Callable<TicketInfo>(){
                @Override
                public TicketInfo call(){
                    TicketInfo ticketInfo =  null;
                    try{
                        BlameInspector blamer = new BlameInspector(versionControlService, issueTrackerService, isParsingCode);
                        blamer.setResults(results);
                        ticketInfo = blamer.handleTicket(ticketNumber);
                        Main.getManager().setDuplicate(ticketInfo, ticketInfo.getTicketNumber());
                    if (Main.setAssignee()) {
                        setAssignee();
                    }
                    refresh();
                    } catch (Exception e){}
                    return ticketInfo;
                }
            });
            taskList.add(task);
            executorService.execute(task);
        }
        for (int j = 0; j < taskList.size(); j++) {
            FutureTask<TicketInfo> task = taskList.get(j);
            try {
               TicketInfo ticketInfo = task.get();
            }catch (ExecutionException | InterruptedException e){
                throw new ManagerException(e);
            }
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(200, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new ManagerException(e);
        }
    }

    public synchronized void addingTicket(final TicketInfo ticketInfo){
        results.add(ticketInfo);
    }



    public ArrayList<TicketInfo> getResults() {
        Collections.sort(results);
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

    public void storeData() throws ManagerException {
        if (areReadyResults) {
            return;
        }
        writeObjectsToFile(projectName, results, stackTraceTree);
    }

    private void writeObjectsToFile(final String fileName, Object o1, Object o2) throws ManagerException {
        try {
            FileOutputStream fout = new FileOutputStream(SER_PATH + fileName + SER_FORMAT);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(o1);
            oos.writeObject(o2);
            oos.close();
        } catch (IOException e) {
            throw new ManagerException(e);
        }
    }

    private ArrayList<Object> readObjectsFromFile(final String fileName) {
        try {
            ArrayList<Object> res = new ArrayList<>();
            FileInputStream streamIn = new FileInputStream(SER_PATH + fileName + SER_FORMAT);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            Object object = objectinputstream.readObject();
            res.add(object);
            object = objectinputstream.readObject();
            res.add(object);
            return res;
        } catch (ClassNotFoundException e) {
            // do something wise here
            // or throw manager exception
        } catch (IOException e) {
            // do something wise here
            // or throw manager exception
        }
        return null;
    }

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        Main.printResults(out);
        request.setHandled(true);
    }

    public boolean isDbUpToDate(){
        if (!issueTrackerService.isUpToDate(results)){
            date = new Date().toString();
            Main.setDate(date);
            areReadyResults = false;
            return false;
        }
        return true;
    }
}
