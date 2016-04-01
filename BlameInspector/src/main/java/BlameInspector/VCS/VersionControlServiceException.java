package BlameInspector.VCS;


public class VersionControlServiceException extends Exception {


    public VersionControlServiceException(final Exception e) {
        super(e);
    }

    public VersionControlServiceException(final Exception e, final String s) {
        super(s, e);
    }
}
