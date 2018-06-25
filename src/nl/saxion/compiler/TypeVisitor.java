package nl.saxion.compiler;

import org.antlr.v4.runtime.tree.ParseTreeProperty;



public class TypeVisitor extends CalculatorBaseVisitor<DataType> {


    private ParseTreeProperty<DataType> types;
    private SymbolTable symbolTable;

    public TypeVisitor() {
        this.types = new ParseTreeProperty<>();
        this.symbolTable = new SymbolTable();
    }

    public ParseTreeProperty<DataType> getTypes() {
        return types;
    }

    @Override
    public DataType visitProgram(CalculatorParser.ProgramContext ctx) {

        this.symbolTable.openScope();

        for (CalculatorParser.StatementContext statement : ctx.statement()) {
            visit(statement);
        }

        this.symbolTable.closeScope();

        return null;
    }

    @Override
    public DataType visitAssignmentStatement(CalculatorParser.AssignmentStatementContext ctx) {
        DataType expressionType = visit(ctx.expression());

        // a value is being assigned so we have to save the type of this value
        symbolTable.enter(ctx.ID().getText(), new Symbol(ctx.ID().getText(), expressionType));
        types.put(ctx, expressionType);
        return expressionType;
    }

    @Override
    public DataType visitPrintStatement(CalculatorParser.PrintStatementContext ctx) {
        DataType type = visit(ctx.expression());
        this.types.put(ctx, type);
        return type;
    }

    @Override
    public DataType visitAddSubExpression(CalculatorParser.AddSubExpressionContext ctx) {
        DataType leftType = visit(ctx.left);
        DataType rightType = visit(ctx.right);

        if (leftType.getType() != rightType.getType()) {
            throw new CompilerException(ctx, "Incompatible types");
        }

        this.types.put(ctx.left, leftType);
        this.types.put(ctx.right, rightType);

        return leftType;
    }

    @Override
    public DataType visitVarExpression(CalculatorParser.VarExpressionContext ctx) {
        Symbol symbol = symbolTable.retrieve(ctx.getText());

        if (symbol != null) {
            // this symbol was already defined so we can get the type
            this.types.put(ctx, symbol.type);
            return symbol.type;
        }


        // this has to be a reference to something undefined
        throw new CompilerException(ctx, ctx.getText() + " is not defined");
    }

    @Override
    public DataType visitMulDivExpression(CalculatorParser.MulDivExpressionContext ctx) {
        DataType leftType = visit(ctx.left);
        DataType rightType = visit(ctx.right);

        if (leftType.getType() != rightType.getType()) {
            throw new CompilerException(ctx, "Incompatible types");
        }

        this.types.put(ctx.left, leftType);
        this.types.put(ctx.right, rightType);
        return leftType;
    }

    @Override
    public DataType visitConstExpression(CalculatorParser.ConstExpressionContext ctx) {
        DataType type = new DataType(DataType.INT);
        this.types.put(ctx, type);
        return type;
    }


}
