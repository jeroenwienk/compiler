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
        Type type = visit(ctx.expression());
        String identifier = ctx.IDENTIFIER().getText();


        // a variable is being assigned so we have to save the type of this value for when it gets referenced
        symbolTable.enter(identifier, new Symbol(ctx, identifier, type));
        types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitPrintStatement(CompilerParser.PrintStatementContext ctx) {
        //System.out.println("# VISITING PrintStatement");

        this.types.put(ctx, Type.METHOD);
        visit(ctx.expression());
        return Type.METHOD;
    }

    @Override
    public Type visitIfStatement(CompilerParser.IfStatementContext ctx) {
        //System.out.println("# VISITING IfStatement");

        Type expressionType = visit(ctx.expression());

        if (expressionType != Type.BOOLEAN) {
            throw new CompilerException(ctx, "Expression should evaluate to a boolean value");
        }

        visitChildren(ctx);

        return null;
    }

    @Override
    public Type visitParenthesesExpression(CompilerParser.ParenthesesExpressionContext ctx) {
        //System.out.println("# VISITING ParenthesesExpression");

        Type expressionType = visit(ctx.expression());
        this.types.put(ctx, expressionType);
        return expressionType;
    }

    @Override
    public Type visitNegateExpression(CompilerParser.NegateExpressionContext ctx) {
        //System.out.println("# VISITING NegateExpression");

        Type expressionType = visit(ctx.expression());
        this.types.put(ctx, expressionType);
        return expressionType;
    }

    @Override
    public Type visitAddSubExpression(CompilerParser.AddSubExpressionContext ctx) {
        //System.out.println("# VISITING AddSubExpression");

        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);

        if (!Type.areCompatible(leftType, rightType)) {
            throw new CompilerException(ctx, "Incompatible types: " + leftType + " " + rightType);
        }

        Type returnType = Type.getReturnType(leftType, rightType);

        this.types.put(ctx.left, leftType);
        this.types.put(ctx.right, rightType);
        this.types.put(ctx, returnType);

        return returnType;
    }

    @Override
    public Type visitMulDivExpression(CompilerParser.MulDivExpressionContext ctx) {
        //System.out.println("# VISITING MulDivExpression");

        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);

        if (!Type.areCompatible(leftType, rightType)) {
            throw new CompilerException(ctx, "Incompatible types: " + leftType + " " + rightType);
        }

        Type returnType = Type.getReturnType(leftType, rightType);

        this.types.put(ctx.left, leftType);
        this.types.put(ctx.right, rightType);
        this.types.put(ctx, returnType);

        return returnType;
    }

    @Override
    public Type visitComparisonExpression(CompilerParser.ComparisonExpressionContext ctx) {
        //System.out.println("# VISITING ComparisonExpression");

        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);

        if (!Type.areCompatible(leftType, rightType)) {
            throw new CompilerException(ctx, "Incompatible types: " + leftType + " " + rightType);
        }

        this.types.put(ctx.left, leftType);
        this.types.put(ctx.right, rightType);

        return Type.BOOLEAN;
    }

    @Override
    public Type visitLogicalExpression(CompilerParser.LogicalExpressionContext ctx) {
        //System.out.println("# VISITING LogicalExpression");

        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);

        if (!Type.areCompatible(leftType, rightType)) {
            throw new CompilerException(ctx, "Incompatible types: " + leftType + " " + rightType);
        }

        this.types.put(ctx.left, leftType);
        this.types.put(ctx.right, rightType);


        return Type.BOOLEAN;
    }

    @Override
    public Type visitNotExpression(CompilerParser.NotExpressionContext ctx) {
        //System.out.println("# VISITING NotExpression");

        Type type = Type.BOOLEAN;
        Type expressionType = visit(ctx.expression());

        if (expressionType != Type.BOOLEAN) {
            throw new CompilerException(ctx, "! operator can only be applied to boolean values");
        }

        this.types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitWhileStatement(CompilerParser.WhileStatementContext ctx) {
        //System.out.println("# VISITING WhileStatement");


        Type type = Type.STATEMENT;
        Type expressionType = visit(ctx.expression());

        if (expressionType != Type.BOOLEAN) {
            throw new CompilerException(ctx, "while expression should evaluate to boolean value");
        }

        visit(ctx.statement());


        this.types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitForStatement(CompilerParser.ForStatementContext ctx) {
        //System.out.println("# VISITING ForStatement");

        return null;
    }

    @Override
    public Type visitIntConstExpression(CompilerParser.IntConstExpressionContext ctx) {
        //System.out.println("# VISITING IntConstExpression");

        Type type = Type.INT;
        this.types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitDoubleConstExpression(CompilerParser.DoubleConstExpressionContext ctx) {
        //System.out.println("# VISITING DoubleConstExpression");

        Type type = Type.DOUBLE;
        this.types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitBooleanConstExpression(CompilerParser.BooleanConstExpressionContext ctx) {
        //System.out.println("# VISITING BooleanConstExpression");

        Type type = Type.BOOLEAN;
        this.types.put(ctx, type);
        return type;
    }

    @Override
    public Type visitVariableConstExpression(CompilerParser.VariableConstExpressionContext ctx) {
        //System.out.println("# VISITING VariableConstExpression");

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
    public Type visitStatement(CompilerParser.StatementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Type visitStatementList(CompilerParser.StatementListContext ctx) {
        return visitChildren(ctx);
    }
}
