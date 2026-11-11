package main.uc;

import java.util.*;

import main.ctrl.*;
import main.model.*;


public class EjecucionUC {

    private final CtrlMision ctrlMision;
    private final CtrlSecuaz ctrlSecuaz;
    private final CtrlEjecucion ctrlEjecucion;

    // els controladors de dades s'agafen de la factoria al constructor i es guarden (regla 2.3.3)
    public EjecucionUC() {
        FactoriaCtrl factoria = FactoriaCtrl.getInstance();
        this.ctrlMision = factoria.getCtrlMision();
        this.ctrlSecuaz = factoria.getCtrlSecuaz();
        this.ctrlEjecucion = factoria.getCtrlEjecucion();
    }


    // No crea res: serveix perquè la interfície mostri la probabilitat en viu mentre el Don tria l'equip
    public double previsualizarProbabilidad(String nombreClave, Map<TRol, List<Integer>> asignacion) {
        Mision mision = obtenerMision(nombreClave);
        Map<TRol, List<Secuaz>> equipo = convertirAsignacion(asignacion);
        return Ejecucion.calcularProbabilidadExito(mision, equipo);
    }

    // Tot o res: si no hi ha diners per a tot el material que falta, no compra res
    public boolean comprarMaterialFaltante(String nombreClave) {
        Mision mision = obtenerMision(nombreClave);
        Map<Objeto, Integer> faltante = mision.materialFaltante();

        // si no falta res no cal comprar (i Don.puedePagar(0) donaria false)
        if (faltante.isEmpty()) return true;

        Don don = Don.getInstance();
        if (!don.puedePagar(mision.costeMaterialFaltante())) return false;

        // una compra (un moviment de caixa) per cada unitat que falta
        for (Map.Entry<Objeto, Integer> e : faltante.entrySet()) {
            Objeto objeto = e.getKey();
            for (int i = 0; i < e.getValue(); i++) {
                if (!don.pagar(TMovimiento.COMPRA, objeto.getPrecio(), "Compra de " + objeto.getNombre())) {
                    throw new IllegalStateException("No se ha podido pagar " + objeto.getNombre());
                }
                objeto.añadir(1);
            }
        }
        return true;
    }

    public Ejecucion ejecutarMision(String nombreClave, Map<TRol, List<Integer>> asignacion) {
        Mision mision = obtenerMision(nombreClave);
        Map<TRol, List<Secuaz>> equipo = convertirAsignacion(asignacion);

        // la interfície ha d'haver ofert abans comprarMaterialFaltante
        if (!mision.materialFaltante().isEmpty()) {
            throw new IllegalStateException("Falta material para la mision " + nombreClave);
        }

        Ejecucion e = Ejecucion.iniciar(ctrlEjecucion.siguienteId(), mision, TEjecucion.MANUAL,
                Reloj.getInstance().horaActual(), equipo);
        ctrlEjecucion.insertar(e);
        return e;
    }


    // No modifica res: només proposa
    public PropuestaAutomatica proponerEjecucionAutomatica() {
        return SelectorAutomatico.proponer(ctrlMision.all(), ctrlSecuaz.all(), Don.getInstance().getCaja());
    }

    // Retorna els avisos de les missions saltades, amb el motiu
    public List<String> confirmarEjecucionAutomatica(PropuestaAutomatica propuesta) {
        List<String> avisos = new ArrayList<>();

        // en l'ordre de la proposta: cada inici descompta stock, així que les següents ja veuen el material reservat
        for (AsignacionPropuesta ap : propuesta.getAsignaciones()) {
            Mision mision = ap.getMision();
            String nombre = mision.getNombreClave();

            // 1. comprovar que res no ha canviat des que es va generar la proposta (abans de gastar diners)
            String motivo = motivoNoEjecutable(mision, ap.getAsignacion());
            if (motivo != null) {
                avisos.add(nombre + ": " + motivo);
                continue;
            }

            // 2. material
            if (!comprarMaterialFaltante(nombre)) {
                avisos.add(nombre + ": no hay dinero suficiente para comprar el material");
                continue;
            }

            // 3. iniciar l'execució
            try {
                Ejecucion e = Ejecucion.iniciar(ctrlEjecucion.siguienteId(), mision, TEjecucion.AUTOMATICA,
                        Reloj.getInstance().horaActual(), ap.getAsignacion());
                ctrlEjecucion.insertar(e);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                avisos.add(nombre + ": " + ex.getMessage());
            }
        }
        return avisos;
    }


    // Retorna per què una missió de la proposta ja no es pot executar, o null si tot continua igual
    private String motivoNoEjecutable(Mision mision, Map<TRol, List<Secuaz>> equipo) {
        if (!mision.puedeIniciarse()) {
            return "la mision ya no se puede iniciar";
        }
        for (List<Secuaz> lista : equipo.values()) {
            for (Secuaz s : lista) {
                if (!s.esLibre()) {
                    return "el secuaz " + s.getId() + " ya no esta libre";
                }
            }
        }
        return null;
    }

    private Mision obtenerMision(String nombreClave) {
        Mision mision = ctrlMision.get(nombreClave);
        if (mision == null) {
            throw new IllegalArgumentException("No existe la mision " + nombreClave);
        }
        return mision;
    }

    // Substitueix cada id pel seu Secuaz (per això la interfície només treballa amb ids)
    private Map<TRol, List<Secuaz>> convertirAsignacion(Map<TRol, List<Integer>> ids) {
        Map<TRol, List<Secuaz>> resultado = new EnumMap<>(TRol.class);
        for (Map.Entry<TRol, List<Integer>> e : ids.entrySet()) {
            List<Secuaz> lista = new ArrayList<>();
            for (int id : e.getValue()) {
                Secuaz s = ctrlSecuaz.get(id);
                if (s == null) {
                    throw new IllegalArgumentException("No existe el secuaz con id " + id);
                }
                lista.add(s);
            }
            resultado.put(e.getKey(), lista);
        }
        return resultado;
    }

}