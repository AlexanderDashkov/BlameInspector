package BlameInspector;

import java.io.IOException;

public class Main {

    public static void main(String [] args) {
        String projectName = null;
        int ticketNumber = 0;
        PropertyService propertyService = null;

        try {
            projectName = args[0];
            ticketNumber = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Not enough arguments. Try again.");
            System.exit(0);
        }

        try {
            propertyService = new PropertyService(projectName);
        } catch (IOException e){
            System.out.println(e.getStackTrace());
            System.out.println("Something wrong with reading config file!");
            System.exit(0);
        } catch (ProjectNotFoundException e){
            System.out.println(e.getStackTrace());
            System.out.println("Project with such name wasn't found on corresponding file.");
            System.exit(0);
        }
        BlameInspector blameInspector = new BlameInspector();
        try {
            blameInspector.init(propertyService);
            blameInspector.handleTicket(ticketNumber);
        } catch (VersionControlServiceException e) {
            printExceptionData(e,"Got exception in version control part.");
        } catch (IssueTrackerException e) {
            printExceptionData(e, "Got exception in issue tracker part.");
        } catch (TicketCorruptedException e) {
            System.out.println("Ticket is corrupted!");
        }
    }

    public static void printExceptionData(final BlameInspectorException e,
                                         final String message){
        System.out.println(message);
        System.out.println(e.getNestedException().getMessage());
        e.getNestedException().printStackTrace();
    }
}
