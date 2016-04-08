package BlameInspector.VCS;


import java.util.HashMap;

public abstract class VersionControlService {

    protected HashMap<String, String> filesInRepo;
    protected String repositoryURL;

    public abstract String getBlamedUserCommit(String fileName, int lineNumber) throws Exception;
    public abstract String getBlamedUserEmail(String fileName, int lineNumer) throws Exception;

    public String getRepositoryOwner(){
        String []urlParts = repositoryURL.split("/");
        return repositoryURL.split("/")[urlParts.length - 2];
    };

    public boolean containsFile(final String fileName){
        return filesInRepo.containsKey(fileName);
    }
}
