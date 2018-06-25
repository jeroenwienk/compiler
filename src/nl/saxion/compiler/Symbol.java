package nl.saxion.compiler;

public class Symbol {
    String name;
    DataType type;
    String address;

    public Symbol(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
