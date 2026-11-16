package main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Pol
 */
public class PropuestaAutomatica {

    private final List<AsignacionPropuesta> asignaciones;
    private final List<String> excluidas;

    public PropuestaAutomatica(List<AsignacionPropuesta> asignaciones,
                               List<String> excluidas) {
        if (asignaciones == null || excluidas == null) {
            throw new IllegalArgumentException("Las listas no pueden ser nulas");
        }
        this.asignaciones = new ArrayList<>(asignaciones);
        this.excluidas = new ArrayList<>(excluidas);
    }

    public List<AsignacionPropuesta> getAsignaciones() {
        return Collections.unmodifiableList(asignaciones);
    }

    public List<String> getExcluidas() {
        return Collections.unmodifiableList(excluidas);
    }
}