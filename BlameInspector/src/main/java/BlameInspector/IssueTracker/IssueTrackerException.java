package blameinspector.issuetracker;

public class IssueTrackerException extends Exception {

    public IssueTrackerException(final Exception e) {
        super(e);
    }

    public IssueTrackerException(final Exception e, final String s) {
        super(s, e);
    }

    public IssueTrackerException(final String s) {
       super(s);
    }
}
