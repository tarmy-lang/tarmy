import TarmyAPI.api;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class runtarm {
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String DARK_RED = "\u001B[31m";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println(YELLOW + "<No source file provided>" + RESET);
            return;
        }

        api tarmyInterpreter = new api();
        int lineCounter = 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
            String line;
            // Step 1: Read and interpret every line into the buffer
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    tarmyInterpreter.interpret(line, lineCounter);
                }
                lineCounter++;
            }
            
            // Step 2: After the file is fully read, trigger the execution engine
            tarmyInterpreter.run();

        } catch (IOException e) {
            // Updated to match your custom error style
            System.err.println(YELLOW + "exception occured please read:" + RESET);
            System.err.println(DARK_RED + "IOException: " + e.getMessage() + RESET);
        }
    }
}