package BlameInspector;


import BlameInspector.IssueTracker.BitBucketService;
import BlameInspector.IssueTracker.GitHubService;
import BlameInspector.IssueTracker.IssueTrackerService;
import BlameInspector.VCS.GitService;
import BlameInspector.VCS.SubversionService;
import BlameInspector.VCS.VersionControlService;
import BlameInspector.VCS.VersionControlServiceException;
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
        throw new RuntimeException("Not found appropriate Issue Tracker constructor.");
    }

    public static VersionControlService getVersionControlService(final String versionControl,
                                                                 final String pathToRepo,
                                                                 final String issueTracker)
            throws NoSuchMethodException,
            IOException,
            SVNException, VersionControlServiceException {
        if (versionControl.equals(GIT)){
            return new GitService(pathToRepo, issueTracker);
        } else if (versionControl.equals(SVN)){
           return new SubversionService(pathToRepo, issueTracker);
        }
        throw new RuntimeException("Not found appropriate Version Control constructor.");
    }
}
