package main.uc;

import main.ctrl.*;
import main.model.*;

import java.util.*;

/**
 * @author Pol
 */

public class EjecucionUC {

    private final CtrlMision ctrlMision;
    private final CtrlSecuaz ctrlSecuaz;
    private final CtrlEjecucion ctrlEjecucion;

    // Propuesta de la ronda automatica que espera confirmacion (null si no hay ninguna)
    private PropuestaAutomatica propuestaPendiente;

    public EjecucionUC() {
        FactoriaCtrl factoria = FactoriaCtrl.getInstance();
        this.ctrlMision = factoria.getCtrlMision();
        this.ctrlSecuaz = factoria.getCtrlSecuaz();
        this.ctrlEjecucion = factoria.getCtrlEjecucion();
    }


    // ejecucion manual

    // No crea nada: sirve para mostrar la probabilidad mientras el Don elige el equipo.
    // La asignacion es: nombre del rol del puesto -> ids de los secuaces elegidos
    public double previsualizarProbabilidad(String nombreClave, Map<String, List<Integer>> asignacion) {
        Mision mision = obtenerMision(nombreClave);
        return Ejecucion.calcularProbabilidadExito(mision, convertirAsignacion(asignacion));
    }

    // everything o nada: si no hay dinero para all of el material que falta, no compra nada
    public boolean comprarMaterialFaltante(String nombreClave) {
        Mision mision = obtenerMision(nombreClave);
        Map<Objeto, Integer> faltante = mision.materialFaltante();

        // si no falta nada no hay que comprar (y Don.puedePagar(0) devolveria false)
        if (faltante.isEmpty()) return true;

        Don don = Don.getInstance();
        if (!don.puedePagar(mision.costeMaterialFaltante())) return false;

        // un movimiento de caja por cada unidad que falta
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

    // Devuelve el id de la ejecucion creada
    public int ejecutarMision(String nombreClave, Map<String, List<Integer>> asignacion) {
        Mision mision = obtenerMision(nombreClave);
        Map<TRol, List<Secuaz>> equipo = convertirAsignacion(asignacion);

        // la interfaz debe haber ofrecido antes comprarMaterialFaltante
        if (!mision.materialFaltante().isEmpty()) {
            throw new IllegalStateException("Falta material para la mision " + nombreClave);
        }

        Ejecucion e = Ejecucion.iniciar(ctrlEjecucion.siguienteId(), mision, TEjecucion.MANUAL,
                Reloj.getInstance().horaActual(), equipo);
        ctrlEjecucion.insertar(e);
        return e.getId();
    }


    //ronda automatica

    // No modifica el sistema: solo propone. Guarda la propuesta para confirmarla despues.
    // Cada fila: {mision, probExito, beneficioEsperado, equipo}
    public List<String[]> proponerEjecucionAutomatica() {
        propuestaPendiente = SelectorAutomatico.proponer(ctrlMision.all(), ctrlSecuaz.all(),
                Don.getInstance().getCaja());

        List<String[]> filas = new ArrayList<>();
        for (AsignacionPropuesta ap : propuestaPendiente.getAsignaciones()) {
            filas.add(new String[] {
                    ap.getMision().getNombreClave(),
                    formatear(ap.getProbExito()),
                    formatear(ap.getBeneficioEsperado()),
                    equipoToString(ap.getAsignacion())
            });
        }
        return filas;
    }

    // Misiones de la ultima propuesta que no se han podido incluir, con el motivo
    public List<String> consultarExcluidasPropuesta() {
        if (propuestaPendiente == null) return new ArrayList<>();
        return new ArrayList<>(propuestaPendiente.getExcluidas());
    }

    // Ejecuta la ultima propuesta. Devuelve los avisos de las misiones saltadas, con el motivo
    public List<String> confirmarEjecucionAutomatica() {
        if (propuestaPendiente == null) {
            throw new IllegalStateException("No hay ninguna propuesta pendiente de confirmar");
        }
        PropuestaAutomatica propuesta = propuestaPendiente;
        propuestaPendiente = null;   // una propuesta solo se confirma una vez

        List<String> avisos = new ArrayList<>();

        // en el orden de la propuesta: cada inicio descuenta stock, asi que las siguientes ya ven el material reservado
        for (AsignacionPropuesta ap : propuesta.getAsignaciones()) {
            Mision mision = ap.getMision();
            String nombre = mision.getNombreClave();

            // 1. comprobar que nada ha cambiado desde que se genero la propuesta (antes de gastar dinero)
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

            // 3. iniciar la ejecucion
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



    // Devuelve por que una mision de la propuesta ya no se puede ejecutar, o null si everything sigue igual
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

    private TRol convertirRol(String rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }
        try {
            return TRol.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No existe el rol " + rol);
        }
    }

    // Sustituye cada id por su Secuaz y cada nombre de rol por su TRol
    private Map<TRol, List<Secuaz>> convertirAsignacion(Map<String, List<Integer>> asignacion) {
        if (asignacion == null) {
            throw new IllegalArgumentException("La asignacion no puede ser nula");
        }
        Map<TRol, List<Secuaz>> resultado = new EnumMap<>(TRol.class);
        for (Map.Entry<String, List<Integer>> e : asignacion.entrySet()) {
            TRol rol = convertirRol(e.getKey());
            if (resultado.containsKey(rol)) {
                throw new IllegalArgumentException("El rol " + rol + " esta repetido");
            }
            List<Secuaz> lista = new ArrayList<>();
            for (int id : e.getValue()) {
                Secuaz s = ctrlSecuaz.get(id);
                if (s == null) {
                    throw new IllegalArgumentException("No existe el secuaz con id " + id);
                }
                lista.add(s);
            }
            resultado.put(rol, lista);
        }
        return resultado;
    }

    // Formato: CONDUCTOR[1 Franklin] ASALTO[3 Trevor, 4 Packie McReary]  (siempre en el orden del enumerado)
    private String equipoToString(Map<TRol, List<Secuaz>> equipo) {
        StringBuilder sb = new StringBuilder();
        for (TRol rol : TRol.values()) {
            List<Secuaz> lista = equipo.get(rol);
            if (lista == null) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(rol.name()).append("[");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(lista.get(i).getId()).append(" ").append(lista.get(i).getNombre());
            }
            sb.append("]");
        }
        return sb.toString();
    }

    // Siempre con punto decimal y dos decimales, para que la salida sea igual en cualquier ordenador
    private String formatear(double valor) {
        return String.format(Locale.ROOT, "%.2f", valor);
    }
}