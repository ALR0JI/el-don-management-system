package main.ctrl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.model.Objeto;


public class CtrlObjeto {
    private final Map<String, Objeto> objetos = new LinkedHashMap<>();

    public Objeto get(String nombre) {
        return objetos.get(nombre);
    }

    public boolean exists(String nombre) {
        return objetos.containsKey(nombre);
    }

    public List<Objeto> all() {
        return List.copyOf(objetos.values());
    }

    public void insertar(Objeto objeto) {
        if (objeto == null) {
            throw new IllegalArgumentException("El objeto no puede ser null");
        }
        if (objetos.containsKey(objeto.getNombre())) {
            throw new IllegalArgumentException("El objeto " + objeto.getNombre() + " ya existe");
        }

        objetos.put(objeto.getNombre(), objeto);
    }
}