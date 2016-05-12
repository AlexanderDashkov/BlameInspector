package blameinspector.reportprinters;


import blameinspector.TicketInfo;
import blameinspector.TraceInfo;
import blameinspector.issuetracker.IssueTrackerService;
import com.jmolly.stacktraceparser.NFrame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;

public class ReportHtml implements IReportPrinter {

    private PrintWriter reportWriter;
    private String projectName;
    private IssueTrackerService its;


    private int numberOfAllTickets;
    private int numberOfAssigned;

    public ReportHtml(final String projectName) throws FileNotFoundException, UnsupportedEncodingException {
        File reportFile = new File("report.html");
        reportWriter = new PrintWriter(reportFile, "UTF-8");
        reportWriter.print(IHtmlStructureStorage.HTML_HEAD);
        reportWriter.print(MessageFormat.format(IHtmlStructureStorage.HTML_START, String.valueOf(projectName)));
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
            String assignees = "";
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
                    deepStackTrace += frame.toPrettyString() + frame.getLocation() + "<br>";
                    assignees +=  MessageFormat.format(IHtmlStructureStorage.HREF_ELEM, link,
                            assignee) + "\n";
                }catch (Exception e){
                    e.printStackTrace();
                }
                i++;
            }
            deepStackTrace = deepStackTrace.replace(" ", "&nbsp");
            String dupU = "";
            String dupR = "";
            String dupAssignees = "";
            if (ticketInfo.getDupplicates().size() == 1){
                dupU = "No duplicates";
                dupR = "No duplicates";
            }else {
                for (int number :ticketInfo.getDupplicates()){
                    String assignee = "";
                    try{
                        assignee = its.assignee(number);
                        ticketInfo.addAssignee(assignee);
                    }catch (Exception e){
                    }
                    if (assignee != "" && assignee!=null){
                        dupR += MessageFormat.format(IHtmlStructureStorage.HREF_ELEM, "#t"+String.valueOf(number) , number) + " ";
                    }else {
                        dupU += MessageFormat.format(IHtmlStructureStorage.HREF_ELEM, "#t"+String.valueOf(number) , number) + " ";
                    }
                }
            }
            String topAssignee = ticketInfo.getTopAssignee().toLowerCase();
            //System.out.println("topAssignee : " + topAssignee);
            for (int number :ticketInfo.getDupplicates()) {
                String assignee = "";
                try {
                    assignee = its.assignee(number);
                    if (topAssignee.contains(assignee.toLowerCase())){
                        dupAssignees += "<b>" + assignee + "</b>\n";
                    }else {
                        dupAssignees += assignee + "\n";
                    }
                } catch (Exception e) {
                }
            }
            //String dup = ticketInfo.getDupplicates().size() == 1 ? "No duplicates" : ticketInfo.getDupplicates().toString();
            reportWriter.print(MessageFormat.format(IHtmlStructureStorage.TABLE_ELEM,
                    ticketInfo.getTicketUrl(),
                    "t" + String.valueOf(ticketNumber),
                    ticketNumber,
                    deepStackTrace,
                    assignees,
                    "-", dupU, dupR, dupAssignees));
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setIts(IssueTrackerService its) {
        this.its = its;
    }
}
