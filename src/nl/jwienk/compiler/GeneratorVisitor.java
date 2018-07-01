package nl.jwienk.compiler;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.ArrayList;


public class GeneratorVisitor extends CompilerBaseVisitor<ArrayList<String>> {

    private String name;
    private SymbolTable symbolTable;
    private ParseTreeProperty<Type> types;
    private int storeIndex = 1; // start at 1 because 0 is the first main argument
    private int labelCount = 0;
    private int locals = 1; // we already have 1 for the main argument
    private int stack = 0;
    private int finalStack = 0;

    public GeneratorVisitor(String name, ParseTreeProperty<Type> types) {
        this.name = name;
        this.types = types;
        this.symbolTable = new SymbolTable();
    }

    public void increaseStack(int size) {
        this.stack += size;
    }

    /**
     * only replace the stackValue if it is higher then the last stackValue
     */
    public void resetStack() {
        if (this.stack > this.finalStack) {
            this.finalStack = this.stack;
        }
        this.stack = 0;
    }

    @Override
    public ArrayList<String> visitProgram(CompilerParser.ProgramContext ctx) {
        //System.out.println("# VISITING Program");

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

    /**
     * The block statement has its own scope
     * e.g { someCode }
     *
     * @param ctx BlockStatementContext
     * @return code
     */
    @Override
    public ArrayList<String> visitBlockStatement(CompilerParser.BlockStatementContext ctx) {
        //System.out.println("# VISITING BlockStatement");

        this.symbolTable.openScope();

        ArrayList<String> code = new ArrayList<>();

        // dont try to visit if there is no code inside
        if (ctx.statementList() != null) {

            for (CompilerParser.StatementContext statement : ctx.statementList().statement()) {
                code.addAll(visit(statement));
            }
        }

        this.symbolTable.closeScope();

        return code;
    }

    /**
     * e.g var a [assignmentStatement]
     *
     * @param ctx VariableStatementContext
     * @return code
     */
    @Override
    public ArrayList<String> visitVariableStatement(CompilerParser.VariableStatementContext ctx) {
        //System.out.println("# VISITING VariableStatement");

        ArrayList<String> code = new ArrayList<>();
        Type type = types.get(ctx);

        int storeAddress = this.storeIndex;

        String identifier = ctx.assignmentStatement().IDENTIFIER().getText();

        // a double takes up 2 spaces
        if (type == Type.DOUBLE) {
            this.storeIndex += 2;
            this.locals += 2;
        } else {
            this.storeIndex++;
            this.locals++;
        }

        // a variable is being assigned so we have to save the type of this value for when it gets referenced
        Symbol symbol = new Symbol(ctx, identifier, type);
        this.symbolTable.enter(identifier, symbol);
        symbol.setAddress(storeAddress);

        // visit the assignment that represents te value
        code.addAll(visit(ctx.assignmentStatement()));

        this.resetStack();
        return code;
    }

    /**
     * e.g. a = 1 + 1;
     *
     * @param ctx AssignmentStatementContext
     * @return code
     */
    @Override
    public ArrayList<String> visitAssignmentStatement(CompilerParser.AssignmentStatementContext ctx) {
        //System.out.println("# VISITING AssignmentStatement");
        ArrayList<String> code = new ArrayList<>();

        Symbol symbol = this.symbolTable.retrieve(ctx.IDENTIFIER().getText());

        Type expressionType = types.get(ctx.expression());
        Type symbolType = symbol.getType();

        // the type can change when reassigned
        if (symbolType != expressionType) {

            symbol.setType(expressionType);
            symbol.setAddress(this.storeIndex);

            if (expressionType == Type.DOUBLE) {
                this.storeIndex++;
                this.storeIndex++;
                this.locals++;
                this.locals++;
            } else {
                this.storeIndex++;
                this.locals++;
            }

        }

        String mnemonic = Helper.getTypeMnemonic(symbol.getType());

        code.addAll(visit(ctx.expression()));
        code.add(mnemonic + "store " + symbol.getAddress());

        return code;
    }

    /**
     * e.g print(2 + 2);
     *
     * @param ctx PrintStatementContext
     * @return code
     */
    @Override
    public ArrayList<String> visitPrintStatement(CompilerParser.PrintStatementContext ctx) {
        //System.out.println("# VISITING PrintStatement");
        ArrayList<String> code = new ArrayList<>();

        code.add("getstatic java/lang/System/out Ljava/io/PrintStream;");
        code.addAll(visit(ctx.expression()));
        Type expressionType = types.get(ctx.expression());
        code.add("invokevirtual java/io/PrintStream/println(" + Helper.getTypeDescriptor(expressionType) + ")V");

        this.increaseStack(1);
        this.resetStack();
        return code;
    }

    /**
     * e.g (1 + 1)
     * It has the highest precedence of all expressions
     *
     * @param ctx ParenthesesExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitParenthesesExpression(CompilerParser.ParenthesesExpressionContext ctx) {
        //System.out.println("# VISITING ParenthesesExpression");
        ArrayList<String> code = new ArrayList<>();
        code.addAll(visit(ctx.expression()));
        return code;
    }

    /**
     * e.g. -1
     *
     * @param ctx NegateExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitNegateExpression(CompilerParser.NegateExpressionContext ctx) {
        //System.out.println("# VISITING NegateExpression");
        ArrayList<String> code = new ArrayList<>();
        Type type = types.get(ctx);

        code.addAll(visit(ctx.expression()));
        code.add(Helper.getTypeMnemonic(type) + "neg");

        return code;
    }

    /**
     * e.g 1 + 1
     *
     * @param ctx AddSubExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitAddSubExpression(CompilerParser.AddSubExpressionContext ctx) {
        //System.out.println("# VISITING AddSubExpression");
        ArrayList<String> code = new ArrayList<>();

        code.addAll(visit(ctx.left));
        // the type might have changed
        visit(ctx.right);


        Type leftType = types.get(ctx.left);
        Type rightType = types.get(ctx.right);

        Type returnType = Type.getReturnType(leftType, rightType);
        this.types.put(ctx, returnType);

        String mnemonic = Helper.getTypeMnemonic(returnType);

        // some casting might be needed if the types are not the same
        if (leftType != returnType) {
            code.add(Helper.getTypeMnemonic(leftType) + "2" + mnemonic);
            if (returnType == Type.DOUBLE) this.increaseStack(1);
        }

        code.addAll(visit(ctx.right));

        if (rightType != returnType) {
            code.add(Helper.getTypeMnemonic(rightType) + "2" + mnemonic);
            if (returnType == Type.DOUBLE) this.increaseStack(1);
        }

        code.add(mnemonic + Helper.getOperatorAsWord(ctx.op.getText()));

        this.resetStack();
        return code;
    }

    /**
     * e.g. 1 * 2
     *
     * @param ctx MulDivExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitMulDivExpression(CompilerParser.MulDivExpressionContext ctx) {
        //System.out.println("# VISITING MulDivExpression");
        ArrayList<String> code = new ArrayList<>();

        code.addAll(visit(ctx.left));
        // the type might have changed
        visit(ctx.right);

        Type leftType = types.get(ctx.left);
        Type rightType = types.get(ctx.right);

        Type returnType = Type.getReturnType(leftType, rightType);
        this.types.put(ctx, returnType);

        String mnemonic = Helper.getTypeMnemonic(returnType);

        // some casting might be needed if the types are not the same
        if (leftType != returnType) {
            code.add(Helper.getTypeMnemonic(leftType) + "2" + mnemonic);
            if (returnType == Type.DOUBLE) this.increaseStack(1);
        }

        code.addAll(visit(ctx.right));

        if (rightType != returnType) {
            code.add(Helper.getTypeMnemonic(rightType) + "2" + mnemonic);
            if (returnType == Type.DOUBLE) this.increaseStack(1);
        }

        code.add(mnemonic + Helper.getOperatorAsWord(ctx.op.getText()));
        this.resetStack();
        return code;
    }

    /**
     * if (2 == 1) { statements }
     *
     * @param ctx IfStatementContext
     * @return code
     */
    @Override
    public ArrayList<String> visitIfStatement(CompilerParser.IfStatementContext ctx) {
        //System.out.println("# VISITING IfStatement");

        int localLabelCount = ++labelCount;

        ArrayList<String> code = new ArrayList<>();

        // first add the contents of the expression: if (expression)
        code.addAll(visit(ctx.expression()));

        String endLabel = "end_if_" + localLabelCount;
        String thenLabel = "then_if_" + localLabelCount;
        String elseLabel = "else_if_" + localLabelCount;

        // the expression puts either 0 or 1 on the stack
        // compare it to 0
        code.add("ifne " + thenLabel);

        code.add(elseLabel + ":");
        // only add the else code if it actually exists
        if (ctx.ELSE() != null && ctx.statement(1) != null) {
            code.addAll(visit(ctx.statement(1)));
        }
        code.add("goto " + endLabel);

        code.add(thenLabel + ":");
        code.addAll(visit(ctx.statement(0)));
        code.add(endLabel + ":");
        return code;
    }

    /**
     * e.g 1 == 1
     *
     * @param ctx ComparisonExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitComparisonExpression(CompilerParser.ComparisonExpressionContext ctx) {
        //System.out.println("# VISITING ComparisonExpression");

        ArrayList<String> code = new ArrayList<>();

        int localLabelCount = ++labelCount;

        code.addAll(visit(ctx.left));
        // the type might have changed
        visit(ctx.right);

        Type leftType = types.get(ctx.left);
        Type rightType = types.get(ctx.right);
        Type returnType = Type.getReturnType(leftType, rightType);
        String mnemonic = Helper.getTypeMnemonic(returnType);
        String operator = Helper.getOperatorAsWord(ctx.op.getText());

        if (leftType != returnType) {
            code.add(Helper.getTypeMnemonic(leftType) + "2" + mnemonic);
            if (returnType == Type.DOUBLE) this.increaseStack(1);
        }

        code.addAll(visit(ctx.right));

        if (rightType != returnType) {
            code.add(Helper.getTypeMnemonic(rightType) + "2" + mnemonic);
            if (returnType == Type.DOUBLE) this.increaseStack(1);
        }

        // for comparing doubles some extra work is needed
        if (returnType == Type.DOUBLE) {
            code.add("dcmpg");
            code.add("iconst_0");

        }

        // just put 1 or 0 on the stack meaning true or false
        code.add("if_icmp" + operator + " then_c_" + localLabelCount);
        code.add("else_c_" + localLabelCount + ":");
        code.add("iconst_0");
        code.add("goto end_c_" + localLabelCount);
        code.add("then_c_" + localLabelCount + ":");
        code.add("iconst_1");
        code.add("end_c_" + localLabelCount + ":");

        return code;
    }

    /**
     * e.g 1 == 1 && 2 == 2
     *
     * @param ctx LogicalExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitLogicalExpression(CompilerParser.LogicalExpressionContext ctx) {
        //System.out.println("# VISITING LogicalExpression");
        ArrayList<String> code = new ArrayList<>();
        String operator = Helper.getOperatorAsWord(ctx.op.getText());
        code.addAll(visit(ctx.left));
        code.addAll(visit(ctx.right));

        // add the bitwise operator
        // e.g iand, ior
        code.add(operator);
        return code;
    }

    /**
     * e.g !true
     *
     * @param ctx NotExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitNotExpression(CompilerParser.NotExpressionContext ctx) {
        //System.out.println("# VISITING NotExpression");
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

    /**
     * e.g while (a < 1) { someCode }
     *
     * @param ctx WhileStatementContext
     * @return code
     */
    @Override
    public ArrayList<String> visitWhileStatement(CompilerParser.WhileStatementContext ctx) {
        //System.out.println("# VISITING WhileStatement");
        ArrayList<String> code = new ArrayList<>();

        int localLabelCount = ++labelCount;

        String beforeLabel = "before_w_" + localLabelCount;
        String thenLabel = "then_w_" + localLabelCount;
        String endLabel = "end_w_" + localLabelCount;

        code.add(beforeLabel + ":");
        code.addAll(visit(ctx.expression()));

        // the expressions put either 1 or 0 on the stack
        // compare it to 0
        code.add("ifne " + thenLabel);
        code.add("goto " + endLabel);
        code.add(thenLabel + ":");
        code.addAll(visit(ctx.statement()));
        code.add("goto " + beforeLabel);
        code.add(endLabel + ":");

        return code;
    }

    /**
     * e.g for (var a = 1; a < 10; a = a + 1) { someCode }
     *
     * @param ctx ForStatementContext
     * @return code
     */
    @Override
    public ArrayList<String> visitForStatement(CompilerParser.ForStatementContext ctx) {
        //System.out.println("# VISITING ForStatement");

        ArrayList<String> code = new ArrayList<>();

        int localLabelCount = ++labelCount;

        String beforeLabel = "before_f_" + localLabelCount;
        String thenLabel = "then_f_" + localLabelCount;
        String endLabel = "end_f_" + localLabelCount;


        // FOR '(' variableStatement ';' expression ';' assignmentStatement ')' statement
        // a new scope is needed for the variable declaration
        this.symbolTable.openScope();

        code.addAll(visit(ctx.variableStatement()));

        code.add(beforeLabel + ":");
        code.addAll(visit(ctx.expression()));
        code.add("ifne " + thenLabel);
        code.add("goto " + endLabel);
        code.add(thenLabel + ":");
        code.addAll(visit(ctx.statement()));
        code.addAll(visit(ctx.assignmentStatement()));
        code.add("goto " + beforeLabel);
        code.add(endLabel + ":");

        this.symbolTable.closeScope();


        return code;
    }

    /**
     * e.g. 1
     *
     * @param ctx IntConstExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitIntConstExpression(CompilerParser.IntConstExpressionContext ctx) {
        //System.out.println("# VISITING IntConstExpression");
        ArrayList<String> code = new ArrayList<>();
        code.add("ldc " + ctx.getText());
        this.increaseStack(1);
        return code;
    }

    /**
     * e.g. 1.1
     *
     * @param ctx DoubleConstExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitDoubleConstExpression(CompilerParser.DoubleConstExpressionContext ctx) {
        //System.out.println("# VISITING DoubleConstExpression");
        ArrayList<String> code = new ArrayList<>();
        code.add("ldc2_w " + ctx.getText());
        this.increaseStack(2);
        return code;
    }

    /**
     * e.g. true
     *
     * @param ctx BooleanConstExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitBooleanConstExpression(CompilerParser.BooleanConstExpressionContext ctx) {
        //System.out.println("# VISITING BooleanConstExpression");
        ArrayList<String> code = new ArrayList<>();
        code.add("iconst_" + Helper.getBooleanValue(ctx.getText()));
        this.increaseStack(1);
        return code;
    }

    /**
     * e.g a
     *
     * @param ctx VariableConstExpressionContext
     * @return code
     */
    @Override
    public ArrayList<String> visitVariableConstExpression(CompilerParser.VariableConstExpressionContext ctx) {
        //System.out.println("# VISITING VariableConstExpression");
        ArrayList<String> code = new ArrayList<>();
        Symbol symbol = symbolTable.retrieve(ctx.getText());
        // a value is being referenced so it has to be in the symboltable
        if (symbol != null) {
            code.add(Helper.getTypeMnemonic(symbol.getType()) + "load " + symbol.getAddress());
            this.types.put(ctx, symbol.getType());
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
