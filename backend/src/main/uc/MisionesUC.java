package main.uc;

import main.model.*;
import main.ctrl.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MisionesUC{
    private final CtrlMision ctrlMision;
    private final CtrlObjeto ctrlObjeto;

    public MisionesUC() {
        this.ctrlMision = FactoriaCtrl.getInstance().getCtrlMision(); 
        this.ctrlObjeto = FactoriaCtrl.getInstance().getCtrlObjeto(); 
    }

    //constructor manual
    public MisionesUC(CtrlMision ctrlMision, CtrlObjeto ctrlObjeto) {
        this.ctrlMision = ctrlMision;
        this.ctrlObjeto = ctrlObjeto;
    }

    //crear nueva mision
    public Mision crearMision(String nombreClave, String ubicacion, long botin, double probFracaso,
                              Integer duracion, Map<TRol, Integer> plantilla,
                              List<String> prerrequisitos, Map<String, Integer> material) {

        if (ctrlMision.exists(nombreClave)) {
            throw new IllegalArgumentException("Ya existe una misión con el nombre clave: " + nombreClave); 
        }

        List<Mision> misionesPrerrequisito = new ArrayList<>();
        if (prerrequisitos != null) {
            for (String prereqNombre : prerrequisitos) {
                Mision prereq = ctrlMision.get(prereqNombre);
                if (prereq == null) {
                    throw new IllegalArgumentException("No existe el prerrequisito: " + prereqNombre); 
                }
                misionesPrerrequisito.add(prereq);
            }
        }

        Map<Objeto, Integer> requisitosObjeto = new HashMap<>();
        if (material != null) {
            for (Map.Entry<String, Integer> entry : material.entrySet()) {
                Objeto obj = ctrlObjeto.get(entry.getKey());
                if (obj == null) {
                    throw new IllegalArgumentException("No existe el objeto: " + entry.getKey()); 
                }
                requisitosObjeto.put(obj, entry.getValue());
            }
        }

        Mision mision = new Mision(nombreClave, ubicacion, botin, probFracaso, duracion, plantilla);

        for (Mision prereq : misionesPrerrequisito) {
            mision.añadirPrerrequisito(prereq); 
        }

        for (Map.Entry<Objeto, Integer> entry : requisitosObjeto.entrySet()) {
            mision.añadirRequisitoMaterial(entry.getKey(), entry.getValue()); 
        }

        
        ctrlMision.insertar(mision); 
        return mision;
    }

    public void modificarMision(String nombreClave, String ubicacion, long botin, double probFracaso, int duracion) {
        Mision mision = consultarMision(nombreClave);
        mision.setUbicacion(ubicacion); 
        mision.setBotin(botin); 
        mision.setProbFracaso(probFracaso); 
        mision.setDuracion(duracion); 
    }
    
    public void eliminarMision(String nombreClave) {
        Mision mision = consultarMision(nombreClave);
        mision.desvincular(); //lanza excepción si tiene dependientes o ejecuciones 
        ctrlMision.eliminar(nombreClave); 
    }

    public void añadirPrerrequisito(String nombreClave, String nombrePrerrequisito) {
        Mision mision = consultarMision(nombreClave);
        Mision prereq = consultarMision(nombrePrerrequisito);
        mision.añadirPrerrequisito(prereq); 
    }

    public void eliminarPrerrequisito(String nombreClave, String nombrePrerrequisito) {
        Mision mision = consultarMision(nombreClave);
        Mision prereq = consultarMision(nombrePrerrequisito);
        mision.eliminarPrerrequisito(prereq); 
    }

    public void definirRolPlantilla(String nombreClave, TRol rol, int cantidad) {
        Mision mision = consultarMision(nombreClave);
        boolean encontrado = false;
        for (PlantillaRol p : mision.getPlantillasRol()) {
            if (p.getRol() == rol) {
                p.setCantidad(cantidad); 
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            mision.añadirPlantillaRol(rol, cantidad); 
        }
    }

    public void eliminarRolPlantilla(String nombreClave, TRol rol) {
        Mision mision = consultarMision(nombreClave);
        mision.eliminarPlantillaRol(rol); 
    }

    public void definirMaterial(String nombreClave, String nombreObjeto, int cantidad) {
        Mision mision = consultarMision(nombreClave);
        Objeto objeto = ctrlObjeto.get(nombreObjeto);
        if (objeto == null) {
            throw new IllegalArgumentException("No existe el objeto: " + nombreObjeto);
        }

        boolean encontrado = false;
        for (RequisitoMaterial rm : mision.getRequisitosMateriales()) {
            if (rm.getObjeto().getNombre().equals(nombreObjeto)) {
                rm.setCantidad(cantidad); 
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            mision.añadirRequisitoMaterial(objeto, cantidad); 
        }
    }

    public void eliminarMaterial(String nombreClave, String nombreObjeto) {
        Mision mision = consultarMision(nombreClave);
        Objeto objeto = ctrlObjeto.get(nombreObjeto);
        if (objeto == null) {
            throw new IllegalArgumentException("No existe el objeto: " + nombreObjeto);
        }
        mision.eliminarRequisitoMaterial(objeto); 
    }

    public Mision consultarMision(String nombreClave) {
        Mision mision = ctrlMision.get(nombreClave);
        if (mision == null) {
            throw new IllegalArgumentException("No existe ninguna misión con el nombre clave: " + nombreClave); 
        }
        return mision;
    }

    //devuelve las misiones ordenadoas por botin
    public List<Mision> listarMisiones(TEstado estado) {
        List<Mision> todas = ctrlMision.all();
        List<Mision> resultado = new ArrayList<>();

        for (Mision m : todas) {
            if (estado == null || m.getEstado() == estado) {
                resultado.add(m);
            }
        }

        // Ordenar por botín de mayor a menor (a igual botín, mantiene el orden de inserción)
        resultado.sort((m1, m2) -> Long.compare(m2.getBotin(), m1.getBotin()));
        return Collections.unmodifiableList(resultado);
    }

}