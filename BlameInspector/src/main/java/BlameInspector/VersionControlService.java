package BlameInspector;


import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.HashMap;

public abstract class VersionControlService {

    public HashMap<String, String> filesInRepo;
    protected String repositoryURL;

    public abstract String getBlamedUser(String fileName, int lineNumber) throws IOException, GitAPIException;

    public abstract String getRepositoryOwner();
}
