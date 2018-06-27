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

    public CompVisitor(String name, ParseTreeProperty<Type> types) {
        this.name = name;
        this.types = types;
        this.symbolTable = new SymbolTable();
    }

    @Override
    public ArrayList<String> visitProgram(CompilerParser.ProgramContext ctx) {

        ArrayList<String> code = new ArrayList<>();

        for (CompilerParser.StatementContext statement : ctx.statement()) {
            //code.addAll(visit(statement));
        }

        return code;
    }

    @Override
    public ArrayList<String> visitBlockStatement(CompilerParser.BlockStatementContext ctx) {
        return super.visitBlockStatement(ctx);
    }

    @Override
    public ArrayList<String> visitVariableStatement(CompilerParser.VariableStatementContext ctx) {
        return super.visitVariableStatement(ctx);
    }

    @Override
    public ArrayList<String> visitIfStatement(CompilerParser.IfStatementContext ctx) {
        return super.visitIfStatement(ctx);
    }

    @Override
    public ArrayList<String> visitPrintStatement(CompilerParser.PrintStatementContext ctx) {
        return super.visitPrintStatement(ctx);
    }

    @Override
    public ArrayList<String> visitParenthesesExpression(CompilerParser.ParenthesesExpressionContext ctx) {
        return super.visitParenthesesExpression(ctx);
    }

    @Override
    public ArrayList<String> visitNegateExpression(CompilerParser.NegateExpressionContext ctx) {
        return super.visitNegateExpression(ctx);
    }

    @Override
    public ArrayList<String> visitDoubleConstExpression(CompilerParser.DoubleConstExpressionContext ctx) {
        return super.visitDoubleConstExpression(ctx);
    }

    @Override
    public ArrayList<String> visitAddSubExpression(CompilerParser.AddSubExpressionContext ctx) {
        return super.visitAddSubExpression(ctx);
    }

    @Override
    public ArrayList<String> visitBooleanConstExpression(CompilerParser.BooleanConstExpressionContext ctx) {
        return super.visitBooleanConstExpression(ctx);
    }

    @Override
    public ArrayList<String> visitBooleanExpression(CompilerParser.BooleanExpressionContext ctx) {
        return super.visitBooleanExpression(ctx);
    }

    @Override
    public ArrayList<String> visitIntConstExpression(CompilerParser.IntConstExpressionContext ctx) {
        return super.visitIntConstExpression(ctx);
    }

    @Override
    public ArrayList<String> visitVariableConstExpression(CompilerParser.VariableConstExpressionContext ctx) {
        return super.visitVariableConstExpression(ctx);
    }

    @Override
    public ArrayList<String> visitMulDivExpression(CompilerParser.MulDivExpressionContext ctx) {
        return super.visitMulDivExpression(ctx);
    }

}
