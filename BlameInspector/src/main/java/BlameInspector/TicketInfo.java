package blameinspector;


import java.util.ArrayList;

public class TicketInfo {

    private boolean assigned;
    private int ticketNumber;
    private ArrayList<String> assignee;
    private ArrayList<TraceInfo> stackTrace;

    private ArrayList<String> assigneeUrl;
    private ArrayList<Integer> dupplicates;
    private String ticketUrl;

    private String errorType;

    private void init(final int ticketNumber, final boolean assigned,
                      final String ticketUrl) {
        this.ticketNumber = ticketNumber;
        this.assigned = assigned;
        this.ticketUrl = ticketUrl;
    }

    public TicketInfo(final int ticketNumber, final ArrayList<String> assignee, final String ticketUrl,
                      final ArrayList<String> assigneeUrl, ArrayList<Integer> dupl,
                      final ArrayList<TraceInfo> stackTrace) {
        init(ticketNumber, true, ticketUrl);
        this.assignee = assignee;
        this.assigneeUrl = assigneeUrl;
        this.dupplicates = dupl;
        this.stackTrace = stackTrace;
    }

    public TicketInfo(final int ticketNumber, final String e, final String ticketUrl) {
        init(ticketNumber, false, ticketUrl);
        this.errorType = e;
    }

    public void setDupplicates(ArrayList<Integer> dupplicates){
        this.dupplicates = dupplicates;
    }

    public boolean isAssigned() {
        return this.assigned;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public ArrayList<String> getAssignee() {
        return assignee;
    }

    public String getErrorType() {
        return errorType;
    }

    public ArrayList<String> getAssigneeUrl() {
        return assigneeUrl;
    }

    public String getTicketUrl() {
        return ticketUrl;
    }

    public ArrayList<Integer> getDupplicates() {
        return dupplicates;
    }

    public ArrayList<TraceInfo> getStackTrace(){
        return stackTrace;
    }
}
