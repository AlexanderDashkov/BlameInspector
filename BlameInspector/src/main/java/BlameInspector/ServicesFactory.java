package blameinspector;


import blameinspector.issuetracker.*;
import blameinspector.vcs.GitService;
import blameinspector.vcs.SubversionService;
import blameinspector.vcs.VersionControlService;
import blameinspector.vcs.VersionControlServiceException;

public class ServicesFactory {

    private static final String GITHUB_URL = "github.com";
    private static final String BITBUCKET_URL = "bitbucket.org";
    private static final String YOUTRACK_URL = "youtrack.jetbrains.com";

    private static final String GIT = "git";
    private static final String SVN = "svn";

    public static IssueTrackerService getIssueTrackerService(final String userName, final String password,
                                                      final String repoOwner, final String projectName,
                                                      final String issueTrackerUrl) throws IssueTrackerException {
        String issueTrackerName = issueTrackerUrl.split("/")[2];
        try {
            if (issueTrackerName.equals(GITHUB_URL)) {
                return new GitHubService(userName, password, repoOwner, projectName);
            } else if (issueTrackerName.equals(BITBUCKET_URL)) {
                return new BitBucketService(userName, password, repoOwner, projectName);
            } else if (issueTrackerName.equals(YOUTRACK_URL)) {
                return new YouTrackService(userName, password, repoOwner, projectName, issueTrackerUrl);
            }
            throw new IssueTrackerException("Not found appropriate Issue Tracker constructor.");
        } catch (IssueTrackerException e){
            throw e;
        } catch (Exception e){
            throw new IssueTrackerException(e);
        }
    }

    public static VersionControlService getVersionControlService(final String versionControl,
                                                                 final String pathToRepo,
                                                                 final String issueTracker)
            throws VersionControlServiceException {
        try {
            if (versionControl.equals(GIT)) {
                return new GitService(pathToRepo, issueTracker);
            } else if (versionControl.equals(SVN)) {
                return new SubversionService(pathToRepo, issueTracker);
            }
            throw new VersionControlServiceException("Not found appropriate Version Control constructor.");
        } catch (VersionControlServiceException e){
            throw e;
        } catch (Exception e){
            throw new VersionControlServiceException(e);
        }
    }
}
