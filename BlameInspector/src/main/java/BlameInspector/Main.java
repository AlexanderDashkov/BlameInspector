package BlameInspector;

import BlameInspector.IssueTracker.IssueTrackerException;
import BlameInspector.ReportPrinters.ReportConsole;
import BlameInspector.ReportPrinters.ReportHtml;
import BlameInspector.ReportPrinters.ReportPrinter;
import BlameInspector.VCS.VersionControlServiceException;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class Main {

    private static BlameInspector blameInspector;
    private static PropertyService propertyService;
    private static ArrayList<ReportPrinter> reportPrinters;

    private static String header = "Examples of usage:\n  " +
            "BlameInspector -p MyProject -t 24  -- show probable assignee for 24 ticket on MyProject\n  " +
            "BlameInspector -p MyProject -r 1 4 -f -X -- set assignee for tickets from 1 to 4 on MyProject\n " +
            "BlameInspector -p MyProject -r 1 -f -X  -- set assignee for tickets from 1 until tickets end on MyProject" +
            " and show exception stacktrace if occurs. \n \n";
    private static String footer = "\nPlease report issues at https://github.com/JackSmithJunior/BlameInspector/issues";

    private static String projectName;
    private static int startBound, endBound;
    private static boolean isInteractive, isSettingAssignee;
    private static boolean isDebug;



    public static void main(final String [] args) {
        processComandLine(args);
        processConfigFile();
        blameInspector = new BlameInspector();
        processTickets();
    }

    private static void processTickets() {
        try {
            blameInspector.init(propertyService);
            if (endBound == -1){
                endBound = blameInspector.getNumberOfTickets();
            }
        } catch (Exception e){
            printExceptionData(e);
        }
        for (int i = startBound; i <= endBound; i++){
            try {
                for (ReportPrinter reportPrinter : reportPrinters){
                    reportPrinter.printTicket(evaluateTicket(i));
                }
                if (!isInteractive){
                    if (isSettingAssignee) assign();
                } else {
                    Scanner in = new Scanner(System.in);
                    System.out.println("Set assignee on that ticket?(y/n)");
                    if (in.next().equals("y")){
                        assign();
                    }
                }
                blameInspector.refresh();
            } catch (TicketCorruptedException e){
                continue;
            }
        }
        for (ReportPrinter reportPrinter : reportPrinters){
                reportPrinter.flush();
        }
    }

    private static void processConfigFile() {
        try {
            propertyService = new PropertyService(projectName);
        } catch (Exception e){
            printExceptionData(e);
        }
    }

    private static void processComandLine(final String [] args) {
        Option projectNameOption = new Option("p", "project", true, "project name");
        projectNameOption.setArgs(1);
        projectNameOption.setArgName("project");
        projectNameOption.setRequired(true);
        Option debugOption = new Option("X", false, "debug mode");
        Option generateReport = new Option("g", false, "generate html report");
        generateReport.setArgs(0);
        debugOption.setArgs(0);
        OptionGroup ticketNumbersGroup  = new OptionGroup();
        Option allTicketsOption = new Option("a", "all", false, "all ticket evaluating");
        allTicketsOption.setArgs(0);
        Option ticketNumberOption = new Option("t", "ticket", true, "ticket number");
        ticketNumberOption.setArgs(1);
        ticketNumberOption.setArgName("number");
        Option ticketsRangeOption = new Option("r", "range", true, "tickets range ");
        ticketsRangeOption.setArgs(2);
        ticketsRangeOption.setOptionalArg(true);
        ticketsRangeOption.setArgName("range bounds");
        ticketNumbersGroup.addOption(ticketNumberOption);
        ticketNumbersGroup.addOption(ticketsRangeOption);
        ticketNumbersGroup.addOption(allTicketsOption);
        ticketNumbersGroup.setRequired(true);
        OptionGroup fixKeys = new OptionGroup();
        fixKeys.addOption(new Option("f", "fix", false, "set assignee automatically"));
        fixKeys.addOption(new Option("s", "show", false, "just print assignee, no setting (default)"));
        fixKeys.addOption(new Option("i", "interactive", false, "ask user whether set assignee"));

        Option helpOption = new Option("help", false, "help key");
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
        projectName = cmdLine.getOptionValue("p");
        if (cmdLine.hasOption("r")){
            String bound[] = cmdLine.getOptionValues("r");
            startBound = Integer.parseInt(bound[0]);
            if (bound.length > 1) {
                endBound = Integer.parseInt(bound[1]);
            } else {
                endBound = -1;
            }
        } else if (cmdLine.hasOption("t")) {
            int ticketNumber = Integer.parseInt(cmdLine.getOptionValue("t"));
            startBound = endBound = ticketNumber;
        } else {
            startBound = 1;
            endBound = -1;
        }
        isSettingAssignee = false;
        isInteractive = false;
        if (cmdLine.hasOption("f")){
            isSettingAssignee = true;
        } else if (cmdLine.hasOption("i")){
            isInteractive = true;
        }
        reportPrinters = new ArrayList<>();
        reportPrinters.add(new ReportConsole());
        if (cmdLine.hasOption("g")){
            try {
                reportPrinters.add(new ReportHtml());
            } catch (Exception e) {
                printExceptionData(e);
                System.exit(0);
            }
        }
    }

    public static TicketInfo evaluateTicket(final int ticketNumber) throws TicketCorruptedException {
        String blameEmail = null;
        try {
            blameEmail = blameInspector.handleTicket(ticketNumber);
        } catch (TicketCorruptedException e) {
            return new TicketInfo(ticketNumber, e);
        } catch (VersionControlServiceException e){
            if (e.getMessage().equals("Can not get blame for this line!")){
                return new TicketInfo(ticketNumber, new TicketCorruptedException(e.getMessage()));
            }
            printExceptionData(e);
        }catch (Exception e){
            printExceptionData(e);
        }
        return new TicketInfo(ticketNumber, blameEmail);
    }

    public static void assign(){
        try {
            blameInspector.setAssignee();
        }catch (IssueTrackerException e){
            printExceptionData(e);
        }

    }

    public static void printExceptionData(final Exception e){
        System.out.println(e.getMessage());
        if (isDebug){
            e.printStackTrace();
        }
        System.exit(0);
    }
}
