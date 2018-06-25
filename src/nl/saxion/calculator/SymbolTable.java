package nl.saxion.calculator;

import java.util.*;

public class SymbolTable {

    private Map<String, Stack<Symbol>> symbolTable;
    private Stack<List<String>> scopeStack;

    public SymbolTable() {
        symbolTable = new HashMap<>();
        scopeStack = new Stack<>();
    }

    public void openScope() {
        scopeStack.push(new ArrayList<>());
    }

    public void closeScope() {
        List<String> topList = scopeStack.pop();

        for (String name : topList) {
            symbolTable.get(name).pop();
        }

    }

    public void enter(String name, Symbol symbol) {
        scopeStack.peek().add(name);

        if (symbolTable.get(name) == null) {
            symbolTable.put(name, new Stack<>());
        }

        symbolTable.get(name).push(symbol);
    }

    public Symbol retrieve(String name) {

        if (symbolTable.get(name) != null) {
            return symbolTable.get(name).peek();
        }

        return null;
    }

    public int currentLevel() {
        return scopeStack.size();
    }



}
