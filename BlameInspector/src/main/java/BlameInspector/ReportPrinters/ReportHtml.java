package BlameInspector.ReportPrinters;


import BlameInspector.TicketInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class ReportHtml implements ReportPrinter{

   private PrintWriter reportWriter;


    private int numberOfAllTickets;
    private int numberOfAssigned;

    public ReportHtml() throws FileNotFoundException, UnsupportedEncodingException {
        File reportFile = new File("report.html");
        reportWriter = new PrintWriter(reportFile, "UTF-8");
        reportWriter.print(HtmlStructureStorage.HTML_START);
        numberOfAllTickets = 0;
        numberOfAssigned = 0;
    }

    @Override
    public void printTicket(final TicketInfo ticketInfo) {
        String ticketNumber = String.valueOf(ticketInfo.getTicketNumber());
        if (ticketInfo.isAssigned()){
            reportWriter.print(MessageFormat.format(HtmlStructureStorage.TABLE_ELEM,
                    ticketInfo.getTicketUrl(),
                    ticketNumber,
                    ticketInfo.getAssigneeUrl(),
                    ticketInfo.getAssignee(),
                    "-"));
            numberOfAssigned++;
        } else {
            reportWriter.print(MessageFormat.format(HtmlStructureStorage.TABLE_ELEM,
                    ticketInfo.getTicketUrl(),
                    ticketNumber, "-", "none", ticketInfo.getErrorType().getMessage()));
        }
        numberOfAllTickets++;
    }

    @Override
    public void flush() {
        reportWriter.print(MessageFormat.format(HtmlStructureStorage.HTML_END,
                String.valueOf(numberOfAllTickets), String.valueOf(numberOfAssigned)));
        reportWriter.close();
    }
}
