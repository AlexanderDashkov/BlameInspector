package blameinspector;


import blameinspector.issuetracker.IssueTrackerException;
import blameinspector.vcs.VersionControlServiceException;
import com.jmolly.stacktraceparser.NStackTrace;
import com.jmolly.stacktraceparser.StackTraceParser;
import junit.framework.TestCase;
import org.antlr.runtime.RecognitionException;

import java.io.IOException;


public class ParserTest extends TestCase {

    public ParserTest(String testName) throws IOException {
        super(testName);
    }

    public void testRegExp() throws VersionControlServiceException, IssueTrackerException {
        BlameInspector blamer = new BlameInspector(null, null, false);
        assertTrue(blamer.isStartingStackTrace("SampleClassOne.invokeJaneException(SampleClassOne.java:14)"));
        assertTrue(blamer.isStartingStackTrace("org.jetbrains.jet.codegen.CompilationErrorHandler$1.reportException(CompilationErrorHandler.java:11)"));
        assertTrue(blamer.isStartingStackTrace("org.jetbrains.jet.codegen.CompilationErrorHandler$1.reportException(CompilationErrorHandler.kt:11)"));
        assertTrue(!blamer.isStartingStackTrace("time is 1:20"));
        assertTrue(!blamer.isStartingStackTrace("postedAtTime 1:20"));
    }

    public void testOptimiserStackTrace() {
        try {
            NStackTrace stackTrace = StackTraceParser.parse(Storage.test2);
            assertEquals(stackTrace.getTrace().getFrames().get(0).getLocation(), "(SourceFile:110)");
        } catch (RecognitionException e) {
            e.printStackTrace();
        }
    }



    public void testParseNoException() {
        String text = "I've got no exception just error!";
        testParse(text, null, "No StackTrace found in current ticket!");
    }


    public void testParseBrokenException() {
        testParse(Storage.test1, null, "StackTrace is corrupted!");
    }

    private void testParse(final String text, final String fileName, final String errorLine) {
//        try {
//            String file = new BlameInspector(null, null).parseIssueBody(text, 1).getFileName();
//            assertEquals(file, fileName);
//        }catch (TicketCorruptedException e){
//            assertEquals(e.getMessage(), errorLine);
//        }
    }
}
