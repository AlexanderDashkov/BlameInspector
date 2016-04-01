package BlameInspector;

import BlameInspector.IssueTracker.IssueTrackerException;
import BlameInspector.VCS.VersionControlServiceException;
import org.apache.commons.cli.*;

import java.util.Comparator;
import java.util.Scanner;

public class Main {

    private static BlameInspector blameInspector;
    private static PropertyService propertyService;

    private static String projectName;
    private static int startBound, endBound;
    private static boolean isInteractive, isSettingAssignee;
    private static boolean isDebug;



    public static void main(String [] args) {
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
                System.out.println("Ticket number: " + i + " Assignee:  " + evaluateTicket(i));
                if (!isInteractive){
                    if (isSettingAssignee) assign();
                }else {
                    Scanner in = new Scanner(System.in);
                    System.out.println("Set assignee on that ticket?(y/n)");
                    if (in.next().equals("y")){
                        assign();
                    }
                }
                blameInspector.refresh();
            }catch (TicketCorruptedException e){
                continue;
            }
        }
    }

    private static void processConfigFile() {
        try {
            propertyService = new PropertyService(projectName);
        } catch (Exception e){
            printExceptionData(e);
        }
    }

    private static void processComandLine(String[] args) {
        Option projectNameOption = new Option("p", "project", true, "project name");
        projectNameOption.setArgs(1);
        projectNameOption.setArgName("project");
        projectNameOption.setRequired(true);
        Option debugOption = new Option("X", false, "debug mode");
        debugOption.setArgs(0);
        OptionGroup ticketNumbersGroup  = new OptionGroup();
        Option ticketNumberOption = new Option("t", "ticket", true, "ticket number");
        ticketNumberOption.setArgs(1);
        ticketNumberOption.setArgName("number");
        Option ticketsRangeOption = new Option("r", "range", true, "tickets range ");
        ticketsRangeOption.setArgs(2);
        ticketsRangeOption.setOptionalArg(true);
        ticketsRangeOption.setArgName("range bounds");
        ticketNumbersGroup.addOption(ticketNumberOption);
        ticketNumbersGroup.addOption(ticketsRangeOption);
        ticketNumbersGroup.setRequired(true);
        OptionGroup fixKeys = new OptionGroup();
        fixKeys.addOption(new Option("f", "fix",false, "set assignee automatically"));
        fixKeys.addOption(new Option("s", "show", false, "just print assignee, no setting"));
        fixKeys.addOption(new Option("i", "interactive", false, "ask user whether set assignee"));

        Option helpOption = new Option("help", false, "help key");
        Options helpOptions = new Options();
        helpOptions.addOption(helpOption);

        Options options = new Options();
        options.addOption(projectNameOption);
        options.addOptionGroup(ticketNumbersGroup);
        options.addOptionGroup(fixKeys);
        options.addOption(debugOption);


        CommandLineParser cmdLineParser = new PosixParser();
        CommandLine cmdLine = null;

        try {
            cmdLine = cmdLineParser.parse(helpOptions, args);
            if (cmdLine.hasOption("-help")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                Comparator<Option> comparator = new Comparator<Option>() {
                    @Override
                    public int compare(Option o1, Option o2) {
                        String OPTS_ORDER = "ptrfisX";
                        return OPTS_ORDER.indexOf(o1.getOpt()) - OPTS_ORDER.indexOf(o2.getOpt());
                    }
                };
                helpFormatter.setOptionComparator(comparator);
                helpFormatter.printHelp("BlameInspector", options, true);
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
            if(bound.length > 1) {
                endBound = Integer.parseInt(bound[1]);
            } else {
                endBound = -1;
            }
        } else {
            int ticketNumber = Integer.parseInt(cmdLine.getOptionValue("t"));
            startBound = endBound = ticketNumber;
        }
        isSettingAssignee = false;
        isInteractive = false;
        if (cmdLine.hasOption("f")){
            isSettingAssignee = true;
        }else if (cmdLine.hasOption("i")){
            isInteractive = true;
        }
    }

    public static String evaluateTicket(final int ticketNumber) throws TicketCorruptedException {
        String blameEmail = null;
        try {
            blameEmail = blameInspector.handleTicket(ticketNumber);
        } catch (TicketCorruptedException e) {
            return e.getMessage();
        } catch (VersionControlServiceException e){
            if (e.getMessage().equals("Can not get blame for this line!")) return e.getMessage();
            printExceptionData(e);
        }catch (Exception e){
            printExceptionData(e);
        }
        return blameEmail;
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
