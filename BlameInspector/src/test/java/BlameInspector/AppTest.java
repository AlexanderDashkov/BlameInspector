package BlameInspector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase
{
    private String projectName;
    private String repoOwner;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName ) throws IOException {
        super( testName );
        this.projectName = "BlameWhoTest";
        this.repoOwner = "JackSmithJunior";
        System.setOut(new PrintStream(outContent));
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testSimpleTicket() throws IOException, GitAPIException, JSONException, ProjectNotFoundException {
        ticketChecker("1","JaneSmithSenior");
    }

    public void testCorruptedTicket(){
//        Main.main(new String[] {repoName, "2", password});
//        assertTrue(outContent.toString().equals("Sorry, current ticket is corrupted!"));
    }

    public void testPackageTicket() throws JSONException, GitAPIException, IOException, ProjectNotFoundException {
        ticketChecker("3", "JackSmithJunior");
    }
    public void testThirdLibraryException() throws JSONException, GitAPIException, IOException, ProjectNotFoundException {
        ticketChecker("4", "JaneSmithSenior");
    }

    protected void ticketChecker(String ticketNumber, String blameLogin) throws IOException, GitAPIException, JSONException, ProjectNotFoundException {
        try {
            Main.main(new String[]{this.projectName, ticketNumber});
        }catch (TicketCorruptedException e){
            System.out.println(e.getStackTrace());
        }

        PropertyService propertyService = new PropertyService(projectName);

        GitHubClient client = new GitHubClient();
        client.setCredentials(propertyService.getUserName(),
                propertyService.getPassword());

        IssueService service = new IssueService(client);
        Issue issue = service.getIssue(repoOwner,
                this.projectName, Integer.parseInt(ticketNumber));
        assertTrue(issue.getAssignee().getLogin().equals(blameLogin));
    }
}
