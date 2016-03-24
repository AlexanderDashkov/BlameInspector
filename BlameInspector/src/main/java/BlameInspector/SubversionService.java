package BlameInspector;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.wc2.ISvnObjectReceiver;
import org.tmatesoft.svn.core.wc2.SvnList;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;

public class SubversionService extends VersionControlService {

    private SVNClientManager svnClientManager;
    private SVNURL svnUrl;

    public SubversionService(final String pathToRepo,
                             final String repoURL,
                             final String userName,
                             final String password) throws SVNException {
        filesInRepo = new HashMap<>();
        File workingCopyLoc = new File(pathToRepo);
        SVNRevision revision = SVNRevision.HEAD;
        SvnOperationFactory operationFactory = new SvnOperationFactory();
        SvnList list = operationFactory.createList();
        list.setDepth(SVNDepth.IMMEDIATES);
        list.setRevision(revision);
        list.addTarget(SvnTarget.fromFile(workingCopyLoc));
        list.setReceiver(new ISvnObjectReceiver<SVNDirEntry>() {
            public void receive(SvnTarget target, SVNDirEntry object) throws SVNException {
                String dirName = object.getName();
                try {
                    if (!dirName.equals("")) {
                        Files.walk(Paths.get(pathToRepo + "\\" + object.getRelativePath())).forEach(filePath -> {
                            if (Files.isRegularFile(filePath)) {
                                File file = new File(String.valueOf(filePath));
                                filesInRepo.put(file.getName(), String.valueOf(filePath));
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        list.run();
    }


    @Override
    public String getBlamedUser(String fileName, int lineNumber) throws IOException, SVNException {
        SVNLogClient logClient = SVNClientManager.newInstance().getLogClient();
        AnnotationHandler annotationHandler = new AnnotationHandler(false, false, logClient.getOptions(),lineNumber);
        logClient.doAnnotate(new File(filesInRepo.get(fileName)), SVNRevision.UNDEFINED,
                SVNRevision.create(1),
                SVNRevision.HEAD, annotationHandler);

        return annotationHandler.getAuthor();
    }

    @Override
    public String getRepositoryOwner() {
        return null;
    }



    private static class AnnotationHandler implements ISVNAnnotateHandler {
        private boolean myIsUseMergeHistory;
        private boolean myIsVerbose;
        private ISVNOptions myOptions;

        private String author;
        private int lineNumber;

        public AnnotationHandler(boolean useMergeHistory, boolean verbose, ISVNOptions options, int lineNumber) {
            myIsUseMergeHistory = useMergeHistory;
            myIsVerbose = verbose;
            myOptions = options;
            this.lineNumber = lineNumber;
        }

        @Deprecated
        @Override
        public void handleLine(Date date, long l, String s, String s2) throws SVNException {
        }

        /**
         * Formats per line information and prints it out to the console.
         */
        public void handleLine(Date date, long revision, String author, String line, Date mergedDate,
                               long mergedRevision, String mergedAuthor, String mergedPath, int lineNumber) throws SVNException {
            if(myIsUseMergeHistory) {
                author = mergedAuthor;
            }
            if (lineNumber == this.lineNumber) {
                this.author = author;
            }
        }

        public boolean handleRevision(Date date, long revision, String author, File contents) throws SVNException {
            /* We do not want our file to be annotated for each revision of the range, but only for the last
             * revision of it, so we return false
             */
            return false;
        }

        public String getAuthor() {
            return author;
        }
        public void handleEOF() {}
    }
}
