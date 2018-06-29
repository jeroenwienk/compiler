package nl.jwienk.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Executor {

    private BufferedReader error;
    private BufferedReader op;
    private int exitVal;

    public void executeJar(String jarPath, String fileName) {
        final List<String> args = new ArrayList<>();
        args.add(0, "java");
        args.add(1, "-jar");
        args.add(2, jarPath);
        args.add(fileName);
        run(args);
    }

    public void execute(String fileName) {
        final List<String> args = new ArrayList<>();
        args.add(0, "java");
        args.add(fileName);
        run(args);
    }

    private void run(List<String> args) {
        try {
            final Runtime re = Runtime.getRuntime();
            final Process command = re.exec(args.toArray(new String[0]));
            this.error = new BufferedReader(new InputStreamReader(command.getErrorStream()));
            this.op = new BufferedReader(new InputStreamReader(command.getInputStream()));
            command.waitFor();
            this.exitVal = command.exitValue();
            if (this.exitVal != 0) {
                throw new IOException("Failed to execute, " + this.getExecutionLog());
            }

        } catch (final Exception e) {
           e.printStackTrace();
        }
    }

    public String getExecutionLog() {
        StringBuilder error = new StringBuilder();
        String line;
        try {
            while ((line = this.error.readLine()) != null) {
                error.append("\n").append(line);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        StringBuilder output = new StringBuilder();
        try {
            while ((line = this.op.readLine()) != null) {
                output.append("\n").append(line);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            this.error.close();
            this.op.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return "exit: " + this.exitVal + ", error: " + error + ", result:\n" + output;
    }

}