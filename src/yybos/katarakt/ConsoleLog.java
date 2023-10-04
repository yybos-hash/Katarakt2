package yybos.katarakt;

public class ConsoleLog {
    public static void warning (String message) {
        // Get the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String fileName = "";
        String methodName = "";

        if (stackTrace.length >= 3) { // Check if there are at least 3 elements in the stack trace
            // The class name is in the third element of the stack trace
            fileName = stackTrace[2].getFileName();
            methodName = stackTrace[2].getMethodName();

            if (fileName != null)
                fileName = fileName.substring(0, fileName.length() - 5);
        }

        String template = "[" + fileName + '.' + methodName + " - WARNING]";

        System.out.println(template + " ".repeat(46 - template.length()) + message);
    }
    public static void info (String message) {
        // Get the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String fileName = "";
        String methodName = "";

        if (stackTrace.length >= 3) { // Check if there are at least 3 elements in the stack trace
            // The class name is in the third element of the stack trace
            fileName = stackTrace[2].getFileName();
            methodName = stackTrace[2].getMethodName();

            if (fileName != null)
                fileName = fileName.substring(0, fileName.length() - 5);
        }

        String template = "[" + fileName + '.' + methodName + " - INFO]";

        System.out.println(template + " ".repeat(46 - template.length()) + message);
    }
    public static void error (String message) {
        // Get the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String fileName = "";
        String methodName = "";

        if (stackTrace.length >= 3) { // Check if there are at least 3 elements in the stack trace
            // The class name is in the third element of the stack trace
            fileName = stackTrace[2].getFileName();
            methodName = stackTrace[2].getMethodName();

            if (fileName != null)
                fileName = fileName.substring(0, fileName.length() - 5);
        }

        String template = "[" + fileName + '.' + methodName + " - ERROR]";

        System.out.println(template + " ".repeat(46 - template.length()) + message);
    }
    public static void exception (String message) {
        // Get the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String fileName = "";
        String methodName = "";

        if (stackTrace.length >= 3) { // Check if there are at least 3 elements in the stack trace
            // The class name is in the third element of the stack trace
            fileName = stackTrace[2].getFileName();
            methodName = stackTrace[2].getMethodName();

            if (fileName != null)
                fileName = fileName.substring(0, fileName.length() - 5);
        }

        String template = "[" + fileName + '.' + methodName + " - EXCEPTION]";

        System.out.println(template + " ".repeat(46 - template.length()) + message);
    }
}
