package blameinspector.vcs;


import com.github.antlrjavaparser.JavaParser;
import com.github.antlrjavaparser.api.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class VersionControlService {

    protected HashMap<String, ArrayList<String>> filesInRepo;
    protected HashMap<String, String> methodLocation;
    protected String repositoryURL;
    protected boolean isParsingCode;

    protected String pathToRepo;

    public abstract String getBlamedUserCommit(final String fileName,
                                               final String className,
                                               final int lineNumber);

    public abstract String getBlamedUserEmail(final String fileName,
                                              final String className,
                                              final int lineNumber);

    public abstract String getBlamedUserName(final String fileName,
                                             final String className,
                                             final int lineNumber);

    public String getRepositoryOwner() {
        String[] urlParts = repositoryURL.split("/");
        return repositoryURL.split("/")[urlParts.length - 2];
    }

    public boolean containsFile(final String fileName) {
        return filesInRepo.containsKey(fileName);
    }

//    public String containsCode(final String className, final String methName) {
//        try {
//            String[] clsNameArr = className.split("\\.");
//            String clsName = clsNameArr[clsNameArr.length - 1];
//            if (clsName.contains("$")) {
//                clsName = clsName.split("\\$")[1];
//            }
//            String methodName = methName;
//            if (methodName.equals("<init>") || methodName.equals("<clinit>")) {
//                methodName = clsName;
//            }
//            for (String path : getFilesByFolder(className)) {
//                File file = new File(path);
//                CompilationUnit compilationUnit = JavaParser.parse(file);
//                VoidVisitorImpl visitor = new VoidVisitorImpl(clsName, methodName);
//                compilationUnit.accept(visitor, null);
//                if (visitor.isFound()) {
//                    return path;
//                }
//            }
//        } catch (Exception e) {
//        }
//        return null;
//    }

    protected ArrayList<String> getFilesByFolder(final String className) {
        String[] folders = className.split("\\.");
        ArrayList<String> result = new ArrayList<>();
        String[] projectName = pathToRepo.split("\\\\");
        String currentPath = pathToRepo + "\\" + projectName[projectName.length - 1] + "\\src\\";
        outterloop:
        for (int i = 0; i < folders.length - 1; i++) {
            File dir = new File(currentPath);
            for (File file : dir.listFiles()) {
                if (file.getName().equals(folders[i])) {
                    currentPath += "\\" + folders[i];
                    continue outterloop;
                }
            }
        }
        File dir = new File(currentPath);
        for (File file : dir.listFiles()) {
            if (file.getName().contains(".java")) {
                result.add(file.getPath());
            }
        }
        return result;
    }

    protected void indexMethods(final String filePath) throws VersionControlServiceException {
        if (!filePath.contains(".java")){
            return;
        }
        try {
            File file = new File(pathToRepo + "\\" +filePath.replace("/", "\\"));
            CompilationUnit compilationUnit = JavaParser.parse(file);
            VoidVisitorImpl visitor = new VoidVisitorImpl();
            compilationUnit.accept(visitor, null);
            String classPackage = compilationUnit.getPackage()!= null ?
                    compilationUnit.getPackage().getName().toString() : "" ;
            for (String methodName : visitor.getMethods()) {
                methodLocation.put(classPackage + "." + methodName,
                        filePath);
            }
        }catch (Exception e){
            throw new VersionControlServiceException(e);
        }
    }

    protected String getFilePath(final String fileName, final String className) throws IOException {
        if (fileName.contains("\\")) {
            String correctedPath = fileName.replace(pathToRepo, "").substring(1).replace("\\", "/");
            return correctedPath;
        }
        if (fileName.contains("/")){
            return fileName;
        }
        String pathPart = className.split("$")[0].replace(".", "\\");
        for (String path : filesInRepo.get(fileName)) {
            if (path.contains(pathPart + fileName)) {
                return path;
            }
        }
        return filesInRepo.get(fileName).get(0);
    }

    public BlamedUserInfo getBlamedUserInfo(final String fileName, final String className, final int lineNumber) {
        return new BlamedUserInfo(getBlamedUserName(fileName, className, lineNumber),
                getBlamedUserEmail(fileName, className, lineNumber),
                getBlamedUserCommit(fileName, className, lineNumber));
    }

    public String containsMethod(final String methodName) {
        return methodLocation.get(methodName);
    }
}