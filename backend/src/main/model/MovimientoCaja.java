package main.model;


public class MovimientoCaja {
    private final int id;
    private final int momento;
    private final String concepto;
    private final long importe;
    private final TMovimiento tipo;

    MovimientoCaja(int id,int momento, String concepto, long importe, TMovimiento tipo) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id del movimiento ha de ser mayor que 0");
        }
        if (momento < 0 || momento > Reloj.getInstance().horaActual()) {
            throw new IllegalArgumentException("El momento en el que se registra el movimiento no puede ser negativo ni posterior a la hora actual");
        }
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto del movimiento no puede ser null o vacio");
        }
        switch (tipo) {
            case null -> {
                throw new IllegalArgumentException("El tipo no puede ser null");
            }
            case COMPRA, FICHAJE, FIANZA -> {
                if (importe >= 0) throw new IllegalArgumentException("Un gasto debe tener importe negativo");
            }
            case COMISION -> {
                if (importe == 0) throw new IllegalArgumentException("Una comision no puede ser 0");
            }
        }

        this.id = id;
        this.momento = momento;
        this.concepto = concepto;
        this.importe = importe;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public int getMomento() {
        return momento;
    }

    public String getConcepto() {
        return concepto;
    }

    public long getImporte() {
        return importe;
    }

    public TMovimiento getTipo() {
        return tipo;
    }
}