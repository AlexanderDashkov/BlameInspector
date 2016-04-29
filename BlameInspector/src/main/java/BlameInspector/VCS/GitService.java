package blameinspector.vcs;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class GitService extends VersionControlService {

    private Git git;
    private ObjectId commitID;

    public GitService(final String pathToRepo, final String repoURL, final boolean isParsingCode)
            throws VersionControlServiceException {
        this.isParsingCode = isParsingCode;
        filesInRepo = new HashMap<>();
        methodLocation = new HashMap<>();
        repositoryURL = repoURL;
        this.pathToRepo = pathToRepo;
        try {
            git = Git.open(new File(pathToRepo + "/.git"));
            commitID = git.getRepository().resolve("HEAD");
            RevWalk walk = new RevWalk(git.getRepository());
            RevCommit commit = walk.parseCommit(commitID);
            RevTree tree = commit.getTree();
            TreeWalk treeWalk = new TreeWalk(git.getRepository());
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                if (!filesInRepo.containsKey(treeWalk.getNameString())) {
                    filesInRepo.put(treeWalk.getNameString(), new ArrayList<String>());
                }
                filesInRepo.get(treeWalk.getNameString()).add(treeWalk.getPathString());
                if (isParsingCode) {
                    indexMethods(treeWalk.getPathString());
                }
            }
        } catch (VersionControlServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new VersionControlServiceException(e);
        }
    }


    @Override
    public String getBlamedUserCommit(final String fileName, final String className,
                                      final int lineNumber) {
        try {
            String filePath = getFilePath(fileName, className);
            BlameCommand cmd = new BlameCommand(git.getRepository());
            cmd.setStartCommit(commitID);
            cmd.setFilePath(filePath);
            BlameResult blameResult = cmd.call();
            String blameCommit = blameResult.getSourceCommit(lineNumber - 1).getName();
            return blameCommit;
        } catch (Exception e) {
            //throw new VersionControlServiceException(e, e.getMessage());
            return null;
        }
    }

    @Override
    public String getBlamedUserEmail(final String fileName, final String className,
                                     final int lineNumber) {
        try {
            String filePath = getFilePath(fileName, className);
            BlameCommand cmd = new BlameCommand(git.getRepository());
            cmd.setStartCommit(commitID);
            cmd.setFilePath(filePath);
            BlameResult blameResult = cmd.call();
            String blamedUserEmail = blameResult.getSourceAuthor(lineNumber - 1).getEmailAddress();
            if (blamedUserEmail.split("@").length > 2) {
                String chunkedEmail[] = blamedUserEmail.split("@");
                blamedUserEmail = chunkedEmail[0] + "@" + chunkedEmail[1];
            }
            return blamedUserEmail;
        } catch (Exception e) {
            //throw new VersionControlServiceException(e, e.getMessage());
            return null;
        }
    }

    public String getBlamedUserName(final String fileName, final String className, final int lineNumber) {
        try {
            String filePath = getFilePath(fileName, className);
            BlameCommand cmd = new BlameCommand(git.getRepository());
            cmd.setStartCommit(commitID);
            cmd.setFilePath(filePath);
            BlameResult blameResult = cmd.call();
            String blamedUserName = blameResult.getSourceAuthor(lineNumber - 1).getName();
            return blamedUserName;
        } catch (Exception e) {
            return null;
        }
    }


}