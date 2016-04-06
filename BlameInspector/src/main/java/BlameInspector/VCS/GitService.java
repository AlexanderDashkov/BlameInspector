package BlameInspector.VCS;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class GitService extends VersionControlService {

    private Git git;
    private ObjectId commitID;

    public GitService(final String pathToRepo, final String repoURL) throws IOException{
        filesInRepo = new HashMap<>();
        repositoryURL = repoURL;
        git = Git.open(new File(pathToRepo + "/.git"));
        commitID = git.getRepository().resolve("HEAD");
        RevWalk walk = new RevWalk(git.getRepository());
        RevCommit commit = walk.parseCommit(commitID);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(git.getRepository());
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        while (treeWalk.next()) {
            filesInRepo.put(treeWalk.getNameString(), treeWalk.getPathString());
        }
    }


    @Override
    public String getBlamedUserCommit(final String fileName, final int lineNumber) throws VersionControlServiceException {
        try {
            BlameCommand cmd = new BlameCommand(git.getRepository());
            cmd.setStartCommit(commitID);
            cmd.setFilePath(filesInRepo.get(fileName));
            BlameResult blameResult = cmd.call();
            String blameCommit  = blameResult.getSourceCommit(lineNumber - 1).getName();
            return blameCommit;
        }catch (Exception e){
            throw new VersionControlServiceException(e, e.getMessage());
        }
    }

    @Override
    public String getBlamedUserEmail(final String fileName, final int lineNumber) throws VersionControlServiceException {
        try {
            BlameCommand cmd = new BlameCommand(git.getRepository());
            cmd.setStartCommit(commitID);
            cmd.setFilePath(filesInRepo.get(fileName));
            BlameResult blameResult = cmd.call();
            String blamedUserEmail = blameResult.getSourceAuthor(lineNumber - 1).getEmailAddress();
            if (blamedUserEmail.split("@").length > 2) {
                String chunkedEmail[] = blamedUserEmail.split("@");
                blamedUserEmail = chunkedEmail[0] + "@" + chunkedEmail[1];
            }
            return blamedUserEmail;
        }catch (Exception e){
            throw new VersionControlServiceException(e, e.getMessage());
        }
    }

    public String getRepositoryOwner(){
        String []urlParts = repositoryURL.split("/");
        return repositoryURL.split("/")[urlParts.length - 2];
    }
}
