package blameinspector;


import com.jmolly.stacktraceparser.NStackTrace;
import com.jmolly.stacktraceparser.StackTraceParser;
import junit.framework.TestCase;
import org.antlr.runtime.RecognitionException;

import java.io.IOException;


public class ParserTest extends TestCase{

    public ParserTest( String testName ) throws IOException {
        super( testName );
    }

    public void testOptimiserStackTrace(){
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


    public void testParseBrokenException(){
        testParse(Storage.test1, null, "StackTrace is corrupted!");
    }

    private void testParse(final String text, final String fileName, final String errorLine){
//        try {
//            String file = new BlameInspector(null, null).parseIssueBody(text, 1).getFileName();
//            assertEquals(file, fileName);
//        }catch (TicketCorruptedException e){
//            assertEquals(e.getMessage(), errorLine);
//        }
    }
}
