package blameinspector.reportprinters;


import blameinspector.TicketInfo;

import java.text.MessageFormat;
import java.util.List;

public class ReportConsole implements IReportPrinter {

    private static final String TICKET_INFO = "Ticket # {0}. Assigned to {1}";
    private static final String TICKET_FAILED = "Ticket # {0} was not assigned due to: ";
    private static final String SUMMARY = "Summary:\n Tickets: All {0}, Assigned {1}";

    private int numberOfAllTickets;
    private int numberOfAssigned;

    public ReportConsole() {
        numberOfAllTickets = 0;
        numberOfAssigned = 0;
    }

    @Override
    public void printTickets(List<TicketInfo> results) {
        for (TicketInfo ticketInfo : results) {
            printTicket(ticketInfo);
        }
    }

    private void printTicket(final TicketInfo ticketInfo) {
        String ticketNumber = String.valueOf(ticketInfo.getTicketNumber());
        if (ticketInfo.isAssigned()) {
            System.out.println(MessageFormat.format(TICKET_INFO,
                    ticketNumber,
                    ticketInfo.getAssignee().get(0)));
            numberOfAssigned++;
            System.out.println(ticketInfo.getDupplicates().size());
        } else {
            System.out.println(MessageFormat.format(TICKET_FAILED,
                    ticketNumber) + ticketInfo.getErrorType());
        }
        numberOfAllTickets++;
    }

    @Override
    public void flush() {
        // System.out.println(MessageFormat.format(SUMMARY,
        //         numberOfAllTickets, numberOfAssigned));
    }
}
