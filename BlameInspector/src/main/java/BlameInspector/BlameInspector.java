package BlameInspector;

import BlameInspector.IssueTracker.IssueTrackerException;
import BlameInspector.IssueTracker.IssueTrackerService;
import BlameInspector.VCS.VersionControlService;
import BlameInspector.VCS.VersionControlServiceException;
import com.jmolly.stacktraceparser.NFrame;
import com.jmolly.stacktraceparser.NStackTrace;
import com.jmolly.stacktraceparser.StackTraceParser;
import org.antlr.runtime.RecognitionException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

public class BlameInspector {

    private final String STACKTRACE_START = "Exception in thread";
    private final String AT = "at";
    private final String RIGHT_BRACKET = ")";

    private final String NO_STACKTRACE = "No StackTrace found in current ticket!";
    private static final String NO_ENTRY = "No entry of exception found in current repository.";

    private static VersionControlService vcs;
    private static IssueTrackerService its;
    private static int numberOfTickets;

    private String blameEmail;

    public void init(final PropertyService propertyService) throws VersionControlServiceException, IssueTrackerException {
        try {
            vcs = ServicesFactory.getVersionControlService(propertyService.getVersionControl(),
                    propertyService.getPathToRepo(),
                    propertyService.getIssueTracker(),
                    propertyService.getUserName(),
                    propertyService.getPassword());
        }catch (Exception e){
            throw new VersionControlServiceException(e, "Can not create VCS object!");
        }
        try {
            its = ServicesFactory.getIssueTrackerService(propertyService.getUserName(),
                    propertyService.getPassword(),
                    vcs.getRepositoryOwner(),
                    propertyService.getProjectName(),
                    propertyService.getIssueTracker());
            numberOfTickets = its.getNumberOfTickets();
        }catch (Exception e){
            throw new IssueTrackerException(e, "Can not create IssueTracker object!");
        }

    }

    public String handleTicket(final int ticketNumber) throws TicketCorruptedException,
            BlameInspectorException, VersionControlServiceException {
        TraceInfo traceInfo = null;
        String issueBody;
        try {
           issueBody = its.getIssueBody(ticketNumber);
        }catch (Exception e){
            throw new BlameInspectorException(e, "No ticket with such number!");
        }
        while (traceInfo == null) {
            try {
                issueBody = correctStackTrace(issueBody);
                traceInfo = parseIssueBody(issueBody);
            } catch (TicketCorruptedException e) {
                throw e;
            } catch (Exception e) {
                throw new BlameInspectorException(e);
            }
        }
        try {
            blameEmail = vcs.getBlamedUser(traceInfo.getFileName(), traceInfo.getLineNumber());
        }catch (Exception e){
            throw new VersionControlServiceException(e);
        }
        return blameEmail;
    }

    private String correctStackTrace(final String issueBody) {
        if (!issueBody.contains(AT)) return issueBody;
        String finishLine = issueBody.substring(issueBody.lastIndexOf(AT));
        String stackTrace = issueBody.substring(0, issueBody.lastIndexOf(AT) + finishLine.indexOf(RIGHT_BRACKET) - 1);
        return stackTrace;
    }

    public TraceInfo parseIssueBody(final String issueBody) throws TicketCorruptedException {
        String stackTrace = getStackTrace(issueBody);
        if (stackTrace.isEmpty() && !stackTrace.contains(AT)){
            throw new TicketCorruptedException(NO_STACKTRACE);
        }
        return getTraceInfo(stackTrace);
    }

    public String getStackTrace(final String issueBody) {
        if (!issueBody.contains(STACKTRACE_START)){
            return "";
        }
        return issueBody.substring(issueBody.indexOf(STACKTRACE_START));
    }

    public void setAssignee() throws IssueTrackerException {
        try{
            its.setIssueAssignee(blameEmail);
        }catch (Exception e){
            throw new IssueTrackerException(e);
        }
    }

    public static TraceInfo getTraceInfo(final String issueBody) throws TicketCorruptedException {
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
        for (NFrame currentFrame :  stackTrace.getTrace().getFrames()){
            int size = currentFrame.getLocation().length();
            locationInfo = currentFrame.getLocation().substring(1, size - 1).split(":");
            if (vcs.containsFile(locationInfo[0])){
                return new TraceInfo(currentFrame.getClassName(), currentFrame.getMethodName(),
                        locationInfo[0], Integer.parseInt(locationInfo[1]));
            }
        }
        throw new TicketCorruptedException(NO_ENTRY);
    }

    public void refresh(){
        blameEmail = null;
        its.refresh();
    }

    public static int getNumberOfTickets() {
        return numberOfTickets;
    }

}
