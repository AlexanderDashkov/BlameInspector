package blameinspector;


public class ManagerException extends Exception {


    public ManagerException(final Exception e) {
        super(e);
    }

    public ManagerException(final Exception e, final String s) {
        super(s, e);
    }
}

