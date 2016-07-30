package blameinspector.autotest;

import blameinspector.AppTest;
import blameinspector.Main;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by александр on 31.07.2016.
 */
public class GitGitHubYouTrackTest extends TestCase {

    private String projectName;
    private String repoOwner;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream sysOut;

    private int counter;

    public GitGitHubYouTrackTest(String testName) throws IOException {
        super(testName);
        counter = 0;
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

    public void testKotlinRepo() throws IOException {
        ticketCheckerOutterProjects("1000", "Kotlin", "Ticket # 1000 was not assigned due to: No StackTrace found in current ticket!");
    }

//    public void testSimpleRealTicket() throws ParserConfigurationException, SVNException, IOException, JSONException, GitAPIException, SAXException {
//        ticketCheckerOutterProjects("2034", "Guava", "Ticket # 2034. Assigned to kluever");
//    }

//    public void testComplexRealTicket() throws RecognitionException, IOException {
//        ticketCheckerOutterProjects("1757", "Guava", "Ticket # 1757. Assigned to cpovirk");
//    }

    public void testNoEntryTicket() throws IOException {
        ticketCheckerOutterProjects("1841", "Guava", "Ticket # 1841 was not assigned due to: No entry of exception found in current repository.");
        ticketCheckerOutterProjects("2234", "Guava", "Ticket # 2234 was not assigned due to: No entry of exception found in current repository.");
    }

    public void testNoEntryTicketCauseOfOptim() throws IOException {
        //ticketCheckerOutterProjects("1806", "Guava", "Ticket # 1806. Assigned to kevinb@google.com");
    }

    protected void ticketCheckerOutterProjects(String ticketNumber, String projectName, String result) throws IOException {
        ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        Main.main(new String[]{"-p", projectName, "-t", ticketNumber});
        assertEquals(myOut.toString().trim(), result);
        System.setOut(sysOut);
    }


}
