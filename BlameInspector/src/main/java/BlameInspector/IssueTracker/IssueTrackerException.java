package BlameInspector.IssueTracker;

public class IssueTrackerException extends Exception {

    public IssueTrackerException(Exception e) {
        super(e);
    }

    public IssueTrackerException(Exception e,final String s) {
        super(s, e);
    }
}
