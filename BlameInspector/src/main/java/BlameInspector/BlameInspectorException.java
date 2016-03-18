package BlameInspector;

public class BlameInspectorException extends Exception{

    protected Throwable nestedException;

    public BlameInspectorException(Exception e) {
        this.nestedException = e;
    }

    public Throwable getNestedException(){
        return this.nestedException;
    }
}
