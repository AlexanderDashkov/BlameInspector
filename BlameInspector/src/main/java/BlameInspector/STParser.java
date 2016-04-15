package BlameInspector;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

public class STParser {

   private LANGUAGE language;

   public STParser(String language){
       for (LANGUAGE lang : LANGUAGE.values()){
           if (language.equals(lang.toString().toLowerCase())){
               this.language = lang;
               return;
           }
       }
   }

   public TraceInfo parse(final String text){
       PrintStream sysOut = System.out;
       PrintStream sysErr = System.err;
       System.setOut(new PrintStream(new ByteArrayOutputStream()));
       System.setErr(new PrintStream(new ByteArrayOutputStream()));
       try {
           if (language.equals(LANGUAGE.JAVA)) {
               //stackTrace = StackTraceParser.parse(text);
           } else {

           }
       }catch (NoSuchElementException e){

       }finally {
           System.setOut(sysOut);
           System.setErr(sysErr);
       }
       return null;
   }


    private enum LANGUAGE {
        JAVA,
        CSHARP
    }

}
