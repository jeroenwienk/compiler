package nl.jwienk.compiler;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.ArrayList;

/**
 * Created by abe23 on 22/03/18.
 */
public class CompVisitor extends CompilerBaseVisitor<ArrayList<String>> {

    private String name;
    private SymbolTable symbolTable;
    private ParseTreeProperty<Type> types;
    private int storeIndex = 1;

    public CompVisitor(String name, ParseTreeProperty<Type> types) {
        this.name = name;
        this.types = types;
        this.symbolTable = new SymbolTable();
    }

    @Override
    public ArrayList<String> visitProgram(CompilerParser.ProgramContext ctx) {
        System.out.println("# VISITING Program");

        // Open a scope for the root
        this.symbolTable.openScope();

        ArrayList<String> code = new ArrayList<>();

        for (CompilerParser.StatementContext statement : ctx.statement()) {
            code.addAll(visit(statement));
        }

        this.symbolTable.closeScope();

        return code;
    }



    @Override
    public ArrayList<String> visitBlockStatement(CompilerParser.BlockStatementContext ctx) {
        System.out.println("# VISITING BlockStatement");

        this.symbolTable.openScope();

        ArrayList<String> code = new ArrayList<>();

        for (CompilerParser.StatementContext statement : ctx.block().statementList().statement()) {
            code.addAll(visit(statement));
        }

        this.symbolTable.closeScope();

        return code;
    }

    @Override
    public ArrayList<String> visitVariableStatement(CompilerParser.VariableStatementContext ctx) {
        System.out.println("# VISITING VariableStatement");

        ArrayList<String> code = new ArrayList<>();
        Type type = types.get(ctx);

        int storeAddress = this.storeIndex;

        String identifier = ctx.variableStat().IDENTIFIER().getText();
        Symbol oldSymbol = symbolTable.retrieve(identifier);

        if (oldSymbol != null) {
            storeAddress = oldSymbol.getAddress();
        } else {
            this.storeIndex++;
        }

        String mnemonic = Helper.getTypeMnemonic(type);

        switch( type ) {
            case INT:
                code.addAll(visit(ctx.variableStat().expression()));
                code.add(mnemonic + "store " + storeAddress);
                break;
            case DOUBLE:
                code.addAll(visit(ctx.variableStat().expression()));
                code.add(mnemonic + "store " + storeAddress);
                break;
            case BOOLEAN:
                code.addAll(visit(ctx.variableStat().expression()));
                code.add(mnemonic + "store " + storeAddress);
                break;
            case STRING:
                break;
            case IDENTIFIER:
                break;
            case METHOD:
                break;
        }

        // a variable is being assigned so we have to save the type of this value for when it gets referenced
        Symbol symbol = new Symbol(ctx, identifier, type);
        symbolTable.enter(identifier, symbol);
        symbol.setAddress(storeAddress);

        return code;
    }



    @Override
    public ArrayList<String> visitPrintStatement(CompilerParser.PrintStatementContext ctx) {
        System.out.println("# VISITING PrintStatement");
        ArrayList<String> code = new ArrayList<>();

        Type expressionType = types.get(ctx.printStat().expression());

        code.add("getstatic java/lang/System/out Ljava/io/PrintStream;");
        code.addAll(visit(ctx.printStat().expression()));
        code.add("invokevirtual java/io/PrintStream/println(" + Helper.getTypeDescriptor(expressionType) + ")V");

        return code;
    }

    @Override
    public ArrayList<String> visitIfStatement(CompilerParser.IfStatementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public ArrayList<String> visitParenthesesExpression(CompilerParser.ParenthesesExpressionContext ctx) {
        ArrayList<String> code = new ArrayList<>();
        code.addAll(visit(ctx.expression()));
        return code;
    }

    @Override
    public ArrayList<String> visitNegateExpression(CompilerParser.NegateExpressionContext ctx) {
        ArrayList<String> code = new ArrayList<>();
        Type type = types.get(ctx);

        code.addAll(visit(ctx.expression()));
        code.add(Helper.getTypeMnemonic(type) + "neg");

        return code;
    }

    @Override
    public ArrayList<String> visitAddSubExpression(CompilerParser.AddSubExpressionContext ctx) {
        System.out.println("# VISITING AddSubExpression");
        ArrayList<String> code = new ArrayList<>();

        Type returnType = types.get(ctx);
        Type leftType = types.get(ctx.left);
        Type rightType = types.get(ctx.right);

        code.addAll(visit(ctx.left));

        String mnemonic = Helper.getTypeMnemonic(returnType);

        if (leftType != returnType) {
            code.add(Helper.getTypeMnemonic(leftType) + "2" + mnemonic);
        }

        code.addAll(visit(ctx.right));

        if (rightType != returnType) {
            code.add(Helper.getTypeMnemonic(rightType) + "2" + mnemonic);
        }

        code.add(mnemonic + Helper.getOperatorAsWord(ctx.op.getText()));
        return code;
    }

    @Override
    public ArrayList<String> visitMulDivExpression(CompilerParser.MulDivExpressionContext ctx) {
        System.out.println("# VISITING MulDivExpression");
        ArrayList<String> code = new ArrayList<>();

        Type returnType = types.get(ctx);
        Type leftType = types.get(ctx.left);
        Type rightType = types.get(ctx.right);

        code.addAll(visit(ctx.left));

        System.out.println(returnType);

        String mnemonic = Helper.getTypeMnemonic(returnType);

        if (leftType != returnType) {
            code.add(Helper.getTypeMnemonic(leftType) + "2" + mnemonic);
        }

        code.addAll(visit(ctx.right));

        if (rightType != returnType) {
            code.add(Helper.getTypeMnemonic(rightType) + "2" + mnemonic);
        }

        code.add(mnemonic + Helper.getOperatorAsWord(ctx.op.getText()));
        return code;
    }

    @Override
    public ArrayList<String> visitBooleanExpression(CompilerParser.BooleanExpressionContext ctx) {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<String> visitIntConstExpression(CompilerParser.IntConstExpressionContext ctx) {
        System.out.println("# VISITING IntConstExpression");
        ArrayList<String> code = new ArrayList<>();
        code.add("ldc " + ctx.getText());
        return code;
    }

    @Override
    public ArrayList<String> visitDoubleConstExpression(CompilerParser.DoubleConstExpressionContext ctx) {
        System.out.println("# VISITING DoubleConstExpression");
        ArrayList<String> code = new ArrayList<>();
        code.add("ldc2_w " + ctx.getText());
        return code;
    }

    @Override
    public ArrayList<String> visitBooleanConstExpression(CompilerParser.BooleanConstExpressionContext ctx) {
        System.out.println("# VISITING BooleanConstExpression");
        ArrayList<String> code = new ArrayList<>();
        code.add("iconst_" + Helper.getBooleanValue(ctx.getText()));
        return code;
    }

    @Override
    public ArrayList<String> visitVariableConstExpression(CompilerParser.VariableConstExpressionContext ctx) {
        System.out.println("# VISITING VariableConstExpression");
        ArrayList<String> code = new ArrayList<>();
        Symbol symbol = symbolTable.retrieve(ctx.getText());
        if (symbol != null) {
            code.add(Helper.getTypeMnemonic(symbol.getType()) + "load " + symbol.getAddress());
        }

        return code;
    }

    @Override
    public ArrayList<String> visitVariableStat(CompilerParser.VariableStatContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public ArrayList<String> visitIfStat(CompilerParser.IfStatContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public ArrayList<String> visitPrintStat(CompilerParser.PrintStatContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public ArrayList<String> visitStatementList(CompilerParser.StatementListContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public ArrayList<String> visitBlock(CompilerParser.BlockContext ctx) {
        return visitChildren(ctx);
    }
}
