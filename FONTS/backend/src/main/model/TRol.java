package main.model;

/**
 * @author Alejandro
 */
public enum TRol {
    CONDUCTOR (15, 1500, 10),
    ASALTO (20, 2000, 30),
    HACKER (3, 300, 5),
    CERRAJERO (5, 500, 15),
    NEGOCIADOR (10, 1000, 10);

    private final double comision;
    private final long fianzaBase;
    private final double probMuerte;

    private TRol (double comision, long fianzaBase, double probMuerte) {
        if (comision < 0 || comision > 100) {
            throw new IllegalArgumentException("La comision debe ser un valor entre 0 y 100");
        }
        if (probMuerte < 0 || probMuerte > 100) {
            throw new IllegalArgumentException("La probabilidad de muerte debe ser un valor entre 0 y 100");
        }
        if (fianzaBase <= 0) {
            throw new IllegalArgumentException("La fianza base debe ser mayor que 0");
        }

        this.comision = comision;
        this.fianzaBase = fianzaBase;
        this.probMuerte = probMuerte;
    }

    public double getComision() {
        return comision;
    }

    public long getFianzaBase() {
        return fianzaBase;
    }

    public double getProbMuerte() {
        return probMuerte;
    }

    public double getProbDetencion() {
        return 100 - probMuerte;
    }

    public boolean esConductor() {
        return this == CONDUCTOR;
    }

    public TConsecuencia decidirConsecuencia(double tirada) {
        if (tirada < probMuerte) return TConsecuencia.MUERTO;
        else return TConsecuencia.DETENIDO;
    }
}