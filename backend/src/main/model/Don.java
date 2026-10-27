package main.model;

import java.util.ArrayList;
import java.util.List;


public class Don {
    public static final double COMISION_MINIMA_DON = 20;
    private static Don instance;

    private String nombre;
    private long caja;
    private final List<MovimientoCaja> movimientos = new ArrayList<>();


    private Don() {
        nombre = "Don";
        caja = 0;
    }

    public static Don getInstance() {
        if (instance == null) instance = new Don();
        return instance;
    }

    public String getNombre() {
        return nombre;
    }

    public long getCaja() {
        return caja;
    }

    public List<MovimientoCaja> getMovimientos() {
        return List.copyOf(movimientos);
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede ser null o vacio");
        }
        this.nombre = nombre;
    }

    public boolean puedePagar(long importe) {
        return importe > 0 && importe <= caja;
    }

    public boolean pagar(TMovimiento tipo, long importe, String concepto) {
        if (tipo != TMovimiento.COMPRA && tipo != TMovimiento.FIANZA && tipo != TMovimiento.FICHAJE) {
            throw new IllegalArgumentException("El tipo de pago solo puede ser COMPRA, FICHAJE o FIANZA");
        }
        if (importe <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que 0");
        }
        if (!puedePagar(importe)) {
            return false;
        }

        registrarMovimiento(tipo, -importe, concepto);
        return true;
    }

    public void registrarComision(long importe, String concepto) {
        if (importe != 0) registrarMovimiento(TMovimiento.COMISION, importe, concepto);
    }

    private MovimientoCaja registrarMovimiento(TMovimiento tipo, long importe, String concepto) {
        // ALEJANDRO: No hace falta comprobar los parametros ya que la propia constructora ya lo hace
        MovimientoCaja mc = new MovimientoCaja(siguienteIdMovimiento(), Reloj.getInstance().horaActual(), concepto, importe, tipo);
        movimientos.add(mc);
        caja += importe;
        return mc;
    }

    public List<MovimientoCaja> movimientosPorTipo(TMovimiento tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo no puede ser null");
        }

        List<MovimientoCaja> movimientosTipo = new ArrayList<>();
        for (MovimientoCaja m : movimientos) {
            if (m.getTipo() == tipo) movimientosTipo.add(m);
        }
        return movimientosTipo;
    }

    private int siguienteIdMovimiento() {
        return movimientos.size() + 1;
    }

    public static void reiniciar() {
        instance = null;
    }
}



