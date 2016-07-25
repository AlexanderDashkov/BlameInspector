package blameinspector.vcs;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class GitService extends VersionControlService {

    private Git git;
    private ObjectId commitID;
    private ConcurrentHashMap<String, HashMap<Integer, String>> blameEmails;
    private ConcurrentHashMap<String, HashMap<Integer, String>> blameNames;
    private ConcurrentHashMap<String, HashMap<Integer, String>> blameCommitsID;
    private ConcurrentHashMap<String, BlameResult> blameResults;

    public GitService(final String pathToRepo, final String repoURL, final boolean isParsingCode)
            throws VersionControlServiceException {
        this.isParsingCode = isParsingCode;
        this.blameNames = new ConcurrentHashMap<>();
        this.blameEmails = new ConcurrentHashMap<>();
        this.blameCommitsID = new ConcurrentHashMap<>();
        this.blameResults = new ConcurrentHashMap<>();
        filesInRepo = new ConcurrentHashMap<>();
        methodLocation = new ConcurrentHashMap<>();
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
            //    if (isParsingCode) {
            //        indexMethods(treeWalk.getPathString());
            //    }
            }
//        } catch (VersionControlServiceException e) {
//            throw e;
        } catch (Exception e) {
            throw new VersionControlServiceException(e);
        }
    }

    public String getBlamedUserCommit(final String fileName, final String className,
                                      final int lineNumber, final BlameResult blameResult) {
        try {
            String blameCommit = blameResult.getSourceCommit(lineNumber - 1).getName();
            if (!blameCommitsID.containsKey(fileName)){
                blameCommitsID.put(fileName, new HashMap<>());
            }
            blameCommitsID.get(fileName).put(lineNumber, blameCommit);
            return blameCommit;
        } catch (Exception e) {
            //throw new VersionControlServiceException(e, e.getMessage());
            return null;
        }
    }


    public String getBlamedUserEmail(final String fileName, final String className,
                                     final int lineNumber, final BlameResult blameResult) {
        try {
            //String filePath = getFilePath(fileName, className);
            //filePath = filePath.replace(this.pathToRepo, "");
            String blamedUserEmail = blameResult.getSourceAuthor(lineNumber - 1).getEmailAddress();
            String chunckedEmail[] = blamedUserEmail.split("@");
            if (chunckedEmail.length > 2) {
                blamedUserEmail = chunckedEmail[0] + "@" + chunckedEmail[1];
            }
            if (!blameEmails.containsKey(fileName)){
                blameEmails.put(fileName, new HashMap<>());
            }
            blameEmails.get(fileName).put(lineNumber, blamedUserEmail);
            return blamedUserEmail;
        } catch (Exception e) {
            //throw new VersionControlServiceException(e, e.getMessage());
            return null;
        }
    }

    public String getBlamedUserName(final String fileName, final String className,
                                    final int lineNumber, final BlameResult blameResult) {
        try {
            String blamedUserName = blameResult.getSourceAuthor(lineNumber - 1).getName();
            if (!blameNames.containsKey(fileName)){
                blameNames.put(fileName, new HashMap<>());
            }
            blameNames.get(fileName).put(lineNumber, blamedUserName);
            return blamedUserName;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public BlamedUserInfo getBlamedUserInfo(final String fileName, final String className, final int lineNumber) throws GitAPIException, IOException {
        String blamedUserName = null;
        String blamedUserEmail = null;
        String blamedUserCommit = null;
        if (blameNames.containsKey(fileName)){
            if (blameNames.get(fileName).containsKey(lineNumber)){
                blamedUserName = blameNames.get(fileName).get(lineNumber);
            }
        }
        if (blameEmails.containsKey(fileName)){
            if (blameEmails.get(fileName).containsKey(lineNumber)){
                blamedUserEmail = blameEmails.get(fileName).get(lineNumber);
            }
        }
        if (blameCommitsID.containsKey(fileName)){
            if (blameCommitsID.get(fileName).containsKey(lineNumber)){
                blamedUserCommit = blameCommitsID.get(fileName).get(lineNumber);
            }
        }
        if(blamedUserCommit == null || blamedUserName == null || blamedUserEmail == null){
            String filePath = getFilePath(fileName, className);
            BlameResult blameResult;
            if(blameResults.containsKey((filePath))){
                blameResult = blameResults.get(filePath);
            }else {
                BlameCommand cmd = new BlameCommand(git.getRepository());
                cmd.setStartCommit(commitID);
                cmd.setFilePath(filePath);
                blameResult = cmd.call();
                blameResults.put(filePath, blameResult);
            }
            blamedUserName = blamedUserName != null ? blamedUserName
                    : getBlamedUserName(fileName, className, lineNumber, blameResult);
            blamedUserEmail = blamedUserEmail != null ? blamedUserName
                    : getBlamedUserEmail(fileName, className, lineNumber, blameResult);
            blamedUserCommit = blamedUserCommit != null ? blamedUserCommit
                    : getBlamedUserCommit(fileName, className, lineNumber, blameResult);
        }
        return new BlamedUserInfo(blamedUserName, blamedUserEmail, blamedUserCommit);
    }


}