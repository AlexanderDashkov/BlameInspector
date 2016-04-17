package blameinspector;

import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.issuetracker.IssueTrackerService;
import blameinspector.vcs.VersionControlService;
import blameinspector.vcs.VersionControlServiceException;
import com.jmolly.stacktraceparser.NFrame;
import com.jmolly.stacktraceparser.NStackTrace;
import com.jmolly.stacktraceparser.StackTraceParser;
import org.antlr.runtime.RecognitionException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

public class BlameInspector {

    private final String AT = "at ";
    private final String RIGHT_BRACKET = ")";
    private final String LEFT_TAG_BRACKET = "&lt;";
    private final String RIGHT_TAG_BRACKET = "&gt;";
    private final String NBSP = "&nbsp";
    private final String NL = "\n";

    private static final String NO_STACKTRACE = "No StackTrace found in current ticket!";
    private static final String NO_ENTRY = "No entry of exception found in current repository.";

    private static StackTraceTree stTree;
    private static VersionControlService vcs;
    private static IssueTrackerService its;
    private static int numberOfTickets;

    private String blameLogin;

    public BlameInspector(final PropertyService propertyService) throws VersionControlServiceException, IssueTrackerException {
        stTree = new StackTraceTree(propertyService.getProjectName());
        vcs = ServicesFactory.getVersionControlService(propertyService.getVersionControl(),
                    propertyService.getPathToRepo(),
                    propertyService.getIssueTracker());
        its = ServicesFactory.getIssueTrackerService(propertyService.getUserName(),
                    propertyService.getPassword(),
                    vcs.getRepositoryOwner(),
                    propertyService.getProjectName(),
                    propertyService.getIssueTracker());
        numberOfTickets = its.getNumberOfTickets();
    }

    public TicketInfo handleTicket(final int ticketNumber) throws TicketCorruptedException,
            BlameInspectorException, VersionControlServiceException {
        TraceInfo traceInfo = null;
        String issueBody;
        String ticketURL = its.ticketUrl(ticketNumber);
        try {
           issueBody = its.getIssueBody(ticketNumber);
        }catch (Exception e){
            return new TicketInfo(ticketNumber, "Can not access ticket with such number!",
                    ticketURL) ;
        }
        String body = standartizeStackTrace(issueBody);
        while (traceInfo == null || traceInfo.getClassName() == null && body.length() != 0) {
            issueBody = body;
            issueBody = correctStackTrace(issueBody);
            body = issueBody;
            while (traceInfo == null || traceInfo.getClassName() == null && issueBody.length()!=0) {
                try {
                    traceInfo = parseIssueBody(issueBody, ticketNumber);
                    if(issueBody.length() <= 1) break;
                    issueBody = issueBody.substring(1);
                } catch (TicketCorruptedException e) {
                    if (e.getMessage().equals(NO_ENTRY)){
                        return new TicketInfo(ticketNumber, NO_ENTRY, ticketURL);
                    }
                    String words[]  = issueBody.split("\\s+");
                    if (words.length > 1 && issueBody.length() > (words[0].length() + 1)) {
                        issueBody = issueBody.substring(words[0].length() + 1);
                        continue;
                    }else {
                        return new TicketInfo(ticketNumber, NO_STACKTRACE, ticketURL);
                    }
                } catch (Exception e) {
                    throw new BlameInspectorException(e);
                }
            }
        }
        if (traceInfo == null){
            return new TicketInfo(ticketNumber, NO_STACKTRACE, ticketURL);
        }
        try {
            blameLogin = its.getUserLogin(vcs, traceInfo.getFileName(),traceInfo.getClassName(), traceInfo.getLineNumber());
        }catch (VersionControlServiceException e){
            return new TicketInfo(ticketNumber, "Can not do blame for this line!" , ticketURL);
        }catch (IssueTrackerException e){
            if (e.getMessage().equals("Can not get blame!")){
                return new TicketInfo(ticketNumber, "Can not get blame!", ticketURL);
            }
            throw new BlameInspectorException(e);
        } catch (Exception e){
            throw new BlameInspectorException(e);
        }
        return new TicketInfo(ticketNumber, blameLogin , ticketURL, its.assigneeUrl(blameLogin));
    }

    private String standartizeStackTrace(final String text){
        String stackTrace = text;
        stackTrace = stackTrace.replace(NBSP + ";", "");
        stackTrace = stackTrace.replace(LEFT_TAG_BRACKET, "<");
        stackTrace = stackTrace.replace(RIGHT_TAG_BRACKET, ">");
        return stackTrace;
    }

    private String correctStackTrace(final String issueBody) {
        if (!issueBody.contains(AT)) return issueBody;
        String finishLine = issueBody.substring(issueBody.lastIndexOf(AT));
        String stackTrace = issueBody.substring(0, issueBody.lastIndexOf(AT) + finishLine.indexOf(RIGHT_BRACKET) + 1);
        return stackTrace;
    }

    public TraceInfo parseIssueBody(final String issueBody, final int ticketNumber) throws TicketCorruptedException {
        String stackTrace = issueBody;
        if (stackTrace.isEmpty() && !stackTrace.contains(AT)){
            throw new TicketCorruptedException(NO_STACKTRACE);
        }
        return getTraceInfo(stackTrace, ticketNumber);
    }


    public void setAssignee() throws IssueTrackerException {
        if (blameLogin == null) return;
        try{
            its.setIssueAssignee(blameLogin);
        }catch (Exception e){
            throw new IssueTrackerException(e);
        }
    }

    public static TraceInfo getTraceInfo(final String issueBody, final int ticketNumber) throws TicketCorruptedException {
        NStackTrace stackTrace;
        PrintStream sysOut = System.out;
        PrintStream sysErr = System.err;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        try {
             stackTrace = StackTraceParser.parse(issueBody);
        } catch (NoSuchElementException | RecognitionException e){
             throw new TicketCorruptedException("StackTrace is corrupted!");
        }finally {
            System.setOut(sysOut);
            System.setErr(sysErr);
        }
        String [] locationInfo;
        if (stackTrace == null){
            throw new TicketCorruptedException(NO_STACKTRACE);
        }
        for (NFrame currentFrame :  stackTrace.getTrace().getFrames()){
            int size = currentFrame.getLocation().length();
            if(currentFrame.getLocation().indexOf(":") == -1) continue;
            locationInfo = currentFrame.getLocation().substring(1, size - 1).split(":");
            if (vcs.containsFile(locationInfo[0])){
                stTree.addTicket(stackTrace, ticketNumber);
                return new TraceInfo(currentFrame.getClassName(), currentFrame.getMethodName(),
                        locationInfo[0], Integer.parseInt(locationInfo[1]));
            }
        }
        if (stackTrace.getTrace().getFrames().size() == 0) {
            throw new TicketCorruptedException(NO_STACKTRACE);
        }else{
            throw new TicketCorruptedException(NO_ENTRY);
        }
    }

    public void refresh(){
        blameLogin = null;
        its.refresh();
    }

    public static int getNumberOfTickets() {
        return numberOfTickets;
    }

}
