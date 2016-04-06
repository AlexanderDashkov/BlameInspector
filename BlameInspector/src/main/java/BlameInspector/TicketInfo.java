package BlameInspector;


public class TicketInfo {

    private boolean assigned;
    private int ticketNumber;
    private String assignee;

    private String assigneeUrl;
    private String ticketUrl;

    private TicketCorruptedException errorType;

    private void init(final int ticketNumber, final boolean assigned, final String ticketUrl){
        this.ticketNumber = ticketNumber;
        this.assigned = assigned;
        this.ticketUrl = ticketUrl;
    }

    public TicketInfo(final int ticketNumber, final String assignee, final String ticketUrl, final String assigneeUrl){
        init(ticketNumber, true, ticketUrl);
        this.assignee = assignee;
        this.assigneeUrl = assigneeUrl;
    }

    public TicketInfo(final int ticketNumber, final TicketCorruptedException e, final String ticketUrl){
        init(ticketNumber, false, ticketUrl);
        this.errorType = e;
    }

    public boolean isAssigned(){
        return this.assigned;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public String getAssignee() {
        return assignee;
    }

    public TicketCorruptedException getErrorType() {
        return errorType;
    }

    public String getAssigneeUrl() {
        return assigneeUrl;
    }

    public String getTicketUrl() {
        return ticketUrl;
    }
}
