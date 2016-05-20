package blameinspector;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class TicketInfo implements Serializable, Comparator<TicketInfo>, Comparable<TicketInfo> {

    private boolean assigned;
    private int ticketNumber;
    private ArrayList<String> assignee;
    private ArrayList<TraceInfo> stackTrace;

    private ArrayList<String> assigneeUrl;
    private ArrayList<Integer> dupplicates;
    private String ticketUrl;
    private HashMap<String, Integer> assigneePoints;

    private String errorType;

    private void init(final int ticketNumber, final boolean assigned,
                      final String ticketUrl) {
        this.ticketNumber = ticketNumber;
        this.assigned = assigned;
        this.ticketUrl = ticketUrl;
        assigneePoints = new HashMap<String, Integer>();
    }

    public TicketInfo(final int ticketNumber, final ArrayList<String> assignee, final String ticketUrl,
                      final ArrayList<String> assigneeUrl, ArrayList<Integer> dupl,
                      final ArrayList<TraceInfo> stackTrace) {
        init(ticketNumber, true, ticketUrl);
        this.assignee = assignee;
        for (String as : assignee){
            assigneePoints.put(as, 1);
        }
        this.assigneeUrl = assigneeUrl;
        this.dupplicates = dupl;
        this.stackTrace = stackTrace;
    }

    public TicketInfo(final int ticketNumber, final String e, final String ticketUrl) {
        init(ticketNumber, false, ticketUrl);
        this.errorType = e;
    }

    public void addAssignee(final String as){
        int points = 0;
        if (assigneePoints.containsKey(as.toLowerCase())){
            points = assigneePoints.get(as);
        }
        assigneePoints.put(as, ++points);
    }

    public String getTopAssignee(){
        int max = 0;
        String result = null;
        for (String as : assigneePoints.keySet()){
            if (max < assigneePoints.get(as)){
                max = assigneePoints.get(as);
                result = as;
            }
        }
        return result;
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

    @Override
    public int compareTo(TicketInfo o) {
        return this.ticketNumber > o.ticketNumber ? 1 : this.ticketNumber == o.ticketNumber ? 0 : -1;
    }

    @Override
    public int compare(TicketInfo o1, TicketInfo o2) {
        return o1.ticketNumber - o2.ticketNumber;
    }
}
