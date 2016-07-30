package blameinspector;

import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.vcs.VersionControlServiceException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.text.MessageFormat;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

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

    public void testCorruptedTicket() {
        System.setOut(new PrintStream(outContent));
        Main.main(new String[]{"-p", this.projectName, "-t", "2"});
        String response = outContent.toString();
        System.setOut(sysOut);
//        assertEquals(response, "Ticket number: 2 Assignee: Ticket is corrupted!");
    }


//    public void testOptim() {
//        ByteArrayOutputStream myOut = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(myOut));
//        Main.main(new String[]{"-p", "BlameWhoTest", "-t", "8", "-d"});
//        assertEquals(myOut.toString().trim(), "Ticket # 8. Assigned to JaneSmithSenior");
//        System.setOut(sysOut);
//    }

    protected void PerformanceSmall() throws IssueTrackerException, PropertyServiceException, BlameInspectorException, VersionControlServiceException, IOException, ManagerException {
        PropertyService propertyService = new PropertyService("BlameWhoTest", "config.properties");
        Manager manager = new Manager(propertyService, false, false);
        performanceTester("BlameWhoTest", manager, 1, 8);
    }

    protected void PerformanceMedium() throws IssueTrackerException, PropertyServiceException, BlameInspectorException, VersionControlServiceException, IOException, ManagerException {
        PropertyService propertyService = new PropertyService("Kotlin", "config.properties");
        Manager manager = new Manager(propertyService, false, false);
        performanceTester("Kotlin", manager, 1249, 1290);
    }

    protected void performanceTester(String projectName, Manager manager, int start,int end) throws PropertyServiceException, VersionControlServiceException, IssueTrackerException, BlameInspectorException, FileNotFoundException, UnsupportedEncodingException, ManagerException {
        PrintWriter writer = new PrintWriter(new FileOutputStream(new File(projectName + "PerformanceResults.txt")),
                true);
        for(int i = 2; i <= 26; i++) {
            manager.setNThread(1);
            long startTime1 = System.currentTimeMillis();
            manager.proccesTickets(start, end);
            long endTime1 = System.currentTimeMillis();
            manager.setNThread(10);
            long startTime2 = System.currentTimeMillis();
            manager.proccesTickets(start, end);
            long endTime2 = System.currentTimeMillis();
            writer.println(projectName + " from " + start + " to " + end + " :");
            writer.println("Number of threads : " + i );
            writer.println(MessageFormat.format("Single: {0} \n Multi: {1}",
                    String.valueOf(endTime1 - startTime1),
                    String.valueOf(endTime2 - startTime2)));
        }
        writer.close();
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



}
