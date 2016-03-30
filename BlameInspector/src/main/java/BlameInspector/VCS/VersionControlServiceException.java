package BlameInspector.VCS;


public class VersionControlServiceException extends Exception {


    public VersionControlServiceException(Exception e) {
        super(e);
    }

    public VersionControlServiceException(Exception e,final String s) {
        super(s, e);
    }
}
