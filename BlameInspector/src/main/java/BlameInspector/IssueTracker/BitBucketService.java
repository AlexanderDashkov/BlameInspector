package blameinspector.issuetracker;

import blameinspector.vcs.BlamedUserInfo;
import blameinspector.vcs.VersionControlServiceException;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;


public class BitBucketService extends IssueTrackerService {

    private int issueNumber;
    private String basicAuth;
    private static final String BITBUCKET_ISSUE = "https://api.bitbucket.org/1.0/repositories/{0}/{1}/issues/{2}";
    private static final String BITBUCKET_USER = "https://api.bitbucket.org/1.0/users/";


    public BitBucketService(final String userName, final String password, final String repositoryOwner,
                            final String repositoryName) {
        super(userName, password, repositoryOwner, repositoryName);
        ISSUE_URL = "https://bitbucket.org/{0}/{1}/issues/";
        ISSUE_URL = MessageFormat.format(ISSUE_URL, repositoryOwner, repositoryName.toLowerCase());
        ASSIGNEE_URL = "https://bitbucket.org/{0}";
        String userCredentials = this.userName + ":" + this.password;
        this.basicAuth = "Basic " + new String(Base64.encodeBase64(userCredentials.getBytes()));
    }

    @Override
    public boolean isUpToDate() {
        return true;
    }

    @Override
    public String assignee(int number) {
        return null;
    }

    @Override
    public String getIssueBody(final int issueNumber) throws JSONException, IOException {
        this.issueNumber = issueNumber;
        String url = MessageFormat.format(BITBUCKET_ISSUE,
                this.repositoryOwner,
                this.repositoryName.toLowerCase(),
                this.issueNumber);

        String result = getRequest(url, basicAuth);
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject.getString("content");
    }

    @Override
    public void setIssueAssignee(final String Login) throws IOException, JSONException {
        String url = MessageFormat.format(BITBUCKET_ISSUE,
                this.repositoryOwner,
                this.repositoryName.toLowerCase(),
                this.issueNumber);
        String blameLogin = "\"" + Login + "\"";
        String data = "{\"responsible\":" + blameLogin + " }";
        putRequest(url, data, basicAuth);
    }


    @Override
    public ArrayList<String> assigneeUrl(final ArrayList<String> userNames) {
        return null;
    }

    @Override
    public String ticketUrl(final int issueNumber) {
        return null;
    }


    public String getUserLogin(final BlamedUserInfo blamedUserInfo) throws IOException, JSONException,
            VersionControlServiceException, IssueTrackerException {
        String blameEmail = null;
        if (blamedUserInfo.getUserEmail() != null) {
            blameEmail = blamedUserInfo.getUserEmail();
        } else {
            throw new IssueTrackerException("not enough info from vcs!");
        }
        String url = BITBUCKET_USER + blameEmail;
        String result = getRequest(url, null);
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject.getJSONObject("user").getString("username");
    }

    @Override
    public void refresh() {
        issueNumber = -1;
    }
}
