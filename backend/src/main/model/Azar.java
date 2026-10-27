package main.model;

import java.util.Random;

public class Azar {
    private static Random random;

    private Azar() {}

    public static Random get() {
        throw new UnsupportedOperationException("Entrega 2");
    }

    public static void setSemilla(long semilla) {
        throw new UnsupportedOperationException("Entrega 2");
    }
}