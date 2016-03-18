package BlameInspector;


import org.eclipse.jgit.api.errors.GitAPIException;
import org.tmatesoft.svn.core.SVNException;

import java.io.IOException;
import java.util.HashMap;

public abstract class VersionControlService {

    protected HashMap<String, String> filesInRepo;
    protected String repositoryURL;

    public abstract String getBlamedUser(String fileName, int lineNumber) throws IOException, GitAPIException, SVNException;

    public abstract String getRepositoryOwner();

    public boolean containsFile(final String fileName){
        return filesInRepo.containsKey(fileName);
    }
}