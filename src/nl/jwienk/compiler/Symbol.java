package nl.jwienk.compiler;

import org.antlr.v4.runtime.tree.ParseTree;

public class Symbol {
    private ParseTree ctx;
    private String name;
    private Type type;
    private int address;

    public Symbol(ParseTree ctx, String name, Type type) {
        this.ctx = ctx;
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ParseTree getCtx() {
        return ctx;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }


}
