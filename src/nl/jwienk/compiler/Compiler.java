package nl.jwienk.compiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;


// TODO ADD LIMITS AND LOCALS!!!!!!!

public class Compiler {

    //used for testing without a file
    private String defaultCode = "a = 5 ;  b = 4 ; a = 34 + 34 + b / 4 ;\n" +
            "print(a);" +
            " print(34 + 2.5 - 8 /4);" +
            "if (a == 5) { print(b); }";

    private String startProg = ".class public {{name}}\n" +
            ".super java/lang/Object\n" +
            "\n" +
            ";\n" +
            "; standard initializer (calls java.lang.Object's initializer)\n" +
            ";\n" +
            ".method public <init>()V\n" +
            "\taload_0\n" +
            "\tinvokenonvirtual java/lang/Object/<init>()V\n" +
            "\treturn\n" +
            ".end method\n" +
            "\n" +
            ";\n" +
            "; main() method\n" +
            ";\n" +
            ".method public static main([Ljava/lang/String;)V\n" +
            ".limit locals 100\n" +
            ".limit stack 100\n";


    private String endProg = "\n\nreturn\n\n.end method";

    public void compile(String[] args) {
        String compileString = "";
        String name = "";
        if (args.length == 0) {
            compileString = defaultCode;
            name = "DefaultCode";
        } else {
            File f = new File(args[0]);
            if (!f.exists()) {
                System.out.println("File does not exist");
                return;
            }
            try {
                compileString = new String(Files.readAllBytes(Paths.get(args[0])));
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            name = f.getName();
            if (name.lastIndexOf(".") > -1)
                name = name.substring(0, name.lastIndexOf("."));
        }

        // Create lexer and run scanner to create stream of tokens
        CharStream charStream = CharStreams.fromString(compileString);
        CompilerLexer lexer = new CompilerLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Create parser and feed it the tokens
        CompilerParser parser = new CompilerParser(tokens);
        ParseTree program = parser.program();

        TypeVisitor typeVisitor = new TypeVisitor();
        typeVisitor.visit(program);

        CompVisitor visitor = new CompVisitor(name, typeVisitor.getTypes());
        ArrayList<String> prog = visitor.visit(program);

        System.out.println("\n\t\t## CODE START ##\n");
        System.out.println(compileString);
        System.out.println("\n\t\t## CODE END ##\n");

        String outputString = startProg.replaceAll("\\{\\{name\\}\\}", name) + prog.stream().collect(Collectors.joining("\n")) + endProg;

        System.out.println("\n\t\t## OUTPUT START ##\n");
        System.out.println(outputString);
        System.out.println("\n\t\t## OUTPUT END ##\n");

        String fileName = name + ".j";

        try (PrintWriter out = new PrintWriter(name + ".j")) {
            out.println(outputString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Executor executor = new Executor();

        executor.executeJar("jasmin.jar", fileName);
        System.out.println(executor.getExecutionLog());
        executor.execute(name);
        System.out.println(executor.getExecutionLog());

    }


    public static void main(String[] args) {
        Compiler compiler = new Compiler();
        compiler.compile(args);

    }

}
