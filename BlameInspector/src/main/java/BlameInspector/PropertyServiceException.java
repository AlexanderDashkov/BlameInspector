package blameinspector;


public class PropertyServiceException extends Exception {
    public PropertyServiceException(final Exception e) {
        super(e);
    }

    public PropertyServiceException(final String s) {
        super(s);
    }
}
