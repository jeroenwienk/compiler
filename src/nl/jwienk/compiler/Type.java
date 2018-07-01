package nl.jwienk.compiler;

public enum Type {
    INT, DOUBLE, BOOLEAN, METHOD, STATEMENT, STRING;

    /**
     * Checks if 2 types are compatible with eachother
     * e.g. 1.1 + 1 can become a double
     *
     * @param firstType  type
     * @param secondType type
     * @return true if compatible else false
     */
    public static boolean areCompatible(Type firstType, Type secondType) {

        if (firstType == secondType) return true;

        if (firstType == Type.DOUBLE && secondType == Type.INT) return true;

        if (secondType == Type.DOUBLE && firstType == Type.INT) return true;

        return false;
    }

    /**
     * Get the return type for 2 types
     * e.g if one is a double and one is an int return double as the new type
     * 1.1 + 1 should become a double
     *
     * @param firstType  type
     * @param secondType type
     * @return type
     */
    public static Type getReturnType(Type firstType, Type secondType) {

        if (firstType == Type.DOUBLE || secondType == Type.DOUBLE) return Type.DOUBLE;

        if (firstType == Type.INT && secondType == Type.INT) return Type.INT;

        return firstType;

    }

}
