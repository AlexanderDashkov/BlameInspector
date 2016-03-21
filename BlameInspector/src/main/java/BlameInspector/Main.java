package BlameInspector;

import org.apache.commons.cli.*;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Main {

    public static void main(String [] args) {
        Option projectNameOption = new Option("p", "project", true, "Project Name");
        projectNameOption.setArgs(1);
        projectNameOption.setOptionalArg(false);
        projectNameOption.setArgName("project name in xml file");
        Option ticketNumberOption = new Option("t", "ticket", true, "Ticket Number");
        Option fixKeyOption = new Option("f", "fix", true, "is set assignee");

        Options options = new Options();
        options.addOption(projectNameOption);
        options.addOption(ticketNumberOption);
        options.addOption(fixKeyOption);

        CommandLineParser cmdLineParser = new PosixParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = cmdLineParser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String projectName = cmdLine.getOptionValue("p");
        int ticketNumber = Integer.parseInt(cmdLine.getOptionValue("t"));
        boolean isSettingAssignee = false;
        if (cmdLine.hasOption("f")){
            isSettingAssignee = true;
        }
        PropertyService propertyService = null;


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
        BlameInspector blameInspector = new BlameInspector();
        PrintStream sysOut = System.out;
        PrintStream outTempStream = new PrintStream(new ByteArrayOutputStream());
        System.setOut(outTempStream);
        System.setErr(outTempStream);
        try {
            blameInspector.init(propertyService);
            blameInspector.handleTicket(ticketNumber, isSettingAssignee);
        } catch (VersionControlServiceException e) {
            System.setOut(sysOut);
            printExceptionData(e,"Got exception in version control part.");
        } catch (IssueTrackerException e) {
            System.setOut(sysOut);
            printExceptionData(e, "Got exception in issue tracker part.");
        } catch (TicketCorruptedException e) {
            System.setOut(sysOut);
            System.out.println("Ticket is corrupted!");
        }catch(Exception e){
            System.setOut(sysOut);
            System.out.println("still exception");
            System.out.println(e);
        }

    }

    public static void printExceptionData(final BlameInspectorException e,
                                         final String message){
        System.out.println(message);
        System.out.println(e.getNestedException().getMessage());
        e.getNestedException().printStackTrace();
    }
}
