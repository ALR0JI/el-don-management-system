package main.ctrl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.model.Candidato;

public class CtrlCandidato {
    private final Map<String, Candidato> candidatos = new LinkedHashMap<>();

    public Candidato get(String nombre) {
        return candidatos.get(nombre);
    }

    public boolean exists(String nombre) {
        return candidatos.containsKey(nombre);
    }

    public List<Candidato> all() {
        return List.copyOf(candidatos.values());
    }

    public void insertar(Candidato candidato) {
        if (candidato == null) {
            throw new IllegalArgumentException("El candidato no puede ser null");
        }
        if (candidatos.containsKey(candidato.getNombre())) {
            throw new IllegalArgumentException("El candidato " + candidato.getNombre() + " ya existe");
        }

        candidatos.put(candidato.getNombre(), candidato);
    }
}