public class Logger {

    private static final String header = "> BIF-BN-PARSER >";

    public static void log(String log){
        System.out.println(header + " LOG: " + log);
    }

    public static void err(String log){
        System.err.println(header + " error: " + log);
    }
}
