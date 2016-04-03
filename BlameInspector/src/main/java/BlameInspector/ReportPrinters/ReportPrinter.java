package BlameInspector.ReportPrinters;

import BlameInspector.TicketInfo;

public interface ReportPrinter {
    public void printTicket(final TicketInfo ticketInfo);
    public void flush();
}
