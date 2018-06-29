package nl.jwienk.compiler;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.ArrayList;

@SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
public class CompVisitor extends CompilerBaseVisitor<ArrayList<String>> {

    private String name;
    private SymbolTable symbolTable;
    private ParseTreeProperty<Type> types;
    private int storeIndex = 1;
    private int labelCount = 0;
    private int locals = 1;
    private int stack = 0;
    private int finalStack = 0;

    public CompVisitor(String name, ParseTreeProperty<Type> types) {
        this.name = name;
        this.types = types;
        this.symbolTable = new SymbolTable();
    }

    public void increaseStack(int size) {
        this.stack += size;
    }

    public void resetStack() {
        if (this.stack > this.finalStack) {
            this.finalStack = this.stack;
        }
        this.stack = 0;
    }

    @Override
    public ArrayList<String> visitProgram(CompilerParser.ProgramContext ctx) {
        System.out.println("# VISITING Program");

        // Open a scope for the root
        this.symbolTable.openScope();

        ArrayList<String> code = new ArrayList<>();

        // if we do visitChildren(ctx) the order gets messed up
        for (CompilerParser.StatementContext statement : ctx.statement()) {
            code.addAll(visit(statement));
        }

        this.symbolTable.closeScope();


        code.add(0, ".limit stack " + this.finalStack);
        code.add(0, ".limit locals " + this.locals);


        return code;
    }


    @Override
    public ArrayList<String> visitBlockStatement(CompilerParser.BlockStatementContext ctx) {
        System.out.println("# VISITING BlockStatement");

        this.symbolTable.openScope();

        ArrayList<String> code = new ArrayList<>();

        if (ctx.statementList() != null) {

            for (CompilerParser.StatementContext statement : ctx.statementList().statement()) {
                code.addAll(visit(statement));
            }
        }

        this.symbolTable.closeScope();

        System.out.println("STACK: " + this.stack);
        System.out.println("LOCALS: " + this.locals);

        return code;
    }

    @Override
    public ArrayList<String> visitVariableStatement(CompilerParser.VariableStatementContext ctx) {
        System.out.println("# VISITING VariableStatement");

        ArrayList<String> code = new ArrayList<>();
        Type type = types.get(ctx);

        int storeAddress = this.storeIndex;

        String identifier = ctx.IDENTIFIER().getText();
        Symbol oldSymbol = symbolTable.retrieve(identifier);

        if (oldSymbol != null && oldSymbol.getType() == type) {
            storeAddress = oldSymbol.getAddress();
        } else {


            if (type == Type.DOUBLE) {
                this.storeIndex++;
                this.storeIndex++;
                this.locals++;
                this.locals++;
            } else {
                this.storeIndex++;   
                this.locals++;
            }
            
        }

        String mnemonic = Helper.getTypeMnemonic(type);

        code.addAll(visit(ctx.expression()));
        code.add(mnemonic + "store " + storeAddress);

        // a variable is being assigned so we have to save the type of this value for when it gets referenced
        Symbol symbol = new Symbol(ctx, identifier, type);
        symbolTable.enter(identifier, symbol);
        symbol.setAddress(storeAddress);


        System.out.println(ctx.children.size());

        this.resetStack();
        return code;
    }


    @Override
    public ArrayList<String> visitPrintStatement(CompilerParser.PrintStatementContext ctx) {
        System.out.println("# VISITING PrintStatement");
        ArrayList<String> code = new ArrayList<>();

        Type expressionType = types.get(ctx.expression());

        code.add("getstatic java/lang/System/out Ljava/io/PrintStream;");
        code.addAll(visit(ctx.expression()));
        code.add("invokevirtual java/io/PrintStream/println(" + Helper.getTypeDescriptor(expressionType) + ")V");

        this.resetStack();
        return code;
    }

    @Override
    public ArrayList<String> visitParenthesesExpression(CompilerParser.ParenthesesExpressionContext ctx) {
        System.out.println("# VISITING ParenthesesExpression");
        ArrayList<String> code = new ArrayList<>();
        code.addAll(visit(ctx.expression()));
        return code;
    }

    @Override
    public ArrayList<String> visitNegateExpression(CompilerParser.NegateExpressionContext ctx) {
        System.out.println("# VISITING NegateExpression");
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

        this.resetStack();
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

        String mnemonic = Helper.getTypeMnemonic(returnType);

        if (leftType != returnType) {
            code.add(Helper.getTypeMnemonic(leftType) + "2" + mnemonic);
        }

        code.addAll(visit(ctx.right));

        if (rightType != returnType) {
            code.add(Helper.getTypeMnemonic(rightType) + "2" + mnemonic);
        }

        code.add(mnemonic + Helper.getOperatorAsWord(ctx.op.getText()));
        this.resetStack();
        return code;
    }

    @Override
    public ArrayList<String> visitIfStatement(CompilerParser.IfStatementContext ctx) {
        System.out.println("# VISITING IfStatement");

        int localLabelCount = ++labelCount;

        ArrayList<String> code = new ArrayList<>();

        // first add the contents of the expression: if (expression)
        code.addAll(visit(ctx.expression()));

        String endLabel = "end_if_" + localLabelCount;
        String thenLabel = "then_if_" + localLabelCount;
        String elseLabel = "else_if_" + localLabelCount;

        code.add("ifne " + thenLabel);

        code.add(elseLabel + ":");
        if (ctx.ELSE() != null && ctx.statement(1) != null) {
            code.addAll(visit(ctx.statement(1)));
        }
        code.add("goto " + endLabel);

        code.add(thenLabel + ":");
        code.addAll(visit(ctx.statement(0)));
        code.add(endLabel + ":"); // Jump to here when condition == 0
        return code;
    }

    @Override
    public ArrayList<String> visitComparisonExpression(CompilerParser.ComparisonExpressionContext ctx) {
        System.out.println("# VISITING ComparisonExpression");

        ArrayList<String> code = new ArrayList<>();

        int localLabelCount = ++labelCount;

        Type leftType = types.get(ctx.left);
        Type rightType = types.get(ctx.right);
        Type returnType = Type.getReturnType(leftType, rightType);

        String operator = Helper.getOperatorAsWord(ctx.op.getText());
        String mnemonic = Helper.getTypeMnemonic(returnType);

        code.addAll(visit(ctx.left));

        if (leftType != returnType) {
            code.add(Helper.getTypeMnemonic(leftType) + "2" + mnemonic);
        }

        code.addAll(visit(ctx.right));

        if (rightType != returnType) {
            code.add(Helper.getTypeMnemonic(rightType) + "2" + mnemonic);
        }

        // for comparing doubles some extra work is needed
        if (returnType == Type.DOUBLE) {
            code.add("dcmpg");
            code.add("iconst_0");
            
        }

        code.add("if_icmp" + operator + " then_c_" + localLabelCount);
        code.add("else_c_" + localLabelCount + ":");
        code.add("iconst_0");
        code.add("goto end_c_" + localLabelCount);
        code.add("then_c_" + localLabelCount + ":");
        code.add("iconst_1");
        code.add("end_c_" + localLabelCount + ":");

        return code;
    }

    @Override
    public ArrayList<String> visitLogicalExpression(CompilerParser.LogicalExpressionContext ctx) {
        System.out.println("# VISITING LogicalExpression");
        ArrayList<String> code = new ArrayList<>();
        String operator = Helper.getOperatorAsWord(ctx.op.getText());
        code.addAll(visit(ctx.left));
        code.addAll(visit(ctx.right));

        // needs to be 5 because its not sure if doubles are used
        //this.increaseStack(5);

        // add the bitwise operator
        code.add(operator);
        return code;
    }

    @Override
    public ArrayList<String> visitNotExpression(CompilerParser.NotExpressionContext ctx) {
        System.out.println("# VISITING NotExpression");
        ArrayList<String> code = new ArrayList<>();
        // add the code for the children
        code.addAll(visitChildren(ctx));
        // flip the result of this code
        // there has to be some easier bitwise flip solution
        code.add("iconst_1");
        code.add("iadd");
        code.add("iconst_2");
        code.add("irem");
        return code;
    }

    @Override
    public ArrayList<String> visitWhileStatement(CompilerParser.WhileStatementContext ctx) {
        System.out.println("# VISITING WhileStatement");
        ArrayList<String> code = new ArrayList<>();


        int localLabelCount = ++labelCount;

        String beforeLabel = "before_w_" + localLabelCount;
        String thenLabel = "then_w_" + localLabelCount;
        String endLabel = "end_w_" + localLabelCount;

        code.add(beforeLabel + ":");
        code.addAll(visit(ctx.expression()));

        code.add("ifne " + thenLabel);
        code.add("goto " + endLabel);
        code.add(thenLabel + ":");
        code.addAll(visit(ctx.statement()));
        code.add("goto " + beforeLabel);
        code.add(endLabel + ":");

        return code;
    }

    @Override
    public ArrayList<String> visitForStatement(CompilerParser.ForStatementContext ctx) {
        System.out.println("# VISITING ForStatement");
        return new ArrayList<>();
    }

    @Override
    public ArrayList<String> visitIntConstExpression(CompilerParser.IntConstExpressionContext ctx) {
        System.out.println("# VISITING IntConstExpression");
        ArrayList<String> code = new ArrayList<>();
        code.add("ldc " + ctx.getText());
        this.increaseStack(1);
        return code;
    }

    @Override
    public ArrayList<String> visitDoubleConstExpression(CompilerParser.DoubleConstExpressionContext ctx) {
        System.out.println("# VISITING DoubleConstExpression");
        ArrayList<String> code = new ArrayList<>();
        code.add("ldc2_w " + ctx.getText());
        this.increaseStack(2);
        return code;
    }

    @Override
    public ArrayList<String> visitBooleanConstExpression(CompilerParser.BooleanConstExpressionContext ctx) {
        System.out.println("# VISITING BooleanConstExpression");
        ArrayList<String> code = new ArrayList<>();
        code.add("iconst_" + Helper.getBooleanValue(ctx.getText()));
        this.increaseStack(1);
        return code;
    }

    @Override
    public ArrayList<String> visitVariableConstExpression(CompilerParser.VariableConstExpressionContext ctx) {
        System.out.println("# VISITING VariableConstExpression");
        ArrayList<String> code = new ArrayList<>();
        Symbol symbol = symbolTable.retrieve(ctx.getText());
        if (symbol != null) {
            code.add(Helper.getTypeMnemonic(symbol.getType()) + "load " + symbol.getAddress());
            this.increaseStack(symbol.getType() == Type.DOUBLE ? 2 : 1);
        }

        return code;
    }

    @Override
    public ArrayList<String> visitStatement(CompilerParser.StatementContext ctx) {
        ArrayList<String> code = new ArrayList<>();

        code.addAll(visitChildren(ctx));

        return code;
    }

    @Override
    public ArrayList<String> visitStatementList(CompilerParser.StatementListContext ctx) {
        ArrayList<String> code = new ArrayList<>();

        code.addAll(visitChildren(ctx));

        return code;
    }
}
