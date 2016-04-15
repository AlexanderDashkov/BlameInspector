package BlameInspector.ReportPrinters;

import BlameInspector.TicketInfo;

public interface ReportPrinter {
    void printTicket(final TicketInfo ticketInfo);
    void flush();
}
