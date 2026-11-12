package main.model;

import java.util.*;

public class Mision{
    public static final int DURACION_POR_DEFECTO = 12;

    private final String nombreClave;
    private String ubicacion;
    private long botin;
    private double probFracaso;
    private int duracion;
    private TEstado estado;

    private final List<Mision> prerrequisitos = new ArrayList<>();
    private final List<Mision> dependientes = new ArrayList<>();
    private final List<PlantillaRol> plantillasRol = new ArrayList<>();
    private final List<RequisitoMaterial> requisitosMateriales = new ArrayList<>();
    private final List<Ejecucion> ejecuciones = new ArrayList<>();
    
    public Mision(String nombreClave, String ubicacion, long botin, double probFracaso, Integer duracion, Map<TRol, Integer> plantilla){
        if(nombreClave == null || nombreClave.trim().length() == 0){
            throw new IllegalArgumentException("El nombre clave de la mision no puede ser nulo ni estar vacio.");   
        }

        if(botin <= 0){
            throw new IllegalArgumentException("El botin de la mision ha de ser mayor que 0.");   
        }

        if(probFracaso < 0.0 || probFracaso > 100.0){
            throw new IllegalArgumentException("La probabilidad de fracaso de la mision ha de estar entre 0 y 100.");   
        }

        if(duracion != null && duracion <= 0){
            throw new IllegalArgumentException("La duracion de la mision ha de ser mayor que 0.");   
        }

        if(plantilla == null || plantilla.isEmpty()){
            throw new IllegalArgumentException("La plantilla de la mision no puede estar vacia.");   
        }

        boolean tieneConductor = false;
        for (Map.Entry<TRol, Integer> entry : plantilla.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Todas las cantidades de la plantilla han de ser mayores que 0.");
            }
            if (entry.getKey() == TRol.CONDUCTOR) {
                tieneConductor = true;
            }
        }

        if(!tieneConductor){
            throw new IllegalArgumentException("Todas las plantillas han de tener un conductor.");
        }

        this.nombreClave = nombreClave;
        this.ubicacion = ubicacion;
        this.botin = botin; 
        this.probFracaso = probFracaso;

        if(duracion != null){
            this.duracion = duracion;
        } else{
            this.duracion = DURACION_POR_DEFECTO;
        }

        for (Map.Entry<TRol, Integer> entry : plantilla.entrySet()){
            this.plantillasRol.add(new PlantillaRol(this, entry.getKey(), entry.getValue()));
        }

        actualizarEstado();
    }
    
    //getters
    public String getNombreClave(){
        return nombreClave;
    }

    public String getUbicacion(){
        return ubicacion;
    }

    public long getBotin(){
        return botin;
    }

    public double getProbFracaso(){
        return probFracaso;
    }

    public int getDuracion(){
        return duracion;
    }

    public TEstado getEstado(){
        return estado;
    }

    public List<Mision> getPrerrequisitos(){
        return prerrequisitos;
    }

    public List<Mision> getDependientes(){
        return dependientes;
    }

    public List<PlantillaRol> getPlantillasRol(){
        return plantillasRol;
    }

    public List<RequisitoMaterial> getRequisitosMateriales(){
        return requisitosMateriales;
    }

    public List<Ejecucion> getEjecuciones(){
        return ejecuciones;
    }

    //setters
    public void setUbicacion(String ubicacion){
        comprobarEditable();
        this.ubicacion = ubicacion;
    }

    public void setBotin(long botin){
        comprobarEditable();
        if (botin <= 0) {
            throw new IllegalArgumentException("El botin de la mision ha de ser mayor que 0.");
        }
        this.botin = botin;
    }

    public void setProbFracaso(double probFracaso){
        comprobarEditable();
        if (probFracaso < 0.0 || probFracaso > 100.0) {
            throw new IllegalArgumentException("La probabilidad de fracaso de la mision ha de estar entre 0 y 100.");
        }
        this.probFracaso = probFracaso;
    }

    public void setDuracion(int duracion){
        comprobarEditable();
        if (duracion <= 0) {
            throw new IllegalArgumentException("La duracion de la mision ha de ser mayor que 0.");
        }
        this.duracion = duracion;
    }

    public void comprobarEditable(){
        if (tieneEjecuciones()) {
            throw new IllegalStateException("No se puede editar una mision que ya ha sido ejecutada.");
        }
    }

    public boolean tieneEjecuciones(){
        return !ejecuciones.isEmpty();
    }

    //consultas de no atributos
    public int getNumIntentos(){
        int count = 0;
        for (Ejecucion e : ejecuciones) {
            if (e.getResultado() != TResultado.EN_CURSO) {
                count++;
            }
        }
        return count;
    }

    public int getNumFracasos(){
        int count = 0;
        for (Ejecucion e : ejecuciones) {
            if (e.getResultado() == TResultado.FRACASO) {
                count++;
            }
        }
        return count;
    }

    public int getPersonalTotal(){
        int total = 0;
        for (PlantillaRol p : plantillasRol) {
            total += p.getCantidad();
        }
        return total;
    }

    public int getCantidadRol(TRol rol){
        for (PlantillaRol p : plantillasRol) {
            if (p.getRol() == rol) {
                return p.getCantidad();
            }
        }
        return 0;
    }

    public void actualizarEstado(){
        if (tieneEjecuciones()) {
            boolean exito = false;
            boolean seHaEjecutado = false;

            for (Ejecucion e : ejecuciones) {
                if (e.estaEnCurso()) {
                    this.estado = TEstado.EN_CURSO;
                    return;
                }
                if (e.getResultado() == TResultado.EXITO) {
                    exito = true;
                }
                seHaEjecutado = true;
            }

            if (exito) {
                this.estado = TEstado.COMPLETADA;
                for (Mision dep : dependientes) {
                    dep.actualizarEstado(); //se recalcula el estado de las dependientes
                }
                return;
            } else if (seHaEjecutado) {
                this.estado = TEstado.FALLIDA;
                return;
            }
        }

        for (Mision pre : prerrequisitos) {
            if (pre.getEstado() != TEstado.COMPLETADA) {
                this.estado = TEstado.BLOQUEADA;
                return;
            }
        }

        this.estado = TEstado.DISPONIBLE;
    }

    public void añadirPrerrequisito(Mision m) {
        comprobarEditable(); 

        if (m == null) {
            throw new IllegalArgumentException("El prerrequisito no puede ser nulo.");
        }
        if (m == this || m.getNombreClave().equals(this.nombreClave)) {
            throw new IllegalArgumentException("Una misión no puede depender de sí misma."); 
        }
        if (prerrequisitos.contains(m)) {
            throw new IllegalArgumentException("La misión " + m.getNombreClave() + " ya es un prerrequisito.");
        }
        if (m.dependeDe(this)) {
            throw new IllegalArgumentException("No se puede añadir el prerrequisito porque se crearía un ciclo de dependencias."); 
        }

        prerrequisitos.add(m);
        m.dependientes.add(this);
        actualizarEstado(); //se vuelve a calcular el estado
    }

    
    //comprueba si esta mision depende de la mision m para evitar ciclos
    public boolean dependeDe(Mision m) {
        Set<Mision> visitados = new HashSet<>();
        return dependeDeRec(m, visitados);
    }

    private boolean dependeDeRec(Mision objetivo, Set<Mision> visitados) {
        for (Mision pre : prerrequisitos) {
            if (pre.equals(objetivo) || pre.getNombreClave().equals(objetivo.getNombreClave())) {
                return true;
            }
            if (visitados.add(pre)) {
                if (pre.dependeDeRec(objetivo, visitados)) {
                    return true;
                }
            }
        }
        return false;
    }

    
    public void eliminarPrerrequisito(Mision m) {
        comprobarEditable(); 

        if (m == null) {
            throw new IllegalArgumentException("El prerrequisito no puede ser nulo.");
        }
        if (!prerrequisitos.contains(m)) {
            throw new IllegalArgumentException("La misión " + m.getNombreClave() + " no es un prerrequisito directo.");
        }

        prerrequisitos.remove(m);
        m.dependientes.remove(this); 
        actualizarEstado(); //se vuelve a calcular el estado
    }

    //se desvincula la mision de sus prerrequisitos
    public void desvincular() {
        if (!dependientes.isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la misión porque hay otras misiones que dependen de ella.");
        }
        if (tieneEjecuciones()) {
            throw new IllegalStateException("No se puede eliminar la misión porque ya tiene ejecuciones registradas."); 
        }

        //se elimina esta mision de la lista de dependientes de cada uno de sus prerrequisitos
        for (Mision pre : prerrequisitos) {
            pre.dependientes.remove(this);
        }
        prerrequisitos.clear();
    }

    
    //se añade un nuevo rol y cantidad a la plantilla
    public void añadirPlantillaRol(TRol rol, int cantidad) {
        comprobarEditable(); 

        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0."); 
        }

        //comprovar que no exista ya una plantilla para ese rol 
        for (PlantillaRol p : plantillasRol) {
            if (p.getRol() == rol) {
                throw new IllegalArgumentException("Ya existe una plantilla configurada para el rol: " + rol);
            }
        }

        plantillasRol.add(new PlantillaRol(this, rol, cantidad));
    }

    //se elimina un rol de la plantilla
    public void eliminarPlantillaRol(TRol rol) {
        comprobarEditable();

        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo.");
        }
        if (rol.esConductor()) {
            throw new IllegalArgumentException("No se permite eliminar el rol CONDUCTOR de la plantilla de una misión."); 
        }

        PlantillaRol aEliminar = null;
        for (PlantillaRol p : plantillasRol) {
            if (p.getRol() == rol) {
                aEliminar = p;
                break;
            }
        }

        if (aEliminar == null) {
            throw new IllegalArgumentException("El rol " + rol + " no forma parte de la plantilla.");
        }

        plantillasRol.remove(aEliminar);
    }

    public boolean puedeIniciarse(){
        return (estado == TEstado.DISPONIBLE || estado == TEstado.FALLIDA);
    }

    public List<Mision> obtenerCadenaPrerrequisitos() {
        Set<Mision> visitados = new HashSet<>();
        List<Mision> resultado = new ArrayList<>();
        obtenerCadenaPrerrequisitosRec(this, visitados, resultado);
        return resultado;
    }

    private void obtenerCadenaPrerrequisitosRec(Mision actual, Set<Mision> visitados, List<Mision> resultado) {
        for (Mision pre : actual.prerrequisitos) {
            if (visitados.add(pre)) {
                obtenerCadenaPrerrequisitosRec(pre, visitados, resultado);
                resultado.add(pre);
            }
        }
    }

    public List<Mision> obtenerDesbloqueosPotenciales() {
        List<Mision> resultado = new ArrayList<>();
        for (Mision dep : dependientes) {
            if (dep.getEstado() == TEstado.BLOQUEADA) {
                resultado.add(dep);
            }
        }
        return resultado;
    }

    public void registrarEjecucion(Ejecucion e) {
        if (e == null) {
            throw new IllegalArgumentException("La ejecución no puede ser nula.");
        }
        if (!ejecuciones.contains(e)) {
            ejecuciones.add(e);
            actualizarEstado();
        }
    }
    //PARA LA SEGUNDA ENTREGA

    public void añadirRequisitoMaterial(Objeto objeto, int cantidad) {
        throw new UnsupportedOperationException("Pendiente 2.ª entrega!");
    }

    public void eliminarRequisitoMaterial(Objeto objeto) {
        throw new UnsupportedOperationException("Pendiente 2.ª entrega!");
    }

    public Map<Objeto, Integer> materialFaltante() {
        Map<Objeto, Integer> faltante = new LinkedHashMap<>();
        for (RequisitoMaterial r : requisitosMateriales) {
            int faltan = r.unidadesFaltantes();
            if (faltan > 0) {
                faltante.put(r.getObjeto(), faltan);
            }
        }
        return faltante;
    }

    public long costeMaterialFaltante() {
        long coste = 0;
        for (RequisitoMaterial r : requisitosMateriales) {
            coste += (long) r.unidadesFaltantes() * r.getObjeto().getPrecio();
        }
        return coste;
    }
}