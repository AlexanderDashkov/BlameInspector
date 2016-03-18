package BlameInspector;


import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.io.IOException;

public class SubversionService extends VersionControlService {

    private SVNClientManager svnClientManager;
    private SVNURL svnUrl;

    public SubversionService(final String pathToRepo,
                             final String repoURL,
                             final String userName,
                             final String password) {
        this.repositoryURL = repoURL;
        ISVNOptions options = SVNWCUtil.createDefaultOptions(new File(pathToRepo), true);
        svnClientManager = SVNClientManager.newInstance((org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions) options,
                userName, password);

    }
    @Override
    public String getBlamedUser(String fileName, int lineNumber) throws IOException, SVNException {
        SVNURL fileURL = SVNURL.parseURIEncoded(repositoryURL + fileName);
        SVNLogClient logClient = SVNClientManager.newInstance().getLogClient();

        return null;
    }

    @Override
    public String getRepositoryOwner() {
        return null;
    }
}
