package blameinspector.reportprinters;


import blameinspector.TicketInfo;
import blameinspector.TraceInfo;
import com.jmolly.stacktraceparser.NFrame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;

public class ReportHtml implements IReportPrinter {

    private PrintWriter reportWriter;


    private int numberOfAllTickets;
    private int numberOfAssigned;

    public ReportHtml() throws FileNotFoundException, UnsupportedEncodingException {
        File reportFile = new File("report.html");
        reportWriter = new PrintWriter(reportFile, "UTF-8");
        reportWriter.print(IHtmlStructureStorage.HTML_START);
        numberOfAllTickets = 0;
        numberOfAssigned = 0;
    }

    @Override
    public void printTickets(ArrayList<TicketInfo> results) {
        for (TicketInfo ticketInfo : results) {
            printTicket(ticketInfo);
        }
    }

    private void printTicket(final TicketInfo ticketInfo) {
        String ticketNumber = String.valueOf(ticketInfo.getTicketNumber());
        if (ticketInfo.isAssigned()) {
            String link;
            String deepStackTrace = "";
            int i = 0;
            for (TraceInfo traceInfo : ticketInfo.getStackTrace()){
                link = "";
                String assignee = "";
                try {
                    link = ticketInfo.getAssigneeUrl().get(i);
                    assignee = ticketInfo.getAssignee().get(i);
                } catch (IndexOutOfBoundsException e) {
                }
                try {
                    NFrame frame = traceInfo.getFrame();
                    deepStackTrace += String.format("%-150s" ,frame.toPrettyString() + frame.getLocation())
                            + String.format("%40s%n", MessageFormat.format(IHtmlStructureStorage.HREF_ELEM, link,
                            assignee));
                }catch (Exception e){
                    e.printStackTrace();
                }
                i++;
            }
            deepStackTrace = deepStackTrace.replace(" ", "&nbsp");
            String dup = ticketInfo.getDupplicates().size() == 1 ? "No duplicates" : ticketInfo.getDupplicates().toString();
            reportWriter.print(MessageFormat.format(IHtmlStructureStorage.TABLE_ELEM,
                    ticketInfo.getTicketUrl(),
                    ticketNumber,
                    deepStackTrace,
                    "-", dup));
            numberOfAssigned++;
        } else {
            //reportWriter.print(MessageFormat.format(IHtmlStructureStorage.TABLE_ELEM,
            //        ticketInfo.getTicketUrl(),
            //        ticketNumber, "-", ticketInfo.getErrorType(), "none"));
        }
        numberOfAllTickets++;
    }

    public void printDuplicates(final ArrayList<ArrayList<Integer>> duplicates) {
        for (ArrayList<Integer> dupl : duplicates) {
            for (int ticket : dupl) {
                reportWriter.write(ticket);
            }
            reportWriter.println();
        }
    }

    @Override
    public void flush() {
        reportWriter.print(MessageFormat.format(IHtmlStructureStorage.HTML_END,
                String.valueOf(numberOfAllTickets), String.valueOf(numberOfAssigned)));
        reportWriter.close();
    }
}
