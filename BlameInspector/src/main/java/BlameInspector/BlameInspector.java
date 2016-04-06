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

    private final String AT = "at ";
    private final String RIGHT_BRACKET = ")";
    private final String NBSP = "&nbsp";
    private final String NL = "\n";

    private static final String NO_STACKTRACE = "No StackTrace found in current ticket!";
    private static final String NO_ENTRY = "No entry of exception found in current repository.";

    private static VersionControlService vcs;
    private static IssueTrackerService its;
    private static int numberOfTickets;

    private String blameLogin;

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

    public TicketInfo handleTicket(final int ticketNumber) throws TicketCorruptedException,
            BlameInspectorException, VersionControlServiceException {
        TraceInfo traceInfo = null;
        String issueBody;
        String ticketURL = its.ticketUrl(ticketNumber);
        try {
           issueBody = its.getIssueBody(ticketNumber);
        }catch (Exception e){
            return new TicketInfo(ticketNumber, new TicketCorruptedException("Can not access ticket with such number!"),
                    ticketURL) ;
        }
        String body = issueBody;
        while (traceInfo == null || traceInfo.getClassName() == null && body.length() != 0) {
            issueBody = body;
            issueBody = correctStackTrace(issueBody);
            body = issueBody;
            while (traceInfo == null || traceInfo.getClassName() == null && issueBody.length()!=0) {
                try {
                    traceInfo = parseIssueBody(issueBody);
                    if(issueBody.length() == 0) break;
                    issueBody = issueBody.substring(1);
                } catch (TicketCorruptedException e) {
                    if (e.getMessage().equals(NO_ENTRY)){
                        return new TicketInfo(ticketNumber, new TicketCorruptedException(NO_ENTRY), ticketURL);
                    }
                    if (issueBody.length() != 0) {
                        issueBody = issueBody.substring(1);
                        continue;
                    }else {
                        return new TicketInfo(ticketNumber, new TicketCorruptedException(NO_STACKTRACE), ticketURL);
                    }
                } catch (Exception e) {
                    throw new BlameInspectorException(e);
                }
            }
        }
        if (traceInfo == null){
            return new TicketInfo(ticketNumber, new TicketCorruptedException(NO_STACKTRACE), ticketURL);
        }
        try {
            blameLogin = its.getUserLogin(vcs, traceInfo.getFileName(), traceInfo.getLineNumber());
        }catch (VersionControlServiceException e){
            return new TicketInfo(ticketNumber, new TicketCorruptedException("Can not do blame for this line!") , ticketURL);
        }catch (IssueTrackerException e){
            if (e.getMessage().equals("Can not get data for commit!")){
                return new TicketInfo(ticketNumber, new TicketCorruptedException("Can not get data for commit!") , ticketURL);
            }
            throw new BlameInspectorException(e);
        } catch (Exception e){
            throw new BlameInspectorException(e);
        }
        return new TicketInfo(ticketNumber, blameLogin , ticketURL, its.assigneeUrl(blameLogin));
    }

    private String correctStackTrace(final String issueBody) {
        if (!issueBody.contains(AT)) return issueBody;
        String finishLine = issueBody.substring(issueBody.lastIndexOf(AT));
        String stackTrace = issueBody.substring(0, issueBody.lastIndexOf(AT) + finishLine.indexOf(RIGHT_BRACKET) + 1);
        stackTrace = stackTrace.replace(NBSP + ";", "");
        return stackTrace;
    }

    public TraceInfo parseIssueBody(final String issueBody) throws TicketCorruptedException {
        String stackTrace = issueBody;
        if (stackTrace.isEmpty() && !stackTrace.contains(AT)){
            throw new TicketCorruptedException(NO_STACKTRACE);
        }
        return getTraceInfo(stackTrace);
    }


    public void setAssignee() throws IssueTrackerException {
        if (blameLogin == null) return;
        try{
            its.setIssueAssignee(blameLogin);
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
        if (stackTrace == null){
            throw new TicketCorruptedException(NO_STACKTRACE);
        }
        for (NFrame currentFrame :  stackTrace.getTrace().getFrames()){
            int size = currentFrame.getLocation().length();
            locationInfo = currentFrame.getLocation().substring(1, size - 1).split(":");
            if (vcs.containsFile(locationInfo[0])){
                return new TraceInfo(currentFrame.getClassName(), currentFrame.getMethodName(),
                        locationInfo[0], Integer.parseInt(locationInfo[1]));
            }
        }
        if (stackTrace.getTrace().getFrames().size()==0) {
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
