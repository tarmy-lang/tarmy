package TarmyAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;

public class api {
    // ANSI Color Constants
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String LIGHT_RED = "\u001B[91m";
    private static final String DARK_RED = "\u001B[31m";

    private final Map<String, String> keywordMap = new HashMap<>();
    private final List<String> linesBuffer = new ArrayList<>();

    public api() {
        // Keyword Mapping
        keywordMap.put("extern", "public");
        keywordMap.put("import", "import");
    }

    /**
     * Collects and translates lines for the final execution.
     */
    public void interpret(String line, int lineNum) {
        try {
            // Check for obvious syntax errors before buffering
            if (line.trim().startsWith("println") && !line.contains("\"")) {
                showNormalError("Missing quotes in print statement", lineNum, 1);
                return;
            }

            // Keyword Replacement
            String processedLine = line;
            for (Map.Entry<String, String> entry : keywordMap.entrySet()) {
                processedLine = processedLine.replace(entry.getKey(), entry.getValue());
            }

            // Translate Tarmy's println to standard Java
            if (processedLine.trim().startsWith("println")) {
                processedLine = processedLine.replaceFirst("println", "System.out.println");
            }

            linesBuffer.add(processedLine);

        } catch (Exception e) {
            printException(e);
        }
    }

    /**
     * The Execution Engine: Dynamically builds and runs the Java code.
     */
    public void run() {
        String defaultClassName = "TarmyRuntime";
        String targetClass = defaultClassName; 
        boolean hasExplicitClass = false;

        try {
            StringBuilder sourceCode = new StringBuilder();
            List<String> imports = new ArrayList<>();
            List<String> body = new ArrayList<>();

            // 1. Separate imports and identify if the user defined a class
            for (String s : linesBuffer) {
                String trimmed = s.trim();
                if (trimmed.startsWith("import ")) {
                    imports.add(trimmed.endsWith(";") ? trimmed : trimmed + ";");
                } else {
                    body.add(s);
                    // Check if user is defining a class (extern class or class)
                    if (trimmed.contains("class ")) {
                        hasExplicitClass = true;
                        String[] parts = trimmed.split("class ");
                        if (parts.length > 1) {
                            // Extract class name (handles "MyClass {" or "MyClass")
                            targetClass = parts[1].split("\\{")[0].trim();
                        }
                    }
                }
            }

            // 2. Assemble the Java Source Code
            for (String imp : imports) sourceCode.append(imp).append("\n");

            if (hasExplicitClass) {
                // Semicolon safety for Explicit Class Mode
                for (String line : body) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        if (!trimmed.endsWith(";") && !trimmed.endsWith("{") && !trimmed.endsWith("}")) {
                            line += ";";
                        }
                    }
                    sourceCode.append(line).append("\n");
                }
            } else {
                // Script Mode: Wrap logic in a helper class and main method
                sourceCode.append("public class ").append(defaultClassName).append(" {\n");
                sourceCode.append("    public static void main(String[] args) throws Exception {\n");
                for (String s : body) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty()) {
                        // Auto-add semicolons to standard lines, ignore blocks
                        if (!trimmed.endsWith(";") && !trimmed.endsWith("{") && !trimmed.endsWith("}")) {
                            trimmed += ";";
                        }
                        sourceCode.append("        ").append(trimmed).append("\n");
                    }
                }
                sourceCode.append("    }\n}");
            }

            // 3. File Creation & Compilation
            File javaFile = new File(targetClass + ".java");
            Files.writeString(javaFile.toPath(), sourceCode.toString());

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                showNormalError("JDK Required. Ensure you are using a JDK, not a JRE.", 0, 0);
                return;
            }

            // Compile the generated file
            int result = compiler.run(null, null, null, javaFile.getPath());

            if (result == 0) {
                // 4. Dynamic Loading and Execution
                URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File(".").toURI().toURL()});
                Class<?> cls = Class.forName(targetClass, true, classLoader);
                Method mainMethod = cls.getMethod("main", String[].class);
                
                // Invoke main(new String[0])
                mainMethod.invoke(null, (Object) new String[]{});
                
                classLoader.close();
            } else {
                showNormalError("Internal compilation failed. Check Tarmy/Java syntax.", 0, 0);
            }

            // Clean up temporary workspace
            javaFile.delete();
            new File(targetClass + ".class").delete();

        } catch (Exception e) {
            printException(e);
        }
    }

    /**
     * Formats internal Java exceptions with Tarmy's color theme.
     */
    private void printException(Exception e) {
        System.err.println(YELLOW + "exception occured please read:" + RESET);
        
        // Unwrap InvocationTargetException to show the real error (e.g., NullPointerException)
        Throwable cause = (e instanceof java.lang.reflect.InvocationTargetException) ? e.getCause() : e;
        
        System.err.println(LIGHT_RED + cause.getClass().getSimpleName() + ":" + RESET);
        System.err.println(DARK_RED + " " + cause.getMessage() + RESET);
        
        for (StackTraceElement element : cause.getStackTrace()) {
            System.err.println(DARK_RED + "\tat " + element + RESET);
        }
    }

    /**
     * Formats syntax-level errors in Yellow.
     */
    private void showNormalError(String reason, int line, int col) {
        System.out.println(YELLOW + "<" + reason + ">" + RESET);
        System.out.println("in " + line + ":" + col);
    }
}