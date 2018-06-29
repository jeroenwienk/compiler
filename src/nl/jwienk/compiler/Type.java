package nl.jwienk.compiler;

public enum Type {
    INT, DOUBLE, BOOLEAN, STRING, IDENTIFIER, METHOD, STATEMENT;

    public static boolean areCompatible(Type firstType, Type secondType) {

        if (firstType == secondType) return true;

        if (firstType == Type.DOUBLE && secondType == Type.INT) return true;

        if (secondType == Type.DOUBLE && firstType == Type.INT) return true;

        return false;
    }

    public static Type getReturnType(Type firstType, Type secondType) {

        if (firstType == Type.DOUBLE || secondType == Type.DOUBLE) return Type.DOUBLE;

        if (firstType == Type.INT && secondType == Type.INT) return Type.INT;

        return firstType;

    }

}
