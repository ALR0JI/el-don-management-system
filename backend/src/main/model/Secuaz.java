package main.model;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

public class Secuaz {

    public static final int HORAS_PRISION = 72;

    private final int id;
    private String nombre;
    private String apodo;
    private final TRol especialidad;
    private TEstadoSec estado;
    private long dineroAcumulado;
    private long fianza;
    private int liberacion;
    private List<Participacion> participaciones;

    public Secuaz(int id, String nombre, String apodo, TRol especialidad) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor que 0");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío");
        }
        if (especialidad == null) {
            throw new IllegalArgumentException("La especialidad no puede ser nula");
        }

        this.id = id;
        this.nombre = nombre;
        this.apodo = apodo;
        this.especialidad = especialidad;
        this.estado = TEstadoSec.LIBRE;
        this.dineroAcumulado = 0L;
        this.fianza = 0L;
        this.liberacion = 0;
        this.participaciones = new ArrayList<>();
    }

    public int getId(){
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApodo() {
        return apodo;
    }

    public TRol getEspecialidad() {
        return especialidad;
    }

    public TEstadoSec getEstado() {
        return estado;
    }

    public long getDineroAcumulado() {
        return dineroAcumulado;
    }

    public long getFianza() {
        return fianza;
    }

    public int getLiberacion() {
        return liberacion;
    }

    public List<Participacion> getParticipaciones() {
        return participaciones;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío");
        }
        this.nombre = nombre;
    }

    public void setApodo(String apodo) {
        this.apodo = apodo;
    }

    public boolean esLibre() {
        return estado == TEstadoSec.LIBRE;
    }

    public int numMisiones(){
        int contador = 0;
        for (Participacion p : participaciones) {
            if (p.getEjecucion().getResultado() != TResultado.EN_CURSO) {
                contador++;
            }
        }
        return contador;
    }

    public long calcularFianza(){
        return especialidad.getFianzaBase() + 100L * numMisiones() + Math.round(dineroAcumulado * 0.1);
    }

    //Si el secuaz no está detenido devuelve 0, si lo está pero deberia estar libre -1
    //en otro caso, devuelve el tiempo que le queda.
    public int getTiempoRestantePrision(int horaActual){
        if (estado != TEstadoSec.DETENIDO) return 0;
        else {
            int temps = liberacion - horaActual;
            if(temps > 0) return temps;
            else return -1;
        }
    }

    public boolean ocupar(){
        if(estado == TEstadoSec.LIBRE){
            estado = TEstadoSec.OCUPADO;
            return true;
        }
        return false;
    }

    public void añadirParticipacion(Participacion p){
        if (p != null) this.participaciones.add(p);
        else throw new IllegalArgumentException("La participacion no puede ser nula");
    }

    public void acumularGanancia(long monto){
        if (monto < 0) throw new IllegalArgumentException("No puedes restar ganancias");
        dineroAcumulado += monto;
    }

    public void procesarConsecuencia(TConsecuencia c, int horaActual){
        if (this.estado != TEstadoSec.OCUPADO) throw new IllegalStateException("El secuaz debe estar OCUPADO para procesar una consecuencia");
        if (c == null) throw new IllegalArgumentException("La consecuencia no puede ser nula");
        if(c == TConsecuencia.NINGUNA) estado = TEstadoSec.LIBRE;
        else if (c == TConsecuencia.DETENIDO) {
            estado = TEstadoSec.DETENIDO;
            fianza = calcularFianza();
            liberacion = horaActual + HORAS_PRISION;
        }
        else {
            estado = TEstadoSec.MUERTO;
            fianza = 0;
            liberacion = 0;
        }
    }

    public boolean comprobarLiberacionAutomatica(int horaActual) {
        if (estado == TEstadoSec.DETENIDO && horaActual >=liberacion){
            liberar();
            return true;
        }
        else return false;
    }

    public void liberar () {
        if (estado != TEstadoSec.DETENIDO) throw new IllegalStateException("El secuaz debe estar DETENIDO para liberarlo");
        estado = TEstadoSec.LIBRE;
        fianza = 0;
        liberacion = 0;
    }

    public boolean expulsar () {
        if (estado == TEstadoSec.LIBRE) {
            estado = TEstadoSec.EXPULSADO;
            return true;
        }
        return false;
    }

    public Secuaz obtenerCompliceHabitual(){
        Map<Secuaz, Integer> contadorCompaneros = new LinkedHashMap<>();
        for(Participacion p: hojaDeServicios()){
            Ejecucion ejecucion = p.getEjecucion();
            for(Participacion p2: ejecucion.getParticipaciones()){
                Secuaz companero = p2.getSecuaz();
                if (companero != null && companero.getId() != this.id) {
                    Integer value = contadorCompaneros.get(companero);
                    if (value != null) {
                        value++;
                    }
                    else value = 1;
                    contadorCompaneros.put(companero, value);
                }

            }
        }
        Secuaz complice = null;
        int maxColabos = 0;
        for (Map.Entry<Secuaz, Integer> entry : contadorCompaneros.entrySet()) {
            Secuaz companero = entry.getKey();
            Integer accesos  = entry.getValue();
            if(accesos > maxColabos){
                complice = companero;
                maxColabos = accesos;
            }
        }
        return complice;
    }

    public List<Participacion> hojaDeServicios(){
        List<Participacion> resultado = new ArrayList<Participacion>();
        for (Participacion p : participaciones) {
            if (p.getEjecucion().getResultado() != TResultado.EN_CURSO) resultado.add(p);
        }
        return resultado;
    }

}