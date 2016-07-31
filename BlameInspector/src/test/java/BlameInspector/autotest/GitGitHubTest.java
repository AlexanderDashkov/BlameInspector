package blameinspector.autotest;

import blameinspector.Main;
import blameinspector.PropertyService;
import blameinspector.PropertyServiceException;
import org.antlr.runtime.RecognitionException;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by александр on 28.07.2016.
 */
public class GitGitHubTest extends Assert {

    private String projectName;
    private String repoOwner;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream sysOut;

    private static final String JACK = "JackSmithJunior";
    private static final String JANE = "JaneSmithSenior";

    private int counter;

    public GitGitHubTest() throws IOException {
        counter = 0;
        this.projectName = "BlameWhoTest";
        this.repoOwner = "JackSmithJunior";
        sysOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    public void testCorrectBlameWhoTestTickets() throws IOException, GitAPIException, JSONException, SVNException,
            SAXException, ParserConfigurationException, PropertyServiceException {
        ticketChecker("1", JANE);
        ticketChecker("3", JACK);
        ticketChecker("4", JANE);
        ticketChecker("5", JACK);
    }

    @Ignore
    @Test
    public void testSimpleRealTicket() throws ParserConfigurationException, SVNException, IOException,
            JSONException, GitAPIException, SAXException {
        ticketCheckerOutterProjects("2034", "Guava", "Ticket # 2034. Assigned to kluever");
    }

    @Ignore
    @Test
    public void testComplexRealTicket() throws RecognitionException, IOException {
        ticketCheckerOutterProjects("1757", "Guava", "Ticket # 1757. Assigned to cpovirk");
    }

    @Test
    public void testNoEntryTicket() throws IOException {
        ticketCheckerOutterProjects("1841", "Guava", "Ticket # 1841 was not assigned due to: No entry of exception found in current repository.");
        ticketCheckerOutterProjects("2234", "Guava", "Ticket # 2234 was not assigned due to: No entry of exception found in current repository.");
    }

    @Ignore
    @Test
    public void testNoEntryTicketCauseOfOptim() throws IOException {
        ticketCheckerOutterProjects("1806", "Guava", "Ticket # 1806. Assigned to kevinb@google.com");
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
        try {
            assertEquals(issue.getAssignee().getLogin(), blameLogin);
            issue.setAssignee(new User().setLogin(""));
            service.editIssue(repoOwner, this.projectName, issue);
        }catch (NullPointerException e){
            counter++;
            if (counter == 5){
                e.printStackTrace();
            }
        }
    }

}
