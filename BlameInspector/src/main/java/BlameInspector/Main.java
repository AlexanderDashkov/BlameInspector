package BlameInspector;

import BlameInspector.IssueTracker.IssueTrackerException;
import BlameInspector.VCS.VersionControlServiceException;
import org.apache.commons.cli.*;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class Main {

    private static BlameInspector blameInspector;
    private static PropertyService propertyService;

    private static String projectName;
    private static int startBound, endBound;
    private static boolean isInteractive, isSettingAssignee;

    private static PrintStream sysOut;

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
        } catch (VersionControlServiceException e) {
            printExceptionData(e, "Got exception in version control part.");
        } catch (IssueTrackerException e) {
            printExceptionData(e, "Got exception in issue tracker part.");
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
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("Something wrong with reading config file!");
            System.exit(0);
        } catch (ProjectNotFoundException e){
            e.printStackTrace();
            System.out.println("Project with such name wasn't found on corresponding file.");
            System.exit(0);
        } catch (SAXException e) {
            e.printStackTrace();
            System.out.println("Something wrong with XML config file!");
            System.exit(0);
        }
    }

    private static void processComandLine(String[] args) {
        Option projectNameOption = new Option("p", "project", true, "project name");
        projectNameOption.setArgs(1);
        projectNameOption.setArgName("project");
        Option ticketNumberOption = new Option("t", "ticket", true, "ticket number");
        ticketNumberOption.setArgs(1);
        ticketNumberOption.setArgName("number");
        Option ticketsRangeOption = new Option("r", "range", true, "tickets range");
        ticketsRangeOption.setArgs(2);
        ticketsRangeOption.setOptionalArg(true);
        ticketsRangeOption.setArgName("range bounds");
        OptionGroup fixKeys = new OptionGroup();
        fixKeys.addOption(new Option("f", "fix",false, "set assignee automaticaly"));
        fixKeys.addOption(new Option("s", "show", false, "just print assignee, no setting"));
        fixKeys.addOption(new Option("i", "interactive", false, "ask user whether set assignee"));

        Option helpOption = new Option("help", false, "help key");
        Options options = new Options();
        options.addOption(projectNameOption);
        options.addOption(ticketNumberOption);
        options.addOptionGroup(fixKeys);
        options.addOption(helpOption);
        options.addOption(ticketsRangeOption);

        CommandLineParser cmdLineParser = new PosixParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = cmdLineParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Wrong arguments, type -help for help");
            System.exit(0);
        }
        if (cmdLine.hasOption("-help")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("BlameInspector", options);
            System.exit(0);
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
        sysOut = System.out;
        PrintStream outTempStream = new PrintStream(new ByteArrayOutputStream());
        System.setOut(outTempStream);
        System.setErr(outTempStream);
        try {
            blameEmail = blameInspector.handleTicket(ticketNumber);
        } catch (VersionControlServiceException e) {
            System.setOut(sysOut);
            printExceptionData(e, "Got exception in version control part.");
        } catch (IssueTrackerException e) {
            System.setOut(sysOut);
            printExceptionData(e, "Got exception in issue tracker part.");
        } catch (TicketCorruptedException e) {
            System.setOut(sysOut);
            System.out.println("Ticket is corrupted!");
            throw e;
        }catch (BlameInspectorException e){
            System.setOut(sysOut);
            printExceptionData(e, "got blame inspection.");
        }catch(Exception e){
            System.setOut(sysOut);
            System.out.println("still exception");
            System.out.println(e);
        }finally {
            System.setOut(sysOut);
        }
        return blameEmail;
    }

    public static void assign(){
        try {
            blameInspector.setAssignee();
        }catch (IssueTrackerException e){
            printExceptionData(e, "Got exception in issue tracker part.");
        }

    }

    public static void printExceptionData(final BlameInspectorException e,
                                         final String message){
        System.out.println(message);
        System.out.println(e.getNestedException().getMessage());
        e.getNestedException().printStackTrace();
    }
}
