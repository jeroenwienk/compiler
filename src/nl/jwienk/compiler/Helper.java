package nl.jwienk.compiler;

public class Helper {

    public static String getTypeMnemonic(Type type) {
        switch (type) {
            case INT:
            case BOOLEAN:
                return "i";
            case DOUBLE:
                return "d";
            default:
                return null;

        }
    }

    public static String getTypeDescriptor(Type type) {
        switch (type) {
            case INT:
                return "I";
            case BOOLEAN:
                return "Z";
            case DOUBLE:
                return "D";
            default:
                return null;

        }
    }

    public static String getOperatorAsWord(String operator) {
        switch (operator) {
            case "+":
                return "add";
            case "-":
                return "sub";
            case "*":
                return "mul";
            case "/":
                return "div";
            case "%":
                return "rem";
            default:
                return null;

        }
    }

    public static String getBooleanValue(String value) {
        switch (value) {
            case "true":
                return "1";
            case "false":
                return "0";
            case "0":
                return value;
            case "1":
                return value;
            default:
                return null;

        }
    }

}
