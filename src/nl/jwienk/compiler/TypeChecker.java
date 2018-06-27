package nl.jwienk.compiler;

public class TypeChecker {

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
