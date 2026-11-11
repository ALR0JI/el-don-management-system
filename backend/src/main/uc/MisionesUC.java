package main.uc;

import main.model.*;
import main.ctrl.*;

import java.util.ArrayList;
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

    //devuelve la misión con el nombreClave indicado (uso interno)
    private Mision obtenerMision(String nombreClave) {
        Mision m = ctrlMision.get(nombreClave);
        if (m == null) {
            throw new IllegalArgumentException("No existe ninguna misión con el nombre clave: " + nombreClave);
        }
        return m;
    }

    //convierte un string de un rol en TRol 
    private TRol convertirRol(String rolStr) {
        if (rolStr == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }
        try {
            return TRol.valueOf(rolStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No existe el rol: " + rolStr);
        }
    }

    //convierte un string de un estado en TEstado
    private TEstado convertirEstado(String estadoStr) {
        if (estadoStr == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        try {
            return TEstado.valueOf(estadoStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No existe el estado: " + estadoStr);
        }
    }

    //convierte un objeto Mision en una strings de strings: {nombreClave, ubicacion, botin, probFracaso, duracion, estado}
    private String[] stringsMision(Mision m) {
        String[] resultado = new String[6];
        resultado[0] = m.getNombreClave();
        
        if(m.getUbicacion() != null){
            resultado[1] = m.getUbicacion();
        }else{
            resultado[1] = "";
        }

        resultado[2] = String.valueOf(m.getBotin());

        resultado[3] = String.valueOf(m.getProbFracaso());

        if(String.valueOf(m.getDuracion()) != null){
            resultado[4] = String.valueOf(m.getDuracion());
        }else{
            resultado[4] = "";
        }

        if(m.getEstado() != null){
            resultado[5] = m.getEstado().name();
        }else{
            resultado[5] = "";
        }

        return resultado;
    }

    // Crear nueva misión (la plantilla entra como Map<String, Integer> para no usar TRol fuera)
    public void crearMision(String nombreClave, String ubicacion, long botin, double probFracaso,
                            Integer duracion, Map<String, Integer> plantilla,
                            List<String> prerrequisitos, Map<String, Integer> material) {

        if (ctrlMision.exists(nombreClave)) {
            throw new IllegalArgumentException("Ya existe una misión con el nombre clave: " + nombreClave); 
        }

        //pasar el mapa de srtings a TRol
        Map<TRol, Integer> plantillaRoles = new HashMap<>();
        if (plantilla != null) {
            for (Map.Entry<String, Integer> entry : plantilla.entrySet()) {
                TRol rol = convertirRol(entry.getKey());
                plantillaRoles.put(rol, entry.getValue());
            }
        }

        List<Mision> misionesPrerrequisito = new ArrayList<>();
        if (prerrequisitos != null) {
            for (String prereqNombre : prerrequisitos) {
                Mision prereq = obtenerMision(prereqNombre);
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

        Mision mision = new Mision(nombreClave, ubicacion, botin, probFracaso, duracion, plantillaRoles);

        for (Mision prereq : misionesPrerrequisito) {
            mision.añadirPrerrequisito(prereq); 
        }

        for (Map.Entry<Objeto, Integer> entry : requisitosObjeto.entrySet()) {
            mision.añadirRequisitoMaterial(entry.getKey(), entry.getValue()); 
        }

        ctrlMision.insertar(mision); 
    }

    public void modificarMision(String nombreClave, String ubicacion, long botin, double probFracaso, int duracion) {
        Mision mision = obtenerMision(nombreClave);
        mision.setUbicacion(ubicacion); 
        mision.setBotin(botin); 
        mision.setProbFracaso(probFracaso); 
        mision.setDuracion(duracion); 
    }
    
    public void eliminarMision(String nombreClave) {
        Mision mision = obtenerMision(nombreClave);
        mision.desvincular(); //lanza excepción si tiene dependientes o ejecuciones 
        ctrlMision.eliminar(nombreClave); 
    }

    public void añadirPrerrequisito(String nombreClave, String nombrePrerrequisito) {
        Mision mision = obtenerMision(nombreClave);
        Mision prereq = obtenerMision(nombrePrerrequisito);
        mision.añadirPrerrequisito(prereq); 
    }

    public void eliminarPrerrequisito(String nombreClave, String nombrePrerrequisito) {
        Mision mision = obtenerMision(nombreClave);
        Mision prereq = obtenerMision(nombrePrerrequisito);
        mision.eliminarPrerrequisito(prereq); 
    }

    public void definirRolPlantilla(String nombreClave, String rolStr, int cantidad) {
        Mision mision = obtenerMision(nombreClave);
        TRol rol = convertirRol(rolStr);
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

    public void eliminarRolPlantilla(String nombreClave, String rolStr) {
        Mision mision = obtenerMision(nombreClave);
        TRol rol = convertirRol(rolStr);
        mision.eliminarPlantillaRol(rol); 
    }

    public void definirMaterial(String nombreClave, String nombreObjeto, int cantidad) {
        Mision mision = obtenerMision(nombreClave);
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
        Mision mision = obtenerMision(nombreClave);
        Objeto objeto = ctrlObjeto.get(nombreObjeto);
        if (objeto == null) {
            throw new IllegalArgumentException("No existe el objeto: " + nombreObjeto);
        }
        mision.eliminarRequisitoMaterial(objeto); 
    }

    public String[] consultarMision(String nombreClave) {
        Mision mision = obtenerMision(nombreClave);
        return stringsMision(mision);
    }

    //devuelve las misiones ordenadoas por estado y botin
    public List<String[]> listarMisiones(String estadoStr) {
        TEstado estadoFiltro = null;
        if (estadoStr != null && !estadoStr.trim().isEmpty()) {
            estadoFiltro = convertirEstado(estadoStr);
        }

        List<Mision> todas = ctrlMision.all();
        List<Mision> resultado = new ArrayList<>();

        for (Mision m : todas) {
            if (estadoFiltro == null || m.getEstado() == estadoFiltro) {
                resultado.add(m);
            }
        }

        //ordenar por botín de mayor a menor
        resultado.sort((m1, m2) -> Long.compare(m2.getBotin(), m1.getBotin()));

        List<String[]> filas = new ArrayList<>();
        for (Mision m : resultado) {
            filas.add(stringsMision(m));
        }
        return filas;
    }

}