package blameinspector.autotest;

import blameinspector.AppTest;
import blameinspector.Main;
import blameinspector.PropertyService;
import blameinspector.PropertyServiceException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
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
 * Created by александр on 28.07.2016.
 */
public class GitGitHubTest extends TestCase{

    private String projectName;
    private String repoOwner;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream sysOut;

    private int counter;


    public GitGitHubTest(String testName) throws IOException {
        super(testName);
        counter = 0;
        this.projectName = "BlameWhoTest";
        this.repoOwner = "JackSmithJunior";
        sysOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testSimpleTicket() throws IOException, GitAPIException, JSONException, SVNException, SAXException, ParserConfigurationException, PropertyServiceException {
        ticketChecker("1", "JaneSmithSenior");
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
            System.out.println("Something went wrong with github.");
            counter++;
            if (counter==5){
                e.printStackTrace();
            }
        }
    }


}
