package BlameInspector;


import org.tmatesoft.svn.core.SVNException;

import java.io.IOException;

public class ServicesFactory {

    private static final String GITHUB_URL = "github.com";
    private static final String BITBUCKET_URL = "bitbucket.org";

    private static final String GIT = "git";
    private static final String SVN = "svn";

    public static IssueTrackerService getIssueTrackerService(final String userName, final String password,
                                                      final String repoOwner, final String projectName,
                                                      final String issueTrackerUrl) throws NoSuchMethodException, IOException {
        String issueTrackerName = issueTrackerUrl.split("/")[2];
        if (issueTrackerName.equals(GITHUB_URL)){
            return new GitHubService(userName, password, repoOwner, projectName);
        } else if (issueTrackerName.equals(BITBUCKET_URL)){
            return new BitBucketService(userName, password, repoOwner, projectName);
        }
        throw new NoSuchMethodException("Not found appropriate Issue Tracker constructor.");
    }

    public static VersionControlService getVersionControlService(final String versionControl,
                                                                 final String pathToRepo,
                                                                 final String issueTracker,
                                                                 final String username,
                                                                 final String password) throws NoSuchMethodException, IOException, SVNException {
        if (versionControl.equals(GIT)){
            return new GitService(pathToRepo, issueTracker);
        }else if (versionControl.equals(SVN)){
           return new SubversionService(pathToRepo, issueTracker, username, password);
        }
        throw new NoSuchMethodException("Not found appropriate Version Control constructor.");
    }
}
