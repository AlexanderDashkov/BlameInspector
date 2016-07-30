package blameinspector.unittest;


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


    String testDuplicate1 = "Exception in thread \"main\" java.lang.ArithmeticException: / by zero\n" +
            "at SampleClassOne.invokeJaneException(SampleClassOne.java:14)\n" +
            "at Main.main(Main.java:12)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
            "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
            "at java.lang.reflect.Method.invoke(Method.java:483)\n" +
            "at com.intellij.rt.execution.application.AppMain.main(AppMain.java:134)";
    String testDuplicate2 = "Exception in thread \"main\" java.lang.RuntimeException\n" +
            "at ru.sample.SampleClassTwo.packageExceptionMethod(SampleClassTwo.java:9)\n" +
            "at Main.main(Main.java:10)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
            "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
            "at java.lang.reflect.Method.invoke(Method.java:483)\n" +
            "at com.intellij.rt.execution.application.AppMain.main(AppMain.java:134)";
    String testDuplicate3 = "Exception in thread \"main\" java.nio.file.NoSuchFileException: fileToDelete_jdk7.txt\n" +
            "at sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:79)\n" +
            "at sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:97)\n" +
            "at sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:102)\n" +
            "at sun.nio.fs.WindowsFileSystemProvider.implDelete(WindowsFileSystemProvider.java:269)\n" +
            "at sun.nio.fs.AbstractFileSystemProvider.delete(AbstractFileSystemProvider.java:103)\n" +
            "at java.nio.file.Files.delete(Files.java:1126)\n" +
            "at SampleClassOne.invokeJackException(SampleClassOne.java:16)\n" +
            "at Main.main(Main.java:13)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
            "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
            "at java.lang.reflect.Method.invoke(Method.java:483)\n" +
            "at com.intellij.rt.execution.application.AppMain.main(AppMain.java:134)";
    String testDuplicate4 = "Exception in thread \"main\" java.lang.ArithmeticException: / by zero\n" +
            "at SampleClassOne.invokeJaneException(SampleClassOne.java:14)\n" +
            "at Main.main(Main.java:12)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
            "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
            "at java.lang.reflect.Method.invoke(Method.java:483)\n" +
            "at com.intellij.rt.execution.application.AppMain.main(AppMain.java:134)";
    String testDuplicate5 = "Exception in thread \"main\" java.lang.RuntimeException\n" +
            "at ru.sample.SampleClassTwo.packageExceptionMethod(SampleClassTwo.java:9)\n" +
            "at Main.main(Main.java:10)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
            "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
            "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
            "at java.lang.reflect.Method.invoke(Method.java:483)\n" +
            "at com.intellij.rt.execution.application.AppMain.main(AppMain.java:134)";

}
