package blameinspector;

import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.reportprinters.IReportPrinter;
import blameinspector.reportprinters.ReportConsole;
import blameinspector.reportprinters.ReportHtml;
import blameinspector.vcs.VersionControlServiceException;
import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class Main {

    private static BlameInspector blameInspector;
    private static PropertyService propertyService;
    private static ArrayList<IReportPrinter> reportPrinters;

    private static String header = "Examples of usage:\n  " +
            "blameinspector -p MyProject -t 24  -- show probable assignee for 24 ticket on MyProject\n  " +
            "blameinspector -p MyProject -r 1 4 -f -X -- set assignee for tickets from 1 to 4 on MyProject\n " +
            "blameinspector -p MyProject -r 1 -f -X  -- set assignee for tickets from 1 until tickets end on MyProject" +
            " and show exception stacktrace if occurs. \n \n";
    private static String footer = "\nPlease report issues at https://github.com/JackSmithJunior/blameinspector/issues";

    private static final String PROJECT_IDENT = "p";
    private static final String TICKET_IDENT = "t";
    private static final String RANGE_IDENT = "r";
    private static final String DEBUG_IDENT = "X";
    private static final String GENERATE_HTML_IDENT = "g";
    private static final String FIX_IDENT = "f";
    private static final String INTER_IDENT = "i";
    private static final String HELP_IDENT = "help";
    private static final String SHOW_IDENT = "s";
    private static final String ALL_IDENT = "a";

    private static String projectName;
    private static int startBound, endBound;
    private static boolean isInteractive, isSettingAssignee;
    private static boolean isDebug;


    public static void main(final String [] args) {
        try {
            processComandLine(args);
            processConfigFile();
            processTickets();
        }catch (Exception e){
            printExceptionData(e);
            System.exit(0);
        }
    }

    public static void processTickets() throws IssueTrackerException, BlameInspectorException, VersionControlServiceException {
        try {
            blameInspector = new BlameInspector(propertyService);
            if (endBound == -1) {
                endBound = blameInspector.getNumberOfTickets();
            }
        } catch (Exception e){
            printExceptionData(e);
        }
        for (int i = startBound; i <= endBound; i++){
            try {
                evaluateTicket(i);
                if (!isInteractive){
                    if (isSettingAssignee) blameInspector.setAssignee();
                } else {
                    Scanner in = new Scanner(System.in);
                    System.out.println("Set assignee on that ticket?(y/n)");
                    if (in.next().equals("y")){
                        blameInspector.setAssignee();
                    }
                }
                blameInspector.refresh();
            } catch (TicketCorruptedException e){
                continue;
            }
        }
        for (IReportPrinter reportPrinter : reportPrinters){
                reportPrinter.flush();
        }
    }

    public static void processConfigFile() throws PropertyServiceException {
        propertyService = new PropertyService(projectName);
    }

    public static void processComandLine(final String [] args) throws FileNotFoundException, UnsupportedEncodingException {
        Option projectNameOption = new Option(PROJECT_IDENT, "project", true, "project name");
        projectNameOption.setArgs(1);
        projectNameOption.setArgName("project");
        projectNameOption.setRequired(true);
        Option debugOption = new Option(DEBUG_IDENT, false, "debug mode");
        Option generateReport = new Option(GENERATE_HTML_IDENT, false, "generate html report");
        generateReport.setArgs(0);
        debugOption.setArgs(0);
        OptionGroup ticketNumbersGroup  = new OptionGroup();
        Option allTicketsOption = new Option(ALL_IDENT, "all", false, "all ticket evaluating");
        allTicketsOption.setArgs(0);
        Option ticketNumberOption = new Option(TICKET_IDENT, "ticket", true, "ticket number");
        ticketNumberOption.setArgs(1);
        ticketNumberOption.setArgName("number");
        Option ticketsRangeOption = new Option(RANGE_IDENT, "range", true, "tickets range ");
        ticketsRangeOption.setArgs(2);
        ticketsRangeOption.setOptionalArg(true);
        ticketsRangeOption.setArgName("range bounds");
        ticketNumbersGroup.addOption(ticketNumberOption);
        ticketNumbersGroup.addOption(ticketsRangeOption);
        ticketNumbersGroup.addOption(allTicketsOption);
        ticketNumbersGroup.setRequired(true);
        OptionGroup fixKeys = new OptionGroup();
        fixKeys.addOption(new Option(FIX_IDENT, "fix", false, "set assignee automatically"));
        fixKeys.addOption(new Option(SHOW_IDENT, "show", false, "just print assignee, no setting (default)"));
        fixKeys.addOption(new Option(INTER_IDENT, "interactive", false, "ask user whether set assignee"));

        Option helpOption = new Option(HELP_IDENT, false, "help key");
        Options helpOptions = new Options();
        helpOptions.addOption(helpOption);

        Options options = new Options();
        options.addOption(projectNameOption);
        options.addOptionGroup(ticketNumbersGroup);
        options.addOptionGroup(fixKeys);
        options.addOption(debugOption);
        options.addOption(generateReport);
        options.addOption(helpOption);


        CommandLineParser cmdLineParser = new PosixParser();
        CommandLine cmdLine = null;

        try {
            cmdLine = cmdLineParser.parse(helpOptions, args);
            if (cmdLine.hasOption("-help")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                Comparator<Option> comparator = new Comparator<Option>() {
                    @Override
                    public int compare(final Option o1, final Option o2) {
                        String optsOrder = "ptrafisgXh";
                        return optsOrder.indexOf(o1.getOpt()) - optsOrder.indexOf(o2.getOpt());
                    }
                };
                helpFormatter.setOptionComparator(comparator);
                helpFormatter.printHelp("BlameInspector", header, options, footer, true);
                System.exit(0);
            }
        } catch (ParseException e) {}


        try {
            cmdLine = cmdLineParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println("Type -help for help");
            System.exit(0);
        }
        if (args.length == 0){
            System.out.println("No arguments provided, type -help for help");
            System.exit(0);
        }
        isDebug = false;
        if (cmdLine.hasOption("-X")){
            isDebug = true;
        }
        projectName = cmdLine.getOptionValue(PROJECT_IDENT);
        if (cmdLine.hasOption(RANGE_IDENT)){
            String bound[] = cmdLine.getOptionValues(RANGE_IDENT);
            startBound = Integer.parseInt(bound[0]);
            if (bound.length > 1) {
                endBound = Integer.parseInt(bound[1]);
            } else {
                endBound = -1;
            }
        } else if (cmdLine.hasOption(TICKET_IDENT)) {
            int ticketNumber = Integer.parseInt(cmdLine.getOptionValue(TICKET_IDENT));
            startBound = endBound = ticketNumber;
        } else {
            startBound = 1;
            endBound = -1;
        }
        isSettingAssignee = false;
        isInteractive = false;
        if (cmdLine.hasOption(FIX_IDENT)){
            isSettingAssignee = true;
        } else if (cmdLine.hasOption(INTER_IDENT)){
            isInteractive = true;
        }
        reportPrinters = new ArrayList<>();
        reportPrinters.add(new ReportConsole());
        if (cmdLine.hasOption(GENERATE_HTML_IDENT)){
           reportPrinters.add(new ReportHtml());
        }
    }

    public static void evaluateTicket(final int ticketNumber) throws TicketCorruptedException, BlameInspectorException, VersionControlServiceException {
       TicketInfo ticketInfo =  blameInspector.handleTicket(ticketNumber);
       for (IReportPrinter reportPrinter : reportPrinters){
            reportPrinter.printTicket(ticketInfo);
       }
    }


    public static void printExceptionData(final Exception e){
        System.out.println(e.getMessage());
        if (isDebug) {
            e.printStackTrace();
        }
    }
}
