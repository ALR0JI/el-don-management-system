package main.ctrl;
import main.model.TRol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.model.Candidato;

public class CtrlCandidato {
    private final Map<String, Candidato> candidatos = new LinkedHashMap<>();

    private void inicializarCandidatos() {
        insertar(new Candidato("Manuel Bravo", "El Petancas", 5000L, TRol.ASALTO));
        insertar(new Candidato("Alex Herrero", "El Sobrino", 7500L, TRol.HACKER));
        insertar(new Candidato("Alicia Ageno", "La Coordi", 10000L, TRol.CONDUCTOR));
        insertar(new Candidato("Marcos Vidal", "El Volante", 7500L, TRol.CONDUCTOR));
        insertar(new Candidato("Sergio Molina", "El Ruta", 8500L, TRol.CONDUCTOR));
        insertar(new Candidato("Laura Campos", "La Sombra", 12000L, TRol.ASALTO));
        insertar(new Candidato("Iván Romero", "El Martillo", 9000L, TRol.ASALTO));
        insertar(new Candidato("David Ortega", "El Tanque", 11000L, TRol.ASALTO));
        insertar(new Candidato("Nuria Ferrer", "La Fantasma", 15000L, TRol.HACKER));
        insertar(new Candidato("Adrián Costa", "Byte", 13000L, TRol.HACKER));
        insertar(new Candidato("Claudia Reyes", "La Firewall", 14000L, TRol.HACKER));
        insertar(new Candidato("Pablo Serrano", "La Ganzúa", 9500L, TRol.CERRAJERO));
        insertar(new Candidato("Elena Márquez", "La Llave", 8000L, TRol.CERRAJERO));
        insertar(new Candidato("Rubén Castillo", "El Cerrajero", 10500L, TRol.CERRAJERO));
        insertar(new Candidato("Marta Soler", "La Diplomática", 12500L, TRol.NEGOCIADOR));
        insertar(new Candidato("Óscar Navarro", "El Trato", 10000L, TRol.NEGOCIADOR));
        insertar(new Candidato("Paula Martín", "La Mediadora", 11500L, TRol.NEGOCIADOR));
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