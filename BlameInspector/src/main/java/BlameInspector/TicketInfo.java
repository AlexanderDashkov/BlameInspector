package BlameInspector;


public class TicketInfo {

    private boolean assigned;
    private int ticketNumber;
    private String assignee;

    private TicketCorruptedException errorType;

    private void init(final int ticketNumber, final boolean assigned){
        this.ticketNumber = ticketNumber;
        this.assigned = assigned;
    }

    public TicketInfo(final int ticketNumber, final String assignee){
        init(ticketNumber, true);
        this.assignee = assignee;
    }

    public TicketInfo(final int ticketNumber, final TicketCorruptedException e){
        init(ticketNumber, false);
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
}
