package blameinspector.autotest;

import blameinspector.Main;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by александр on 31.07.2016.
 */
public class GitGitHubYouTrackTest extends Assert {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream sysOut;

    public GitGitHubYouTrackTest() throws IOException {
        sysOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    public void testKotlinRepo() throws IOException {
        ticketCheckerOutterProjects("1000", "Kotlin", "Ticket # 1000 was not assigned due to: No StackTrace found in current ticket!");
    }

    protected void ticketCheckerOutterProjects(String ticketNumber, String projectName, String result) throws IOException {
        ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        Main.main(new String[]{"-p", projectName, "-t", ticketNumber});
        assertEquals(myOut.toString().trim(), result);
        System.setOut(sysOut);
    }

}
