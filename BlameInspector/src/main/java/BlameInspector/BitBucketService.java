package BlameInspector;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;


public class BitBucketService extends IssueTrackerService {

    private int issueNumber;
    private String basicAuth;
    private static final String BITBUCKET_ISSUE = "https://api.bitbucket.org/1.0/repositories/{0}/{1}/issues/{2}";
    private static final String BITBUCKET_USER = "https://api.bitbucket.org/1.0/users/";

    public BitBucketService(final String userName, final String password, final String repositoryOwner,
                            final String repositoryName) {
        super(userName, password, repositoryOwner, repositoryName);
        String userCredentials = this.userName + ":" + this.password;
        this.basicAuth = "Basic " + new String(Base64.encodeBase64(userCredentials.getBytes()));
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
    public void setIssueAssignee(final String blameEmail) throws IOException, JSONException {
        String url = MessageFormat.format(BITBUCKET_ISSUE,
                this.repositoryOwner,
                this.repositoryName.toLowerCase(),
                this.issueNumber);
        String blameLogin = "\"" + getLogin(blameEmail) + "\"";
        String data = "{\"responsible\":" + blameLogin  + " }";
        putRequest(url, data, basicAuth);
    }

    private String getLogin(final String blameEmail) throws IOException, JSONException {
        String url = BITBUCKET_USER + blameEmail;
        String result = getRequest(url, null);
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject.getJSONObject("user").getString("username");
    }
}
