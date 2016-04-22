package blameinspector.vcs;

public class BlamedUserInfo {

    private String userName;
    private String userEmail;
    private String userCommitId;

    public BlamedUserInfo(final String userName, final String userEmail, final String userCommitId) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userCommitId = userCommitId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserCommitId() {
        return userCommitId;
    }
}
