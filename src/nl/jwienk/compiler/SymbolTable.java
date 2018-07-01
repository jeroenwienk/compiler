package nl.jwienk.compiler;


import java.util.*;

public class SymbolTable {

    private Map<String, Stack<Symbol>> symbolTable;
    private Stack<List<String>> scopeStack;

    public SymbolTable() {
        symbolTable = new HashMap<>();
        scopeStack = new Stack<>();
    }

    /**
     * Open a new scope
     */
    public void openScope() {
        scopeStack.push(new ArrayList<>());
    }

    /**
     * Close the last scope on the stack
     */
    public void closeScope() {
        List<String> topList = scopeStack.pop();

        for (String name : topList) {
            symbolTable.get(name).pop();
        }

    }

    /**
     * Enter a value into the current scope
     *
     * @param name   valueName
     * @param symbol valueSymbol
     */
    public void enter(String name, Symbol symbol) {
        scopeStack.peek().add(name);

        if (symbolTable.get(name) == null) {
            symbolTable.put(name, new Stack<>());
        }

        symbolTable.get(name).push(symbol);
    }

    /**
     * Retrieve the first symbol based on the name
     *
     * @param name name of the variable
     * @return Symbol
     */
    public Symbol retrieve(String name) {

        if (symbolTable.get(name) != null && !symbolTable.get(name).empty()) {
            return symbolTable.get(name).peek();
        }

        return null;
    }

    public int currentLevel() {
        return scopeStack.size();
    }


}
