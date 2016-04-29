package blameinspector.vcs;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.wc2.ISvnObjectReceiver;
import org.tmatesoft.svn.core.wc2.SvnList;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SubversionService extends VersionControlService {

    private Exception nestedException;


    public SubversionService(final String pathToRepo,
                             final String repoURL, final boolean isParsingCode) throws SVNException, VersionControlServiceException {
        filesInRepo = new HashMap<>();
        methodLocation = new HashMap<>();
        this.repositoryURL = repoURL;
        this.pathToRepo = pathToRepo;
        File workingCopyLoc = new File(pathToRepo);
        SVNRevision revision = SVNRevision.HEAD;
        SvnOperationFactory operationFactory = new SvnOperationFactory();
        SvnList list = operationFactory.createList();
        list.setDepth(SVNDepth.IMMEDIATES);
        list.setRevision(revision);
        list.addTarget(SvnTarget.fromFile(workingCopyLoc));
        list.setReceiver(new ISvnObjectReceiver<SVNDirEntry>() {
            public void receive(final SvnTarget target, final SVNDirEntry object) throws SVNException {
                String dirName = object.getName();
                try {
                    if (!dirName.equals("") && !dirName.equals("tags")) {
                        Files.walk(Paths.get(pathToRepo + "\\" + object.getRelativePath())).forEach(filePath -> {
                            if (Files.isRegularFile(filePath)) {
                                File file = new File(String.valueOf(filePath));
                                if (!filesInRepo.containsKey(file.getName())) {
                                    filesInRepo.put(file.getName(), new ArrayList<String>());
                                }
                                filesInRepo.get(file.getName()).add(String.valueOf(filePath));
                                try {
                                    indexMethods(String.valueOf(filePath));
                                } catch (Exception e) {
                                    nestedException = e;
                                    return;
                                }
                            }
                        });
                    }
                } catch (NoSuchFileException e) {
                    return;
                } catch (IOException e) {
                    nestedException = e;
                    return;
                }
            }
        });
        list.run();
        if (nestedException != null) {
            throw new VersionControlServiceException(nestedException, "Something wrong with svn dir!");
        }
    }


    private AnnotationHandler doBlame(final String fileName, final String className, final int lineNumber) throws VersionControlServiceException {
        SVNLogClient logClient = SVNClientManager.newInstance().getLogClient();
        AnnotationHandler annotationHandler = new AnnotationHandler(false, false, logClient.getOptions(), lineNumber);
        try {
            String filePath = getFilePath(fileName, className);
            logClient.doAnnotate(new File(filePath), SVNRevision.HEAD,
                    SVNRevision.create(0),
                    SVNRevision.HEAD, annotationHandler);
            return annotationHandler;
        } catch (Exception e) {
            throw new VersionControlServiceException(e, e.getMessage());
        }
    }

    @Override
    public String getBlamedUserEmail(final String fileName, final String className,
                                     final int lineNumber) {
        try {
            return doBlame(fileName, className, lineNumber).getAuthor();
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public String getBlamedUserName(String fileName, String className, int lineNumber) {
        try {
            return doBlame(fileName, className, lineNumber).getAuthor();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getBlamedUserCommit(final String fileName, final String className, final int lineNumber) {
        try {
            return doBlame(fileName, className, lineNumber).getRevision();
        } catch (Exception e) {
            return null;
        }
    }


    private static class AnnotationHandler implements ISVNAnnotateHandler {
        private boolean myIsUseMergeHistory;
        private boolean myIsVerbose;
        private ISVNOptions myOptions;

        private String author;
        private String revision;
        private int lineNumber;

        public AnnotationHandler(final boolean useMergeHistory,
                                 final boolean verbose,
                                 final ISVNOptions options,
                                 final int lineNumber) {
            myIsUseMergeHistory = useMergeHistory;
            myIsVerbose = verbose;
            myOptions = options;
            this.lineNumber = lineNumber;
        }

        @Deprecated
        @Override
        public void handleLine(final Date date,
                               final long l,
                               final String s,
                               final String s2) throws SVNException {
        }

        /**
         * Formats per line information and prints it out to the console.
         */
        public void handleLine(final Date date,
                               final long revision,
                               final String author,
                               final String line,
                               final Date mergedDate,
                               final long mergedRevision,
                               final String mergedAuthor,
                               final String mergedPath,
                               final int lineNumber) throws SVNException {
            String resAuthor = author;
            String rev = String.valueOf(revision);
            if (myIsUseMergeHistory) {
                resAuthor = mergedAuthor;
                rev = String.valueOf(mergedRevision);
            }
            if (lineNumber == this.lineNumber) {
                this.author = resAuthor;
                this.revision = rev;
            }
        }

        public boolean handleRevision(final Date date,
                                      final long revision,
                                      final String author,
                                      final File contents) throws SVNException {
            /* We do not want our file to be annotated for each revision of the range, but only for the last
             * revision of it, so we return false
             */
            return false;
        }

        public String getAuthor() {
            return author;
        }

        public void handleEOF() {
        }

        public String getRevision() {
            return revision;
        }
    }
}
