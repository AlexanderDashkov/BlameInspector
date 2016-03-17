package BlameInspector;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

import java.io.IOException;

public class Main {

    public static void main(String [] args) throws IOException, GitAPIException, JSONException, TicketCorruptedException {
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
        blameInspector.init(propertyService);
        blameInspector.handleTicket(ticketNumber);
    }
}
