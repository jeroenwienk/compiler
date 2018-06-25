package nl.saxion.calculator;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.ArrayList;

/**
 * Created by abe23 on 22/03/18.
 */
public class CalcVisitor extends CalculatorBaseVisitor<ArrayList<String>> {

    private String name;
    private SymbolTable symbolTable;
    private ParseTreeProperty<DataType> types;

    public CalcVisitor(String name, ParseTreeProperty<DataType> types) {
        this.name = name;
        this.types = types;
        this.symbolTable = new SymbolTable();
    }

    @Override
    public ArrayList<String> visitProgram(CalculatorParser.ProgramContext ctx) {

        ArrayList<String> code = new ArrayList<>();

        for (CalculatorParser.StatementContext statement : ctx.statement()) {
            code.addAll(visit(statement));
        }

        return code;
    }

    @Override
    public ArrayList<String> visitAssignmentStatement(CalculatorParser.AssignmentStatementContext ctx) {
        ArrayList<String> code = new ArrayList<>();

        visit(ctx.expression());

        DataType type = types.get(ctx);
        System.out.println(type.getType());

        return code;
    }

    @Override
    public ArrayList<String> visitPrintStatement(CalculatorParser.PrintStatementContext ctx) {
        ArrayList<String> code = new ArrayList<>();

        visit(ctx.expression());

        DataType type = types.get(ctx);
        System.out.println(type.getType());

        return code;
    }

    @Override
    public ArrayList<String> visitMulDivExpression(CalculatorParser.MulDivExpressionContext ctx) {
        ArrayList<String> code = new ArrayList<>();

        visit(ctx.right);
        visit(ctx.left);

        DataType type1 = types.get(ctx.left);
        DataType type2 = types.get(ctx.right);
        System.out.println(type1.getType());
        System.out.println(type2.getType());

        return code;
    }

    @Override
    public ArrayList<String> visitAddSubExpression(CalculatorParser.AddSubExpressionContext ctx) {
        ArrayList<String> code = new ArrayList<>();

        visit(ctx.right);
        visit(ctx.left);

        DataType type1 = types.get(ctx.left);
        DataType type2 = types.get(ctx.right);
        System.out.println(type1.getType());
        System.out.println(type2.getType());

        return code;
    }

    @Override
    public ArrayList<String> visitVarExpression(CalculatorParser.VarExpressionContext ctx) {
        ArrayList<String> code = new ArrayList<>();


        DataType type = types.get(ctx);
        System.out.println(type.getType());

        return code;
    }

    @Override
    public ArrayList<String> visitConstExpression(CalculatorParser.ConstExpressionContext ctx) {
        ArrayList<String> code = new ArrayList<>();


        DataType type = types.get(ctx);
        System.out.println(type.getType());

        return code;
    }

}
