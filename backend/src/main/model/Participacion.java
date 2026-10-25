
package main.model;

public class Participacion {

    private Secuaz secuaz;
    private Ejecucion ejecucion;
    private TRol rolDesempenado;
    private long cobrado;
    private TConsecuencia consecuencia;

    public Participacion(Secuaz secuaz, Ejecucion ejecucion, TRol rolDesempenado) {
        if (secuaz == null) throw new IllegalArgumentException("El secuaz no puede ser nulo");
        if (ejecucion == null) throw new IllegalArgumentException("La ejecucion no puede ser nula");
        if (rolDesempenado == null) throw new IllegalArgumentException("El rol no puede ser nulo");

        this.secuaz = secuaz;
        this.ejecucion = ejecucion;
        this.rolDesempenado = rolDesempenado;

        cobrado = 0;
        consecuencia = TConsecuencia.NINGUNA;

        secuaz.añadirParticipacion(this);
    }

    public Secuaz getSecuaz() {
        return secuaz;
    }

    public Ejecucion getEjecucion() {
        return ejecucion;
    }

    public TRol getRolDesempenado() {
        return rolDesempenado;
    }

    public long getCobrado() {
        return cobrado;
    }

    public TConsecuencia getConsecuencia() {
        return consecuencia;
    }

    public boolean esEnSuEspecialidad(){
        return secuaz.getEspecialidad() == rolDesempenado;
    }

    public long liquidarCobro(long botin) {
        if (ejecucion.getResultado() != TResultado.EXITO) throw new IllegalStateException("La misión debe haber acabado con exito");
        double porcentajeComision = rolDesempenado.getComision();
        cobrado = (long) ((botin * rolDesempenado.getComision()) / 100);
        secuaz.acumularGanancia(cobrado);
        return cobrado;

    }

    public void aplicarConsecuencia(TConsecuencia c, int horaActual){
        if(c == null) throw new IllegalArgumentException("La consecuencia no puede ser nula");
        if(ejecucion.getResultado() == TResultado.EN_CURSO) throw new IllegalStateException("La misión debe haber acabado");
        if(c != TConsecuencia.NINGUNA && ejecucion.getResultado() != TResultado.FRACASO) throw new IllegalArgumentException("La consecuencia negativa solo se aplica en ejecuciones de FRACASO");

        consecuencia = c;
         secuaz.procesarConsecuencia(c, horaActual);
    }

}
