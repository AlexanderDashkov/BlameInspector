package BlameInspector;


public interface Storage {

    public static String test1 = "Exception in thread \"main\" java.lang.ArithmeticException: / by zero\n" +
            "at SampleClassOne.invokeJaneException(SampleClassOne.java:14)\n" +
            "at Main.main(Main.java:12)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
            "at sun.reflect.DelegatingMethodAccessorImpl.";
}
