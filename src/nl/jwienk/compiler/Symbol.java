package nl.jwienk.compiler;

//TODO associate context

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

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }


}
