package main.ctrl;
import main.model.TRol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.model.Candidato;

public class CtrlCandidato {
    private final Map<String, Candidato> candidatos = new LinkedHashMap<>();

    private void inicializarCandidatos() {
        insertar(new Candidato("Manu Bravas", "El Petancas", 5000L, TRol.ASALTO));
        insertar(new Candidato("Alex Herrero", "El Sobrino", 7500L, TRol.HACKER));
        insertar(new Candidato("Alicia Ageno", "La Coordi", 10000L, TRol.CONDUCTOR));
        insertar(new Candidato("Louis Litt", "El Reloj", 7500L, TRol.NEGOCIADOR));
        insertar(new Candidato("Sheila Sazs", "La Rotondas", 8500L, TRol.CONDUCTOR));
        insertar(new Candidato("Jaume Tiz", "Bonnie", 12000L, TRol.ASALTO));
        insertar(new Candidato("Raul Curisol", "Clyde", 9000L, TRol.CONDUCTOR));
        insertar(new Candidato("Tahira Montanyes", "La Bomba", 11000L, TRol.ASALTO));
        insertar(new Candidato("Claudio Codina", "El Becario", 15000L, TRol.HACKER));
        insertar(new Candidato("J Gorra", "El Pantallas", 13000L, TRol.HACKER));
        insertar(new Candidato("Andres Leal", "El Biceps", 14000L, TRol.ASALTO));
        insertar(new Candidato("JuliaAan Alvarez", "Busi", 9500L, TRol.CERRAJERO));
        insertar(new Candidato("Joaco Grados", "SJD's son", 8000L, TRol.CERRAJERO));
        insertar(new Candidato("Hustle Hard", "El Padre", 10500L, TRol.CERRAJERO));
        insertar(new Candidato("Peter Sanchez", "El Perro", 12500L, TRol.NEGOCIADOR));
        insertar(new Candidato("Maxi Pa", "El Camisas", 10000L, TRol.NEGOCIADOR));
        insertar(new Candidato("Nico Gonzalez", "El Mesias", 11500L, TRol.NEGOCIADOR));
        insertar(new Candidato("Sarai Susurros", "El Queso", 11500L, TRol.CONDUCTOR));
    }

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