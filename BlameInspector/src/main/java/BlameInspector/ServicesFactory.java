package BlameInspector;


public class ServicesFactory {
    public static IssueTrackerService getIssueTrackerService(final String userName, final String password,
                                                      final String repoOwner, final String projectName,
                                                      final String issueTrackerUrl){
        String issueTrackerName = issueTrackerUrl.split("/")[2];
        if (issueTrackerName.equals("github.com")){
            return new GitHubService(userName, password, repoOwner, projectName);
        } else if (issueTrackerName.equals("bitbucket.org")){
            return new BitBucketService(userName, password, repoOwner, projectName);
        }
        return null;
    }
}
