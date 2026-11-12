package main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignacionPropuesta {

    private final Mision mision;
    private final Map<TRol, List<Secuaz>> asignacion;
    private final double probExito;
    private final double beneficioEsperado;

    public AsignacionPropuesta(Mision mision, Map<TRol, List<Secuaz>> asignacion,
                               double probExito, double beneficioEsperado) {
        if (mision == null || asignacion == null) {
            throw new IllegalArgumentException("Mision y asignacion no pueden ser nulas");
        }
        this.mision = mision;
        // copia propia: si quien la creó la modifica después, la propuesta no cambia
        this.asignacion = new HashMap<>();
        for (Map.Entry<TRol, List<Secuaz>> e : asignacion.entrySet()) {
            this.asignacion.put(e.getKey(), Collections.unmodifiableList(new ArrayList<>(e.getValue())));
        }
        this.probExito = probExito;
        this.beneficioEsperado = beneficioEsperado;
    }

    public Mision getMision() { return mision; }
    public double getProbExito() { return probExito; }
    public double getBeneficioEsperado() { return beneficioEsperado; }

    public Map<TRol, List<Secuaz>> getAsignacion() {
        return Collections.unmodifiableMap(asignacion);
    }
}