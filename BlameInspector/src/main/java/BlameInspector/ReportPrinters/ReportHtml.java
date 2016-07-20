package blameinspector.reportprinters;


import blameinspector.TicketInfo;
import blameinspector.TraceInfo;
import blameinspector.issuetracker.IssueTrackerService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportHtml implements IReportPrinter {

    private PrintWriter reportWriter;
    private String projectName;
    private IssueTrackerService its;
    private String htmlResult;
    private Date date;


    private int numberOfAllTickets;
    private int numberOfAssigned;

    public ReportHtml(final String projectName, final Date dbDate) throws FileNotFoundException, UnsupportedEncodingException {
        htmlResult = "";
        File reportFile = new File("report.html");
        reportWriter = new PrintWriter(reportFile, "UTF-8");
        reportWriter.print(IHtmlStructureStorage.HTML_HEAD);
        htmlResult += IHtmlStructureStorage.HTML_HEAD;
        String date = " Date : " + dbDate.toString();
        reportWriter.print(MessageFormat.format(IHtmlStructureStorage.HTML_START, String.valueOf(projectName) + date ));
        htmlResult+= MessageFormat.format(IHtmlStructureStorage.HTML_START, String.valueOf(projectName) + date);
        numberOfAllTickets = 0;
        numberOfAssigned = 0;
    }

    public void setWriter(PrintWriter writer){
        PrintWriter prevWriter = reportWriter;
        this.reportWriter = writer;
        if (prevWriter != null) {
            reportWriter.print(IHtmlStructureStorage.HTML_HEAD);
            String date = " Date : " + new Date().toString();
            reportWriter.print(MessageFormat.format(IHtmlStructureStorage.HTML_START, String.valueOf(projectName) + date));
        }
    }

    public String getHtmlResult(){
        return htmlResult;
    }

    @Override
    public synchronized void printTickets(List<TicketInfo> results) {
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
                    deepStackTrace += traceInfo.getStackTraceLine() + "<br>";
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
            if (ticketInfo.getDupplicates() == null || ticketInfo.getDupplicates().size() == 1){
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
            if (ticketInfo.getDupplicates() != null) {
                for (int number : ticketInfo.getDupplicates()) {
                    String assignee = "";
                    try {
                        assignee = its.assignee(number);
                        if (topAssignee.contains(assignee.toLowerCase())) {
                            dupAssignees += "<b>" + assignee + "</b>\n";
                        } else {
                            dupAssignees += assignee + "\n";
                        }
                    } catch (Exception e) {
                    }
                }
            }
            //String dup = ticketInfo.getDupplicates().size() == 1 ? "No duplicates" : ticketInfo.getDupplicates().toString();
            String output = MessageFormat.format(IHtmlStructureStorage.TABLE_ELEM,
                    ticketInfo.getTicketUrl(),
                    "t" + String.valueOf(ticketNumber),
                    ticketNumber,
                    deepStackTrace,
                    assignees,
                    "-", dupU, dupR, dupAssignees);
            reportWriter.print(output);
            htmlResult += output;
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
                htmlResult += ticket;
            }
            reportWriter.println();
        }
    }

    @Override
    public void flush() {
        String output = MessageFormat.format(IHtmlStructureStorage.HTML_END,
                String.valueOf(numberOfAllTickets), String.valueOf(numberOfAssigned));
        reportWriter.print(output);
        htmlResult += output;
        numberOfAllTickets = 0;
        numberOfAssigned = 0;
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
