package BlameInspector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import org.tmatesoft.svn.core.SVNException;
import org.xml.sax.SAXException;

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
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream sysOut;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName ) throws IOException {
        super( testName );
        this.projectName = "BlameWhoTest";
        this.repoOwner = "JackSmithJunior";
        sysOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testSimpleTicket() throws IOException, GitAPIException, JSONException, ProjectNotFoundException, SVNException, SAXException {
        ticketChecker("1","JaneSmithSenior");
    }

    public void testCorruptedTicket(){
        Main.main(new String[] {"-p",this.projectName,"-t", "2"});
        String response[] = outContent.toString().split("!");
        String singleResponse = response[0];
        System.setOut(sysOut);
        System.out.println(singleResponse);
//        assertTrue(singleResponse.equals("Ticket is corrupted"));
    }

    public void testPackageTicket() throws JSONException, GitAPIException, IOException, ProjectNotFoundException, SVNException, SAXException {
        ticketChecker("3", "JackSmithJunior");
    }
    public void testThirdLibraryException() throws JSONException, GitAPIException, IOException, ProjectNotFoundException, SVNException, SAXException {
        ticketChecker("4", "JaneSmithSenior");
    }

    protected void ticketChecker(String ticketNumber, String blameLogin) throws IOException, GitAPIException, JSONException, ProjectNotFoundException, SVNException, SAXException {
        Main.main(new String[]{"-p", this.projectName,"-t" ,ticketNumber});


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
