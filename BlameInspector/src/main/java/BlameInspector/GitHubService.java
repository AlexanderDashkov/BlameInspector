package BlameInspector;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class GitHubService extends IssueTrackerService {

    private Issue issue;
    private IssueService issueService;


    public GitHubService(final String userName,
                         final String password,
                         final String repositoryOwner,
                         final String repositoryName){
        super(userName, password, repositoryOwner, repositoryName);
        GitHubClient client = new GitHubClient();
        client.setCredentials(userName, password);
        this.issueService = new IssueService(client);
    }

    @Override
    public String getIssueBody(final int issueNumber) throws IOException {
        issue = issueService.getIssue(repositoryOwner, repositoryName, issueNumber);
        return issue.getBody();
    }

    @Override
    public void setIssueAssignee(final String blameEmail) throws IOException, JSONException {
        User blamedUser = new User();
        issue.setAssignee(blamedUser.setLogin(getUserLogin(blameEmail)));
        issueService.editIssue(repositoryOwner, repositoryName, issue);
    }

    private static String getUserLogin(final String blamedUserEmail) throws IOException, JSONException {

        String email = blamedUserEmail.split("@")[0];
        String url = "https://api.github.com/search/users?q=" + email + "+in:email";

        String result = getRequest(url, null);

        JSONObject searchResult = new JSONObject(result.replaceAll(", ", ", \\"));
        JSONArray items = searchResult.getJSONArray("items");

        return items.getJSONObject(0).getString("login");
    }
}
