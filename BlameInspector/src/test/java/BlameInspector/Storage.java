package blameinspector;


public interface Storage {

    public static String test1 = "Exception in thread \"main\" java.lang.ArithmeticException: / by zero\n" +
            "at SampleClassOne.invokeJaneException(SampleClassOne.java:14)\n" +
            "at Main.main(Main.java:12)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
            "at sun.reflect.DelegatingMethodAccessorImpl.";
    String test2 = "java.lang.ExceptionInInitializerError\n" +
            "    at com.google.common.base.Splitter.<init>(SourceFile:110)\n" +
            "    at com.google.common.base.Splitter.on(SourceFile:174)\n" +
            "    at com.x.y.BaseApplication.count(SourceFile:900)\n" +
            "    at com.x.y.CheckNewApps.doInBackground(SourceFile:106)\n" +
            "    at com.x.y.CheckNewApps.doInBackground(SourceFile:1)\n" +
            "    at android.os.AsyncTask$2.call(AsyncTask.java:288)\n" +
            "    at java.util.concurrent.FutureTask.run(FutureTask.java:237)\n" +
            "    at android.os.AsyncTask$SerialExecutor$1.run(AsyncTask.java:231)\n" +
            "    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1112)\n" +
            "    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:587)\n" +
            "    at java.lang.Thread.run(Thread.java:811)\n" +
            "Caused by: java.lang.UnsupportedOperationException\n" +
            "    at com.google.common.base.CharMatcher.a(SourceFile:775)\n" +
            "    at com.google.common.base.CharMatcher.<clinit>(SourceFile:212)\n" +
            "    ... 11 more";
    String testKotlin = "{code}fun print(message : String) {" +
            "  System.out?.println(message)}fun String.print() {" +
            "  System.out?.println(this)}{code}Exception in thread \"main\" " +
            "java.lang.ClassFormatError: " +
            "Duplicate method name&signature in class file demo/namespace";
}
