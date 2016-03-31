package BlameInspector;

public class BlameInspectorException extends Exception{


    public BlameInspectorException(Exception e) {
        super(e);
    }

    public BlameInspectorException(Exception e, String s) {
        super(s, e);
    }
}
