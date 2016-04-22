package blameinspector;

/**
 * Created by Alexander on 26.02.2016.
 */
public class TraceInfo {
    private String className;
    private String methodName;
    private String fileName;
    private int lineNumber;

    public TraceInfo(final String className,
                     final String methodName,
                     final String fileName,
                     final int lineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFileName() {
        return fileName;
    }
}
