package nl.saxion.calculator;

public class DataType {
    public static int INT = 0;
    public static int BOOLEAN = 1;
    public static int STRING = 2;
    public static int IDENTIFIER = 3;
    public static int METHOD = 4;

    private int type;

    public DataType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
