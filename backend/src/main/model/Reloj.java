package main.model;

public class Reloj {
    private static Reloj instance;
    private int hora = 0;

    private Reloj() {}

    public static Reloj getInstance() {
        if (instance == null) instance = new Reloj();
        return instance;
    }

    public int horaActual() {
        return hora;
    }

    public void avanzar() {
        throw new UnsupportedOperationException("Entrega 2");
    }

    public static void reiniciar() {
        instance = null;
    }
}