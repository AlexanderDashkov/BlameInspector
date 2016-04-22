package blameinspector.reportprinters;

import blameinspector.TicketInfo;

public interface IReportPrinter {
    void printTicket(final TicketInfo ticketInfo);

    void flush();
}
