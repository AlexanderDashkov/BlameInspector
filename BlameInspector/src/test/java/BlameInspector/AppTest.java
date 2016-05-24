package blameinspector;

import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.vcs.VersionControlServiceException;
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
import java.io.*;
import java.text.MessageFormat;

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

    private int counter;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) throws IOException {
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

//    public void testOptim() {
//        ByteArrayOutputStream myOut = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(myOut));
//        Main.main(new String[]{"-p", "BlameWhoTest", "-t", "8", "-d"});
//        assertEquals(myOut.toString().trim(), "Ticket # 8. Assigned to JaneSmithSenior");
//        System.setOut(sysOut);
//    }

    public void testPerformanceSmall() throws IssueTrackerException, PropertyServiceException, BlameInspectorException, VersionControlServiceException, FileNotFoundException, UnsupportedEncodingException, ManagerException {
        PropertyService propertyService = new PropertyService("BlameWhoTest", "config.properties");
        Manager manager = new Manager(propertyService, false, false);
        performanceTester("BlameWhoTest", manager, 1, 8);
    }

    public void testPerformanceMedium() throws IssueTrackerException, PropertyServiceException, BlameInspectorException, VersionControlServiceException, FileNotFoundException, UnsupportedEncodingException, ManagerException {
        PropertyService propertyService = new PropertyService("Kotlin", "config.properties");
        Manager manager = new Manager(propertyService, false, false);
        performanceTester("Kotlin", manager, 1249, 1290);
    }

    protected void performanceTester(String projectName, Manager manager, int start,int end) throws PropertyServiceException, VersionControlServiceException, IssueTrackerException, BlameInspectorException, FileNotFoundException, UnsupportedEncodingException, ManagerException {
        manager.setNThread(1);
        long startTime1 = System.currentTimeMillis();
        manager.proccesTickets(start, end);
        long endTime1 = System.currentTimeMillis();
        manager.setNThread(10);
        long startTime2 = System.currentTimeMillis();
        manager.proccesTickets(start, end);
        long endTime2 = System.currentTimeMillis();
        PrintWriter writer = new PrintWriter(projectName + "PerformanceResults.txt", "UTF-8");
        writer.println(projectName + " from " + start + " to "+  end +  " :");
        writer.println(MessageFormat.format("Single: {0} \n Multi: {1}",
                String.valueOf(endTime1 - startTime1),
                String.valueOf(endTime2 - startTime2)));
        writer.close();
    }

    protected void ticketCheckerOutterProjects(String ticketNumber, String projectName, String result) throws IOException {
        ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        Main.main(new String[]{"-p", projectName, "-t", ticketNumber});
        assertEquals(myOut.toString().trim(), result);
        System.setOut(sysOut);
    }


    protected void expertTest() throws PropertyServiceException, VersionControlServiceException, IssueTrackerException, BlameInspectorException, TicketCorruptedException, IOException {
        PropertyService propertyService = new PropertyService("Kotlin", "config.properties");
        Manager manager = new Manager(propertyService, true, false);
        int endBound  = 8;
        int allAmount = 0;
        int correctedAssigned = 0;
        for (int i = 1; i < endBound; i++){
            manager.handleTicket(i);
        }
        int i = 1;
        for (TicketInfo ticketInfo : manager.getResults()){
            if (manager.isAssigned(i)){
                if (ticketInfo != null && ticketInfo.getAssignee()!=null && ticketInfo.getAssignee().get(0) != null){
                    allAmount++;
                    String surname;
                    try {
                        surname = ticketInfo.getAssignee().get(0).split(".")[0].toLowerCase();
                    }catch (ArrayIndexOutOfBoundsException e){
                        surname = ticketInfo.getAssignee().get(0).toLowerCase();
                    }
                    if (manager.properAssignee(i).toLowerCase().contains(surname)){
                        correctedAssigned++;
                    }
                }
            }
            i++;
        }
        double percent = correctedAssigned / allAmount;
        System.out.println(percent);
        assertTrue(percent > 0.05);
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
