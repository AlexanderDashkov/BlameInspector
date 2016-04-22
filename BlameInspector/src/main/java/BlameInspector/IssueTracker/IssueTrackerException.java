package blameinspector.issuetracker;

public class IssueTrackerException extends Exception {

    private boolean isCannotGetBlame;

    public IssueTrackerException(boolean b, final String s) {
        super(s);
        isCannotGetBlame = b;
    }

    public IssueTrackerException(final Exception e) {
        super(e);
    }

    public IssueTrackerException(final Exception e, final String s) {
        super(s, e);
    }

    public IssueTrackerException(final String s) {
        super(s);
    }

    public boolean isCannotGetBlame() {
        return isCannotGetBlame;
    }
}
