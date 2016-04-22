package blameinspector;

import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.issuetracker.IssueTrackerService;
import blameinspector.vcs.BlamedUserInfo;
import blameinspector.vcs.VersionControlService;
import blameinspector.vcs.VersionControlServiceException;
import com.jmolly.stacktraceparser.NFrame;
import com.jmolly.stacktraceparser.NStackTrace;
import com.jmolly.stacktraceparser.StackTraceParser;
import org.antlr.runtime.RecognitionException;

import java.io.*;
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
        String issueBody = null;
        String ticketURL = its.ticketUrl(ticketNumber);
        String exceptionMessage = null;
        try {
           issueBody = its.getIssueBody(ticketNumber);
        }catch (Exception e){
            exceptionMessage = "Can not access ticket with such number!";
        }
        if(issueBody != null) {
            String body = standartizeStackTrace(issueBody);
            outerwhile:
            while (traceInfo == null || traceInfo.getClassName() == null && body.length() != 0) {
                issueBody = body;
                issueBody = correctStackTrace(issueBody);
                body = issueBody;
                while (traceInfo == null || traceInfo.getClassName() == null && issueBody.length() != 0) {
                    try {
                        traceInfo = parseIssueBody(issueBody, ticketNumber);
                        if (issueBody.length() <= 1) break;
                        issueBody = issueBody.substring(1);
                    } catch (TicketCorruptedException e) {
                        if (e.getMessage().equals(NO_ENTRY)) {
                            exceptionMessage = NO_ENTRY;
                            break outerwhile;
                        }
                        String words[] = issueBody.split("\\s+");
                        if (words.length > 1 && issueBody.length() > (words[0].length() + 1)) {
                            issueBody = issueBody.substring(words[0].length() + 1);
                            continue;
                        } else {
                            exceptionMessage = NO_STACKTRACE;
                            break outerwhile;
                        }
                    } catch (Exception e) {
                        throw new BlameInspectorException(e);
                    }
                }
            }
        }
        if (traceInfo == null && exceptionMessage == null){
            exceptionMessage = NO_STACKTRACE;
        }
        try {
            if(exceptionMessage == null){
               BlamedUserInfo blamedUserInfo = vcs.getBlamedUserInfo(traceInfo.getFileName(),
                       traceInfo.getClassName(), traceInfo.getLineNumber());
               blameLogin = its.getUserLogin(blamedUserInfo);
            }
        }catch (VersionControlServiceException e){
            exceptionMessage = "Can not do blame for this line!";
        }catch (IssueTrackerException e){
            if (e.isCannotGetBlame()){
                exceptionMessage = e.getMessage();
            }else{
                throw new BlameInspectorException(e);
            }
        } catch (Exception e){
            throw new BlameInspectorException(e);
        }
        if(exceptionMessage == null){
            return new TicketInfo(ticketNumber, blameLogin , ticketURL, its.assigneeUrl(blameLogin));
        } else {
            return new TicketInfo(ticketNumber, exceptionMessage, ticketURL);
        }
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
            String path = vcs.containsCode(currentFrame.getClassName(), currentFrame.getMethodName());
            if (path != null) {
                stTree.addTicket(stackTrace, ticketNumber);
                int lineNumber;
                try {
                    lineNumber = Integer.parseInt(locationInfo[1]);
                }catch (Exception e){
                    lineNumber = getLine(path, currentFrame.getMethodName());
                }
                return new TraceInfo(currentFrame.getClassName(), currentFrame.getMethodName(),
                        path, lineNumber);
            }
        }
        if (stackTrace.getTrace().getFrames().size() == 0) {
            throw new TicketCorruptedException(NO_STACKTRACE);
        }else{
            throw new TicketCorruptedException(NO_ENTRY);
        }
    }

    private static int getLine(final String fileName, final String methodName) {
        BufferedReader buf = null;
        String line;
        int lineNumber = 0;
        try {
            buf = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(fileName))));
            while ((line = buf.readLine()) != null)   {
                lineNumber++;
                if (line.contains(methodName)) {
                    return lineNumber;
                }
            }
        } catch (Exception e) {}
        return -1;
    }

    public void refresh(){
        blameLogin = null;
        its.refresh();
    }

    public static int getNumberOfTickets() {
        return numberOfTickets;
    }

}
