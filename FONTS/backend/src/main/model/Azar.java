package main.model;

import java.util.Random;


/**
 * @author Alejandro
 */
public class Azar {
    private static Random instance;

    private Azar() {}

    public static Random getInstance() {
        if (instance == null) instance = new Random();
        return instance;
    }

    public static void setSemilla(long semilla) {
        instance = new Random(semilla);
    }

    // Esta funcion se usa unicamente para simplificar el codigo de los tests
    public static void reiniciar() {
        instance = null;
    }
}