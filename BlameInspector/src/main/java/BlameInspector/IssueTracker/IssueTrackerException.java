package BlameInspector.IssueTracker;

public class IssueTrackerException extends Exception {

    public IssueTrackerException(final Exception e) {
        super(e);
    }

    public IssueTrackerException(final Exception e, final String s) {
        super(s, e);
    }
}
