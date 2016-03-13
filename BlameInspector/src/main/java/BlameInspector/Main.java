package BlameInspector;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static final String createConfigCommand = "create";

    public static void main(String [] args) throws IOException, GitAPIException, JSONException, TicketCorruptedException {
        String projectName;
        int ticketNumber = 0;
        PropertyService propertyService = new PropertyService();

        try {
            projectName = args[0];
            if (projectName.equals(createConfigCommand)){
                Scanner in = new Scanner(System.in);
                System.out.println("Enter project name:");
                propertyService.setProjectName(in.nextLine());
                System.out.println("Enter user name:");
                propertyService.setUserName(in.nextLine());
                System.out.println("Enter password:");
                propertyService.setPassword(in.nextLine());
                System.out.println("Enter issue tracker url:");
                propertyService.setIssueTracker(in.nextLine());
                System.out.println("Enter path to local repo on this PC:");
                propertyService.setPathToRepo(in.nextLine());
                propertyService.writeInFile();
                in.close();
                System.exit(0);
            }
            ticketNumber = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Not enough arguments. Try again.");
            System.exit(0);
        } catch (IOException e){
            System.out.println(e.getStackTrace());
            System.out.println("Something wrong with writing in file!");
            System.exit(0);
        }

        try {
            propertyService.readFromFile();
        } catch (IOException e){
            System.out.println(e.getStackTrace());
            System.out.println("Something wrong with reading config file!");
            System.exit(0);
        }
        BlameInspector blameInspector = new BlameInspector();
        blameInspector.init(propertyService);
        blameInspector.handleTicket(ticketNumber);
    }
}
