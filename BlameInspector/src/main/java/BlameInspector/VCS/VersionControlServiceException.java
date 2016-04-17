package blameinspector.vcs;


public class VersionControlServiceException extends Exception {


    public VersionControlServiceException(final Exception e) {
        super(e);
    }

    public VersionControlServiceException(final Exception e, final String s) {
        super(s, e);
    }

    public VersionControlServiceException(final String s) {
        super(s);
    }
}
