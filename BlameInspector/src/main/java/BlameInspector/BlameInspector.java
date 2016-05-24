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
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public class BlameInspector implements Serializable{

    private final String AT = "at ";
    private final String RIGHT_BRACKET = ")";
    private final String LEFT_TAG_BRACKET = "&lt;";
    private final String RIGHT_TAG_BRACKET = "&gt;";
    private final String NBSP = "&nbsp";
    private final String NL = "\n";
    private final Pattern STACKTRACE_PATTERN = Pattern.compile(".+\\(.+(\\.java|\\.kt)?:\\d+\\)$");
    private static final long serialVersionUID = 1L;

    private static final String NO_STACKTRACE = "No StackTrace found in current ticket!";
    private static final String NO_ENTRY = "No entry of exception found in current repository.";

    private transient static StackTraceTree stTree;
    private transient static VersionControlService vcs;
    private transient static IssueTrackerService its;
    private static int numberOfTickets;
    private ArrayList<String> blameLogin;
    private static ArrayList<Integer> duples;

    private static ArrayList<TicketInfo> results;


    private static boolean isParsingCode;

    public BlameInspector(VersionControlService vcs, IssueTrackerService its, boolean parseProjectSources) throws VersionControlServiceException, IssueTrackerException {
        isParsingCode = parseProjectSources;
        results = new ArrayList<>();
        this.vcs = vcs;
        this.its = its;
        if (its != null) {
            numberOfTickets = its.getNumberOfTickets();
        }
    }

    public boolean isAssigned(final int ticketNumber){
        try {
            return its.assignee(ticketNumber) != null;
        }catch (IOException | NullPointerException e){
            return false;
        }

    }

    public boolean isStartingStackTrace(final String line){
        return STACKTRACE_PATTERN.matcher(line).matches();
    }

    public void setResults(ArrayList<TicketInfo> results){
        this.results = results;
    }

    public String properAssignee(final int ticketNumber) throws IOException {
         return its.assignee(ticketNumber);
    }

    public ArrayList<ArrayList<Integer>> getDuplicates() {
        return stTree.getDuplicates();
    }

    public ArrayList<TicketInfo> getResults() {
        return results;
    }

    public TicketInfo handleTicket(final int ticketNumber) throws TicketCorruptedException,
            BlameInspectorException, VersionControlServiceException {
        //System.out.println("ticket resolving :" + ticketNumber);
        ArrayList<TraceInfo> traces = new ArrayList<>();
        String issueBody = null;
        String ticketURL = its.ticketUrl(ticketNumber);
        String exceptionMessage = null;
        blameLogin = new ArrayList<>();
        try {
            issueBody = its.getIssueBody(ticketNumber);
        } catch (Exception e) {
            exceptionMessage = "Can not access ticket with such number!";
        }
        if (issueBody != null) {
            String body = standartizeStackTrace(issueBody);
            // the insert of smart parsing of stacktrace using pattern
            String lexems[] = body.split("\\s+");
            String stackTrace = "";
            synchronized (this) {
                for (int i = 1; i < lexems.length; i++) {
                    if (isStartingStackTrace(lexems[i]) && lexems[i - 1].equals("at")) {
                        stackTrace += AT + lexems[i] + "\n";
                    }
                }
            }
            //System.out.println("Stacktrace # " + ticketNumber + "is:");
            //System.out.println(stackTrace);
            if (stackTrace.isEmpty()){
                exceptionMessage = NO_STACKTRACE;
            }
            try {
                traces = parseIssueBody(stackTrace,ticketNumber);
            } catch (TicketCorruptedException e){
                exceptionMessage = e.getMessage();
            }
//            outerwhile:
//            while (traces.size() == 0 || traces.get(0) == null || traces.get(0).getClassName() == null && body.length() != 0) {
//                System.out.println("in while");
//                issueBody = body;
//                issueBody = correctStackTrace(issueBody);
//                body = issueBody;
//                while (traces.size() == 0 || traces.get(0) == null || traces.get(0).getClassName() == null && issueBody.length() != 0) {
//                    try {
//                        traces = parseIssueBody(issueBody, ticketNumber);
//                        if (issueBody.length() <= 1) break;
//                        issueBody = issueBody.substring(1);
//                    } catch (TicketCorruptedException e) {
//                        if (e.getMessage().equals(NO_ENTRY)) {
//                            exceptionMessage = NO_ENTRY;
//                            break outerwhile;
//                        }
//                        String words[] = issueBody.split("\\s+");
//                        if (words.length > 1 && issueBody.length() > (words[0].length() + 1)) {
//                            issueBody = issueBody.substring(words[0].length() + 1);
//                            continue;
//                        } else {
//                            exceptionMessage = NO_STACKTRACE;
//                            break outerwhile;
//                        }
//                    } catch (Exception e) {
//                        throw new BlameInspectorException(e);
//                    }
//                }
//            }
        }
        if ((traces.size() == 0 || traces.get(0) == null) && exceptionMessage == null) {
            exceptionMessage = NO_STACKTRACE;
        }
        try {
            if (exceptionMessage == null) {
                for (TraceInfo traceInfo : traces) {
                    BlamedUserInfo blamedUserInfo = vcs.getBlamedUserInfo(traceInfo.getFileName(),
                            traceInfo.getClassName(), traceInfo.getLineNumber());
                    //System.out.println("blamedUserInfo : "  + blamedUserInfo.getUserName() + " " +
                    //        blamedUserInfo.getUserEmail() + " " + blamedUserInfo.getUserCommitId());
                    if (!blamedUserInfo.isUseful() && !blameLogin.isEmpty()){
                       break;
                    }
                    blameLogin.add(its.getUserLogin(blamedUserInfo));
                }
            }
        } catch (VersionControlServiceException e) {
            exceptionMessage = "Can not do blame for this line!";
        } catch (IssueTrackerException e) {
            if (e.isCannotGetBlame()) {
                exceptionMessage = e.getMessage();
            } else if(e.getMessage().equals("Not enough info got from VCS")){
                exceptionMessage = e.getMessage();
            } else {
                throw new BlameInspectorException(e);
            }
        } catch (Exception e) {
            throw new BlameInspectorException(e);
        }
        TicketInfo ticketInfo;
        if (exceptionMessage == null) {
            ticketInfo = new TicketInfo(ticketNumber, blameLogin, ticketURL,
                    its.assigneeUrl(blameLogin), null, traces, issueBody);
        } else {
            ticketInfo = new TicketInfo(ticketNumber, exceptionMessage, ticketURL, issueBody);
        }
        return addingResult(ticketInfo);
    }

    public synchronized TicketInfo addingResult(final TicketInfo ticketInfo){
        results.add(ticketInfo);
        return ticketInfo;
    }

    private String standartizeStackTrace(final String text) {
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

    public ArrayList<TraceInfo> parseIssueBody(final String issueBody, final int ticketNumber) throws TicketCorruptedException {
        String stackTrace = issueBody;
        if (stackTrace.isEmpty() && !stackTrace.contains(AT)) {
            throw new TicketCorruptedException(NO_STACKTRACE);
        }
        return getTraceInfo(stackTrace, ticketNumber);
    }


    public void setAssignee() throws IssueTrackerException {
        if (blameLogin == null) return;
        try {
            its.setIssueAssignee(blameLogin.get(0));
        } catch (Exception e) {
            throw new IssueTrackerException(e);
        }
    }

    public synchronized static NStackTrace getParsedStackTrace(final String issueBody) throws TicketCorruptedException {
        NStackTrace stackTrace;
        PrintStream sysOut = System.out;
        PrintStream sysErr = System.err;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        try {
            stackTrace = StackTraceParser.parse(issueBody);
        } catch (NoSuchElementException | RecognitionException e) {
            throw new TicketCorruptedException("StackTrace is corrupted!");
        } finally {
            System.setOut(sysOut);
            System.setErr(sysErr);
        }
        return stackTrace;
    }

    public synchronized static ArrayList<TraceInfo> getTraceInfo(final String issueBody, final int ticketNumber) throws TicketCorruptedException {
        NStackTrace stackTrace = getParsedStackTrace(issueBody);
        String[] locationInfo;
        if (stackTrace == null) {
            throw new TicketCorruptedException(NO_STACKTRACE);
        }
        ArrayList<TraceInfo> traces = new ArrayList<>();
        for (NFrame currentFrame : stackTrace.getTrace().getFrames()) {
            int size = currentFrame.getLocation().length();
            if (currentFrame.getLocation().indexOf(":") == -1) continue;
            locationInfo = currentFrame.getLocation().substring(1, size - 1).split(":");
            if (vcs.containsFile(locationInfo[0])) {
                traces.add(new TraceInfo(currentFrame.getClassName(), currentFrame.getMethodName(),
                        locationInfo[0], Integer.parseInt(locationInfo[1]), currentFrame));
                continue;
            }
            if (!isParsingCode) {
                continue;
            }
            //System.out.println("handle ticket " + ticketNumber + "!");
            //System.out.println("class and method :" + currentFrame.getClassName() + " " + currentFrame.getMethodName());
            //String path = vcs.containsMethod(currentFrame.getClassName() + "." + currentFrame.getMethodName());
            String path = vcs.containsCode(currentFrame.getClassName(), currentFrame.getMethodName());
            if (path != null) {
                int lineNumber;
                try {
                    lineNumber = Integer.parseInt(locationInfo[1]);
                } catch (Exception e) {
                    lineNumber = getLine(path, currentFrame.getMethodName());
                }
                traces.add(new TraceInfo(currentFrame.getClassName(), currentFrame.getMethodName(),
                        path, lineNumber, currentFrame));
                continue;
            }
        }
        if (traces.size() > 0) {
            return traces;
        }
        if (stackTrace.getTrace().getFrames().size() == 0) {
            throw new TicketCorruptedException(NO_STACKTRACE);
        } else {
            throw new TicketCorruptedException(NO_ENTRY);
        }
    }

    private static int getLine(final String fileName, final String methodName) {
        BufferedReader buf = null;
        String line;
        int lineNumber = 0;
        try {
            buf = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(fileName))));
            while ((line = buf.readLine()) != null) {
                lineNumber++;
                if (line.contains(methodName)) {
                    return lineNumber;
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public void refresh() {
        blameLogin = null;
        its.refresh();
    }

    public static int getNumberOfTickets() {
        return numberOfTickets;
    }

}
