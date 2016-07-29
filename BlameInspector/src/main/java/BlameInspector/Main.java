package blameinspector;

import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.reportprinters.IReportPrinter;
import blameinspector.reportprinters.ReportConsole;
import blameinspector.reportprinters.ReportHtml;
import blameinspector.vcs.VersionControlServiceException;
import org.apache.commons.cli.*;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main{

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final Logger LOGGER_PERFORMANCE = Logger.getLogger(Main.class.getName());
    private static Manager manager;
    private static PropertyService propertyService;
    private static ArrayList<IReportPrinter> reportPrinters;
    private static Server server;
    private static String date;

    private static String header = "Examples of usage:\n  " +
            "blameinspector -p MyProject -t 24  -- show probable assignee for 24 ticket on MyProject\n  " +
            "blameinspector -p MyProject -r 1 4 -f -X -- set assignee for tickets from 1 to 4 on MyProject\n " +
            "blameinspector -p MyProject -r 1 -f -X  -- set assignee for tickets from 1 until tickets end on MyProject" +
            " and show exception stacktrace if occurs. \n \n";
    private static String footer = "\nPlease report issues at https://github.com/JackSmithJunior/blameinspector/issues";
    private static String workingDirectory = System.getProperty("user.dir");
    private static String LOG_PATH = workingDirectory + File.separator + "workspace" + File.separator + "Logging" + File.separator;


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
    private static final String PARSE_PROJECT_IDENT = "d";
    private static final String STORED_RES_IDENT = "o";

    private static String projectName;
    private static int startBound, endBound;
    private static boolean isInteractive, isSettingAssignee;
    private static boolean isDebug;
    private static boolean parseProjectSources;
    private static boolean useDb;

    final static CountDownLatch latch = new CountDownLatch(1);

    public static void main(final String[] args) {
        try {
            Files.createDirectories(Paths.get(LOG_PATH));
            configureLogging();
            LOGGER.fine("in main function of main!");
            setDate(new Date().toString());

            long start = System.currentTimeMillis();
            processComandLine(args);
            processConfigFile();
            processTickets();
            printResults(null);
            long elapsedTime = System.currentTimeMillis() - start;
            LOGGER_PERFORMANCE.severe("Elapsed time is : " +  elapsedTime + " ms");

            server = new Server(9090);
            server.getConnectors()[0].getConnectionFactory(HttpConnectionFactory.class).setHttpCompliance(HttpCompliance.LEGACY);
            server.setHandler(manager);

            server.start();

            Timer timer = new Timer();
            TimerTask timerTask = new TimerTaskImpl();
            timer.schedule(timerTask, 10);
            latch.await();
            timer.cancel();

            System.out.println("timer has worked!");
            server.join();
            System.out.println("server finished!");
        } catch (Exception e) {
            printExceptionData(e);
            System.exit(0);
        }
    }

    private static void configureLogging() throws IOException {
        FileHandler fileHandler = new FileHandler(LOG_PATH + "logging.txt", true);
        fileHandler.setLevel(Level.FINEST);
        fileHandler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(fileHandler);
        LOGGER.setLevel(Level.FINEST);
        FileHandler fileHandler1 = new FileHandler(LOG_PATH + "log_performance.txt", true);
        fileHandler1.setLevel(Level.FINEST);
        fileHandler1.setFormatter(new SimpleFormatter());
        LOGGER_PERFORMANCE.addHandler(fileHandler1);
        LOGGER_PERFORMANCE.setLevel(Level.SEVERE);
    }

    public static void processTickets() throws IssueTrackerException, BlameInspectorException, VersionControlServiceException, ManagerException {
        try {
            manager = new Manager(propertyService, parseProjectSources, useDb);
            if (reportPrinters.size() > 1) {
                ((ReportHtml) reportPrinters.get(1)).setIts(manager.getIssueTrackerService());
            }
            if (endBound == -1) {
                endBound = manager.getNumberOfTickets();
            }
        } catch (Exception e) {
            printExceptionData(e);
        }
        manager.proccesTickets(startBound, endBound);
        manager.storeData();
    }

    public static void printResults(PrintWriter writer){
        for (IReportPrinter reportPrinter : reportPrinters) {
            if (writer != null && reportPrinter instanceof ReportHtml){
                 ((ReportHtml) reportPrinter).setProjectName(projectName);
                 ((ReportHtml) reportPrinter).setWriter(writer);
            }
            reportPrinter.printTickets(manager.getResults());
            reportPrinter.flush();
        }
    }

    public static boolean setAssignee() {
        if (!isInteractive) {
            if (isSettingAssignee) return true;
        } else {
            Scanner in = new Scanner(System.in);
            System.out.println("Set assignee on that ticket?(y/n)");
            if (in.next().equals("y")) {
                return true;
            }
        }
        return false;
    }

    public static void processConfigFile() throws PropertyServiceException {
        propertyService = new PropertyService(projectName, "config.properties");
    }

    public static void processComandLine(final String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        Option projectNameOption = new Option(PROJECT_IDENT, "project", true, "project name");
        projectNameOption.setArgs(1);
        projectNameOption.setArgName("project");
        projectNameOption.setRequired(true);
        Option debugOption = new Option(DEBUG_IDENT, false, "debug mode");
        Option deepOption = new Option(PARSE_PROJECT_IDENT, false, "parse project source files");
        Option generateReport = new Option(GENERATE_HTML_IDENT, false, "generate html report");
        generateReport.setArgs(0);
        debugOption.setArgs(0);
        OptionGroup ticketNumbersGroup = new OptionGroup();
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
        Option isUsingDbOption = new Option(STORED_RES_IDENT, "stored", false, "is using stored data");
        isUsingDbOption.setArgs(0);
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
        options.addOption(deepOption);
        options.addOption(generateReport);
        options.addOption(helpOption);
        options.addOption(isUsingDbOption);


        CommandLineParser cmdLineParser = new PosixParser();
        CommandLine cmdLine = null;

        try {
            cmdLine = cmdLineParser.parse(helpOptions, args);
            if (cmdLine.hasOption("-help")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                Comparator<Option> comparator = new Comparator<Option>() {
                    @Override
                    public int compare(final Option o1, final Option o2) {
                        String optsOrder = "ptrafisgXdh";
                        return optsOrder.indexOf(o1.getOpt()) - optsOrder.indexOf(o2.getOpt());
                    }
                };
                helpFormatter.setOptionComparator(comparator);
                helpFormatter.printHelp("BlameInspector", header, options, footer, true);
                System.exit(0);
            }
        } catch (ParseException e) {
        }


        try {
            cmdLine = cmdLineParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println("Type -help for help");
            System.exit(0);
        }
        if (args.length == 0) {
            System.out.println("No arguments provided, type -help for help");
            System.exit(0);
        }
        isDebug = false;
        if (cmdLine.hasOption("-X")) {
            isDebug = true;
        }
        parseProjectSources = false;
        if (cmdLine.hasOption(PARSE_PROJECT_IDENT)) {
            parseProjectSources = true;
        }
        projectName = cmdLine.getOptionValue(PROJECT_IDENT);
        if (cmdLine.hasOption(RANGE_IDENT)) {
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
        if (cmdLine.hasOption(FIX_IDENT)) {
            isSettingAssignee = true;
        } else if (cmdLine.hasOption(INTER_IDENT)) {
            isInteractive = true;
        }
        reportPrinters = new ArrayList<>();
        reportPrinters.add(new ReportConsole());
        LOGGER.fine("in setting data for report");
        if (cmdLine.hasOption(GENERATE_HTML_IDENT)) {
            ReportHtml reportHtml = new ReportHtml(projectName, date);
            reportPrinters.add(reportHtml);
        }
        useDb = false;
        if (cmdLine.hasOption(STORED_RES_IDENT)) {
            useDb = true;
        }
    }


    public static void printExceptionData(final Exception e) {
        System.out.println("Exception occured :");
        System.out.println(e.getMessage());
        if (isDebug) {
            e.printStackTrace();
        }
    }

    public static Manager getManager(){
        return manager;
    }

    public static void setDate(final String date){
        LOGGER.fine("in set Date main!");
        Main.date = date;
        System.out.println(Main.date.toString());
    }


}

class TimerTaskImpl extends TimerTask{

    private static final long TIME_DURATION= 10_000;
    private static final long SLEEP_TIME = 10_000;

    @Override
    public void run() {
        try {
            long startTimeMillis = System.currentTimeMillis();
            long endTimeMillis = System.currentTimeMillis();
            while(endTimeMillis - startTimeMillis < TIME_DURATION) {
                endTimeMillis = System.currentTimeMillis();
                Thread.sleep(SLEEP_TIME);
                System.out.println("time has come again!");
                Manager manager = Main.getManager();
                if (!manager.isDbUpToDate()) {
                    System.out.println("Something has changed!");
                    manager.proccesTickets();
                }
            }
            Main.latch.countDown();
            System.out.println("Out of the loop!");
        } catch (Exception e) {
            Main.printExceptionData(e);
        }

    }
}