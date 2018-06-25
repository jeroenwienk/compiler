package nl.saxion.calculator;

//import nl.saxion.calclang.antlr4.calculatorLexer;
//import nl.saxion.calclang.cantlr4.calculatorParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by abe23 on 22/03/18.
 */
public class Calculator {

    //used for testing without a file
    private String defaultCode  = "a=5 ;  b = 4 ; a = 34 + 34 + b / 4 ;\n"+
            "print a;" +
            " print 34 + 34 - 8 /4;";

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
            ".method public static main([Ljava/lang/String;)V\n";


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
        CalculatorLexer lexer = new CalculatorLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Create parser and feed it the tokens
        CalculatorParser parser = new CalculatorParser(tokens);
        ParseTree program = parser.program();

        TypeVisitor typeVisitor = new TypeVisitor();
        typeVisitor.visit(program);

        CalcVisitor visitor = new CalcVisitor(name, typeVisitor.getTypes());
        ArrayList<String> prog  = visitor.visit(program);


        System.out.println("## DEFAULTCODE ##");
        System.out.println(defaultCode);
        System.out.println("");

        // Output fixed part of the Jasmin file (except for the name)
        System.out.println(startProg.replaceAll("\\{\\{name\\}\\}",name));
        // Output compiled part of the jasmin file
        System.out.println(prog.stream().collect(Collectors.joining("\n")));
        // Output footer of jasmin file
        System.out.println(endProg);
    }


    public static void main(String[] args) {
        Calculator calc = new Calculator();
        calc.compile(args);

    }

}
