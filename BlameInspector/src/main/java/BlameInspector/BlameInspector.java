package BlameInspector;

import com.jmolly.stacktraceparser.NFrame;
import com.jmolly.stacktraceparser.NStackTrace;
import com.jmolly.stacktraceparser.StackTraceParser;
import org.antlr.runtime.RecognitionException;

import java.util.NoSuchElementException;

public class BlameInspector {

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
            throw new VersionControlServiceException(e);
        }
        try {
            its = ServicesFactory.getIssueTrackerService(propertyService.getUserName(),
                    propertyService.getPassword(),
                    vcs.getRepositoryOwner(),
                    propertyService.getProjectName(),
                    propertyService.getIssueTracker());
            numberOfTickets = its.getNumberOfTickets();
        }catch (Exception e){
            throw new IssueTrackerException(e);
        }

    }

    public String handleTicket(final int ticketNumber) throws TicketCorruptedException,
            VersionControlServiceException,
            IssueTrackerException {
        TraceInfo traceInfo = null;
        try {
            traceInfo = getTraceInfo(its.getIssueBody(ticketNumber));
        }catch (TicketCorruptedException e){
            throw e;
        }catch (Exception e) {
            throw new VersionControlServiceException(e);
        }
        try {
            blameEmail = vcs.getBlamedUser(traceInfo.getFileName(), traceInfo.getLineNumber());
        }catch (Exception e){
            throw new VersionControlServiceException(e);
        }
        return blameEmail;
    }

    public void setAssignee() throws IssueTrackerException {
        try{
            its.setIssueAssignee(blameEmail);
        }catch (Exception e){
            throw new IssueTrackerException(e);
        }
    }

    private static TraceInfo getTraceInfo(final String issueBody) throws TicketCorruptedException {
        NStackTrace stackTrace;
        try {
             stackTrace = StackTraceParser.parse(issueBody);
        } catch (NoSuchElementException | RecognitionException e){
             throw new TicketCorruptedException("StackTrace corrupted!");
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
        throw new TicketCorruptedException("No entry of exception found in current repository.");
    }

    public void refresh(){
        blameEmail = null;
        its.refresh();
    }

    public static int getNumberOfTickets() {
        return numberOfTickets;
    }

}
