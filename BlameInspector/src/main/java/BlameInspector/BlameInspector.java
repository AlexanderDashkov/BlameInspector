package BlameInspector;

import com.jmolly.stacktraceparser.NFrame;
import com.jmolly.stacktraceparser.NStackTrace;
import com.jmolly.stacktraceparser.StackTraceParser;
import org.antlr.runtime.RecognitionException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

import java.io.IOException;
import java.util.NoSuchElementException;


public class BlameInspector {

    private static VersionControlService vcs;
    private static IssueTrackerService its;

    public void init(final PropertyService propertyService) throws IOException, GitAPIException, JSONException, TicketCorruptedException {
        vcs = new GitService(propertyService.getPathToRepo(), propertyService.getIssueTracker());
        its = ServicesFactory.getIssueTrackerService(propertyService.getUserName(),
                propertyService.getPassword(),
                vcs.getRepositoryOwner(),
                propertyService.getProjectName(),
                propertyService.getIssueTracker());
    }

    public void handleTicket(final int ticketNumber) throws IOException, JSONException, GitAPIException, TicketCorruptedException {
        TraceInfo traceInfo = getTraceInfo(its.getIssueBody(ticketNumber));
        String blameEmail = vcs.getBlamedUser(traceInfo.getFileName(), traceInfo.getLineNumber());
        its.setIssueAssignee(blameEmail);
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

}
