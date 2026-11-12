package main.uc;

import main.ctrl.CtrlEjecucion;
import main.ctrl.CtrlMision;
import main.ctrl.CtrlSecuaz;
import main.ctrl.FactoriaCtrl;
import main.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Alejandro
 */
public class SeguimientoUC {
    private static final int TAM_TOP = 3;

    private final CtrlMision ctrlMision;
    private final CtrlSecuaz ctrlSecuaz;
    private final CtrlEjecucion ctrlEjecucion;

    public SeguimientoUC() {
        FactoriaCtrl factoriaCtrl = FactoriaCtrl.getInstance();
        ctrlMision = factoriaCtrl.getCtrlMision();
        ctrlSecuaz = factoriaCtrl.getCtrlSecuaz();
        ctrlEjecucion = factoriaCtrl.getCtrlEjecucion();
    }

    public long consultarCaja() {
        return Don.getInstance().getCaja();
    }

    public List<String[]> consultarMovimientos() {
        List<String[]> filas = new ArrayList<>();
        for (MovimientoCaja m : Don.getInstance().getMovimientos()) {
            String[] movimientoString = {String.valueOf(m.getId()),
                                        String.valueOf(m.getMomento()),
                                        m.getTipo().name(),
                                        m.getConcepto(),
                                        String.valueOf(m.getImporte())};
            filas.add(movimientoString);
        }
        return filas;
    }

    public List<String[]> listarEjecucionesEnCurso() {
        int hora = Reloj.getInstance().horaActual();
        List<String[]> filas = new ArrayList<>();
        for (Ejecucion e : ctrlEjecucion.all()) {
            if (e.estaEnCurso()) {
                filas.add(ejecucionToStringVec(e, hora));
            }
        }

        return filas;
    }

    public List<String[]> consultarHistorial() {
        int hora = Reloj.getInstance().horaActual();
        List<Ejecucion> ordenadas = new ArrayList<>(ctrlEjecucion.all());
        ordenadas.sort(Comparator.comparingInt(Ejecucion::getInicio)
                .thenComparingInt(Ejecucion::getId)
                .reversed());

        List<String[]> filas = new ArrayList<>();
        for (Ejecucion e : ordenadas) {
            filas.add(ejecucionToStringVec(e, hora));
        }
        return filas;
    }

    public int numeroNoLeidas() {
        int count = 0;
        for (Ejecucion e : ctrlEjecucion.all()) {
            if (!e.estaEnCurso() && !e.isLeida()) count++;
        }

        return  count;
    }

    public List<String[]> abrirNotificaciones() {
        // se le da valor antes de actualizar el estado para evitar que esten todas leidas siempre
        List<String[]> filas = consultarHistorial();

        for (Ejecucion e : ctrlEjecucion.all()) {
            e.marcarComoLeida();
        }

        return filas;
    }

    public long estadoDeCuentas(int idSecuaz) {
        Secuaz s;
        if ((s = ctrlSecuaz.get(idSecuaz)) == null) {
            throw new IllegalArgumentException("No existe ningún secuaz con id " + idSecuaz);
        }
        return s.getDineroAcumulado();
    }

    public String[] obtenerCompliceHabitual(int idSecuaz) {
        Secuaz s, complice;
        if ((s = ctrlSecuaz.get(idSecuaz)) == null) {
            throw new IllegalArgumentException("No existe ningún secuaz con id " + idSecuaz);
        }

        if ((complice = s.obtenerCompliceHabitual()) == null) {
            return null;
        }
        return secuazToStringVec(complice);
    }

    public List<String[]> hojaDeServicios(int idSecuaz) {
        List<String[]> filas = new ArrayList<>();
        Secuaz s;
        if ((s = ctrlSecuaz.get(idSecuaz)) == null) {
            throw new IllegalArgumentException("No existe ningún secuaz con id " + idSecuaz);
        }

        for (Participacion p : s.hojaDeServicios()) {
            Ejecucion e = p.getEjecucion();
            filas.add(new String[] {
                    String.valueOf(e.getId()),
                    e.getMision().getNombreClave(),
                    e.getResultado().name(),
                    p.getRolDesempenado().name(),
                    String.valueOf(p.getCobrado()),
                    p.getConsecuencia().name()
            });
        }
        return filas;
    }

    public List<String[]> top3Operativos() {
        List<Secuaz> ordenados = new ArrayList<>(ctrlSecuaz.all());
        ordenados.sort(Comparator.comparingInt(Secuaz::numMisiones).reversed());

        List<String[]> filas = new ArrayList<>();
        int cantidad = Math.min(TAM_TOP, ordenados.size());
        for (int i = 0; i < cantidad; i++) {
            filas.add(secuazToStringVec(ordenados.get(i)));
        }

        return filas;
    }

    public List<String[]> top3GolpesHistoricos() {
        List<Mision> ordenadas = new ArrayList<>(ctrlMision.all());
        ordenadas.sort(Comparator.comparingLong(Mision::getBotin).reversed());

        List<String[]> filas = new ArrayList<>();
        int cantidad = Math.min(TAM_TOP, ordenadas.size());
        for (int i = 0; i < cantidad; i++) {
            filas.add(misionToStringVec(ordenadas.get(i)));
        }
        return filas;
    }

    public List<String[]> top3PuntosCalientes() {
        List<Mision> conFracasos = new ArrayList<>();
        for (Mision m : ctrlMision.all()) {
            if (m.getNumFracasos() > 0) {
                conFracasos.add(m);
            }
        }
        conFracasos.sort(Comparator.comparingInt(Mision::getNumFracasos).reversed());

        List<String[]> filas = new ArrayList<>();
        int cantidad = Math.min(TAM_TOP, conFracasos.size());
        for (int i = 0; i < cantidad; i++) {
            filas.add(misionToStringVec(conFracasos.get(i)));
        }
        return filas;
    }

    private String[] ejecucionToStringVec(Ejecucion e, int hora) {
        return new String[] {
                String.valueOf(e.getId()),
                e.getMision().getNombreClave(),
                e.getTipo().name(),
                String.valueOf(e.getInicio()),
                String.valueOf(e.getFin()),
                String.valueOf(e.getTiempoRestante(hora)),
                e.getResultado().name(),
                String.valueOf(e.isLeida()),
                String.valueOf(e.getMision().getBotin()),
                String.valueOf(e.getTotalCobrado()),
                String.valueOf(e.getComisionDon()),
                String.valueOf(e.contarConsecuencias(TConsecuencia.MUERTO)),
                String.valueOf(e.contarConsecuencias(TConsecuencia.DETENIDO)),
                e.getMaterialPerdido(),
                String.valueOf(e.getProbExito())
        };
    }

    private String[] secuazToStringVec(Secuaz s) {
        return new String[] {
                String.valueOf(s.getId()),
                s.getNombre(),
                s.getApodo() == null ? "" : s.getApodo(),
                s.getEspecialidad().name(),
                s.getEstado().name(),
                String.valueOf(s.numMisiones())
        };
    }

    private String[] misionToStringVec(Mision m) {
        return new String[] {
                m.getNombreClave(),
                m.getUbicacion() == null ? "" : m.getUbicacion(),
                String.valueOf(m.getBotin()),
                m.getEstado().name(),
                String.valueOf(m.getNumIntentos()),
                String.valueOf(m.getNumFracasos())
        };
    }


}