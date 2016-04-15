package BlameInspector;

public class BlameInspectorException extends Exception{


    public BlameInspectorException(final Exception e) {
        super(e);
    }

    public BlameInspectorException(final Exception e, final String s) {
        super(s, e);
    }
}
