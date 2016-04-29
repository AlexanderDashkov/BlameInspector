package blameinspector;

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
        extends TestCase {
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
    public AppTest(String testName) throws IOException {
        super(testName);
        this.projectName = "BlameWhoTest";
        this.repoOwner = "JackSmithJunior";
        sysOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }


    public void testSimpleTicket() throws IOException, GitAPIException, JSONException, SVNException, SAXException, ParserConfigurationException, PropertyServiceException {
        ticketChecker("1", "JaneSmithSenior");
    }

    public void testCorruptedTicket() {
        System.setOut(new PrintStream(outContent));
        Main.main(new String[]{"-p", this.projectName, "-t", "2"});
        String response = outContent.toString();
        System.setOut(sysOut);
//        assertEquals(response, "Ticket number: 2 Assignee: Ticket is corrupted!");
    }

    public void testPackageTicket() throws JSONException, GitAPIException, IOException, SVNException, SAXException, ParserConfigurationException, PropertyServiceException {
        ticketChecker("3", "JackSmithJunior");
    }

    public void testThirdLibraryException() throws JSONException, GitAPIException, IOException, SVNException, SAXException, ParserConfigurationException, PropertyServiceException {
        ticketChecker("4", "JaneSmithSenior");
    }

    public void testComplexTicket() throws ParserConfigurationException, SVNException, IOException, JSONException, GitAPIException, SAXException, PropertyServiceException {
        ticketChecker("5", "JackSmithJunior");
    }

    public void testKotlinRepo() throws IOException {
        ticketCheckerOutterProjects("1000", "Kotlin", "Ticket # 1000 was not assigned due to: No StackTrace found in current ticket!");
    }

    public void testSimpleRealTicket() throws ParserConfigurationException, SVNException, IOException, JSONException, GitAPIException, SAXException {
        ticketCheckerOutterProjects("2034", "Guava", "Ticket # 2034. Assigned to kluever");
    }

    public void testComplexRealTicket() throws RecognitionException, IOException {
        ticketCheckerOutterProjects("1757", "Guava", "Ticket # 1757. Assigned to cpovirk");
    }

    public void testNoEntryTicket() throws IOException {
        ticketCheckerOutterProjects("1841", "Guava", "Ticket # 1841 was not assigned due to: No entry of exception found in current repository.");
        ticketCheckerOutterProjects("2234", "Guava", "Ticket # 2234 was not assigned due to: No entry of exception found in current repository.");
    }

    public void testNoEntryTicketCauseOfOptim() throws IOException {
        //ticketCheckerOutterProjects("1806", "Guava", "Ticket # 1806. Assigned to kevinb@google.com");
    }

    public void testOptim() {
        ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        Main.main(new String[]{"-p", "BlameWhoTest", "-t", "8", "-d"});
        assertEquals(myOut.toString().trim(), "Ticket # 8. Assigned to JackSmithJunior");
        System.setOut(sysOut);
    }

    protected void ticketCheckerOutterProjects(String ticketNumber, String projectName, String result) throws IOException {
        ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        Main.main(new String[]{"-p", projectName, "-t", ticketNumber});
        assertEquals(myOut.toString().trim(), result);
        System.setOut(sysOut);
    }


    protected void ticketChecker(String ticketNumber, String blameLogin) throws IOException, GitAPIException, JSONException, SVNException, SAXException, ParserConfigurationException, PropertyServiceException {
        Main.main(new String[]{"-p", this.projectName, "-t", ticketNumber, "-f"});


        PropertyService propertyService = new PropertyService(projectName, "config.properties");

        GitHubClient client = new GitHubClient();
        client.setCredentials(propertyService.getUserName(),
                propertyService.getPassword());

        IssueService service = new IssueService(client);
        Issue issue = service.getIssue(repoOwner,
                this.projectName, Integer.parseInt(ticketNumber));
        assertEquals(issue.getAssignee().getLogin(), blameLogin);
        issue.setAssignee(new User().setLogin(""));
        service.editIssue(repoOwner, this.projectName, issue);
    }
}
