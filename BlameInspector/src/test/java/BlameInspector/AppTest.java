package BlameInspector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.antlr.runtime.RecognitionException;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import org.tmatesoft.svn.core.SVNException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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

    private void testParse(final String text, final String fileName, final String errorLine){
        try {
            String file = new BlameInspector().parseIssueBody(text).getFileName();
            assertEquals(file, fileName);
        }catch (TicketCorruptedException e){
            assertEquals(e.getMessage(), errorLine);
        }
    }


    public void testParseNoException() {
        String text = "I've got no exception just error!";
        testParse(text, null, "No StackTrace found in current ticket!");
    }


    public void testParseBrokenException(){
        testParse(Storage.test1, null, "StackTrace is corrupted!");
    }

    public void testSimpleTicket() throws IOException, GitAPIException, JSONException, ProjectNotFoundException, SVNException, SAXException, ParserConfigurationException {
        ticketChecker("1","JaneSmithSenior");
    }

    public void testCorruptedTicket(){
        System.setOut(new PrintStream(outContent));
        Main.main(new String[] {"-p",this.projectName,"-t", "2"});
        String response = outContent.toString();
        System.setOut(sysOut);
//        assertEquals(response, "Ticket number: 2 Assignee: Ticket is corrupted!");
    }

    public void testPackageTicket() throws JSONException, GitAPIException, IOException, ProjectNotFoundException, SVNException, SAXException, ParserConfigurationException {
        ticketChecker("3", "JackSmithJunior");
    }
    public void testThirdLibraryException() throws JSONException, GitAPIException, IOException, ProjectNotFoundException, SVNException, SAXException, ParserConfigurationException {
        ticketChecker("4", "JaneSmithSenior");
    }

    public void testComplexTicket() throws ProjectNotFoundException, ParserConfigurationException, SVNException, IOException, JSONException, GitAPIException, SAXException {
        ticketChecker("5", "JackSmithJunior");
    }

    public void testSimpleRealTicket() throws ProjectNotFoundException, ParserConfigurationException, SVNException, IOException, JSONException, GitAPIException, SAXException {
        ticketCheckerOutterProjects("2034", "Guava", "Ticket # 2034. Assigned to kak@google.com");
    }

    public void testComplexRealTicket() throws RecognitionException, IOException {
         ticketCheckerOutterProjects("1757", "Guava", "Ticket # 1757. Assigned to cpovirk@google.com");
    }

    public void testNoEntryTicket() throws IOException {
        ticketCheckerOutterProjects("1841", "Guava", "Ticket # 1841 was not assigned due to: No entry of exception found in current repository.");
        ticketCheckerOutterProjects("2234", "Guava", "Ticket # 2234 was not assigned due to: No entry of exception found in current repository.");
    }

    protected void ticketCheckerOutterProjects(String ticketNumber, String projectName, String result) throws IOException {
        ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        Main.main(new String[]{"-p", projectName, "-t", ticketNumber});
        assertEquals(myOut.toString().trim(), result);
        System.setOut(sysOut);
    }

    protected void ticketChecker(String ticketNumber, String blameLogin) throws IOException, GitAPIException, JSONException, ProjectNotFoundException, SVNException, SAXException, ParserConfigurationException {
        Main.main(new String[]{"-p", this.projectName,"-t" ,ticketNumber, "-f"});


        PropertyService propertyService = new PropertyService(projectName);

        GitHubClient client = new GitHubClient();
        client.setCredentials(propertyService.getUserName(),
                propertyService.getPassword());

        IssueService service = new IssueService(client);
        Issue issue = service.getIssue(repoOwner,
                this.projectName, Integer.parseInt(ticketNumber));
        assertTrue(issue.getAssignee().getLogin().equals(blameLogin));
        issue.setAssignee(new User().setLogin(""));
        service.editIssue(repoOwner, this.projectName, issue);
    }
}
