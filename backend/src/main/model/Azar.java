package main.model;

import java.util.Random;

public class Azar {
    private static Random instance;

    private Azar() {}

    public static Random getInstance() {
        if (instance == null) instance = new Random();
        return instance;
    }

    public static void setSemilla(long semilla) {
        throw new UnsupportedOperationException("Entrega 2");
    }
}