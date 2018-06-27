package nl.jwienk.compiler;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class TypeVisitor extends CompilerBaseVisitor<Type> {

    private ParseTreeProperty<Type> types;
    private SymbolTable symbolTable;

    public TypeVisitor() {
        this.types = new ParseTreeProperty<>();
        this.symbolTable = new SymbolTable();
    }

    public ParseTreeProperty<Type> getTypes() {
        return types;
    }

    @Override
    public Type visitProgram(CompilerParser.ProgramContext ctx) {
        //System.out.println("# VISITING Program");
        // Open a scope for the root
        this.symbolTable.openScope();
        visitChildren(ctx);
        this.symbolTable.closeScope();

        return null;
    }

    @Override
    public Type visitBlockStatement(CompilerParser.BlockStatementContext ctx) {
        //System.out.println("# VISITING Block");

        this.symbolTable.openScope();
        visitChildren(ctx);
        this.symbolTable.closeScope();

        return null;
    }

    @Override
    public Type visitVariableStatement(CompilerParser.VariableStatementContext ctx) {
        //System.out.println("# VISITING VariableStatement");
        Type type = visit(ctx.variableStat().expression());


        // a variable is being assigned so we have to save the type of this value for when it gets referenced
        symbolTable.enter(ctx.variableStat().IDENTIFIER().getText(), new Symbol(ctx, ctx.variableStat().IDENTIFIER().getText(), type));
        types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitPrintStatement(CompilerParser.PrintStatementContext ctx) {
        //System.out.println("# VISITING PrintStatement");
        Type expressionType = visit(ctx.printStat().expression());

        this.types.put(ctx, Type.METHOD);
        return Type.METHOD;
    }

    @Override
    public Type visitIfStatement(CompilerParser.IfStatementContext ctx) {
        //System.out.println("# VISITING IfStatement");
        Type expressionType = visit(ctx.ifStat().expression());

        if (expressionType != Type.BOOLEAN) {
            throw new CompilerException(ctx, "Expression should evaluate to a boolean value");
        }

        visitChildren(ctx);

        return null;
    }

    @Override
    public Type visitParenthesesExpression(CompilerParser.ParenthesesExpressionContext ctx) {
        Type expressionType = visit(ctx.expression());
        this.types.put(ctx, expressionType);
        return expressionType;
    }

    @Override
    public Type visitNegateExpression(CompilerParser.NegateExpressionContext ctx) {
        Type expressionType = visit(ctx.expression());
        this.types.put(ctx, expressionType);
        return expressionType;
    }

    @Override
    public Type visitAddSubExpression(CompilerParser.AddSubExpressionContext ctx) {
        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);

        if (!TypeChecker.areCompatible(leftType, rightType)) {
            throw new CompilerException(ctx, "Incompatible types: " + leftType + " " + rightType);
        }

        Type returnType = TypeChecker.getReturnType(leftType, rightType);

        this.types.put(ctx.left, leftType);
        this.types.put(ctx.right, rightType);
        this.types.put(ctx, returnType);

        return TypeChecker.getReturnType(leftType, rightType);
    }

    @Override
    public Type visitMulDivExpression(CompilerParser.MulDivExpressionContext ctx) {
        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);

        if (!TypeChecker.areCompatible(leftType, rightType)) {
            throw new CompilerException(ctx, "Incompatible types: " + leftType + " " + rightType);
        }

        Type returnType = TypeChecker.getReturnType(leftType, rightType);

        this.types.put(ctx.left, leftType);
        this.types.put(ctx.right, rightType);
        this.types.put(ctx, returnType);

        return TypeChecker.getReturnType(leftType, rightType);
    }

    @Override
    public Type visitBooleanExpression(CompilerParser.BooleanExpressionContext ctx) {
        Type leftExpressionType = visit(ctx.left);
        Type rightExpressionType = visit(ctx.right);
        String operator = ctx.op.getText();

        Type type = Type.BOOLEAN;
        this.types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitIntConstExpression(CompilerParser.IntConstExpressionContext ctx) {
        Type type = Type.INT;
        this.types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitDoubleConstExpression(CompilerParser.DoubleConstExpressionContext ctx) {
        Type type = Type.DOUBLE;
        this.types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitBooleanConstExpression(CompilerParser.BooleanConstExpressionContext ctx) {
        Type type = Type.BOOLEAN;
        this.types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitVariableConstExpression(CompilerParser.VariableConstExpressionContext ctx) {
        Symbol symbol = symbolTable.retrieve(ctx.getText());

        if (symbol != null) {
            // this symbol was already defined so we can get the type
            this.types.put(ctx, symbol.getType());
            return symbol.getType();
        }


        // this has to be a reference to something undefined
        throw new CompilerException(ctx, ctx.getText() + " is not defined");
    }

    @Override
    public Type visitVariableStat(CompilerParser.VariableStatContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Type visitIfStat(CompilerParser.IfStatContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Type visitPrintStat(CompilerParser.PrintStatContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Type visitStatementList(CompilerParser.StatementListContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Type visitBlock(CompilerParser.BlockContext ctx) {
        visitChildren(ctx);
        return null;
    }
}
