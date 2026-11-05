package main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

public class Ejecucion {

    public static final double PROB_INCIDENTE_BASE = 40;


    private int id;
    private Mision mision;
    private TEjecucion tipo;
    private int inicio;
    private int fin;
    private double probExito;
    private TResultado resultado;
    private boolean leida;
    private String materialPerdido;
    private List<Participacion> participaciones;

    private Ejecucion (int id, Mision mision, TEjecucion tipo, int inicio) {

        if (id <= 0) {
                throw new IllegalArgumentException("El id de la ejecucion debe ser mayor que 0");
        }
        if (mision == null) {
                throw new IllegalArgumentException("La mision no puede ser nula");
        }
        if (tipo == null) {
                throw new IllegalArgumentException("El tipo de ejecucion no puede ser nulo");
        }
        if (inicio < 0) {
                throw new IllegalArgumentException("La hora de inicio no puede ser negativa");
        }

        this.id = id;
        this.mision = mision;
        this.tipo = tipo;
        this.inicio = inicio;
        this.fin = inicio + mision.getDuracion();
        this.resultado = TResultado.EN_CURSO;
        this.leida = false;
        this.materialPerdido = "";
        this.participaciones = new ArrayList<>();
    }

    public static Ejecucion iniciar(int id, Mision mision, TEjecucion tipo,
                                    int horaActual, Map<TRol, List<Secuaz>> asignacion) {
        // ---- Validaciones ----
        if (!mision.puedeIniciarse()) {
            throw new IllegalStateException("La mision no se puede iniciar");
        }

        for (PlantillaRol pr : mision.getPlantillasRol()) {
            List<Secuaz> equipo = asignacion.get(pr.getRol());
            if (equipo == null || equipo.size() != pr.getCantidad()) {
                throw new IllegalArgumentException("Puesto mal cubierto: " + pr.getRol());
            }
        }
        for (TRol rol : asignacion.keySet()) {
            if (mision.getCantidadRol(rol) == 0) {
                throw new IllegalArgumentException("Puesto que no esta en la plantilla: " + rol);
            }
        }

        Set<Secuaz> vistos = new HashSet<>();
        for (List<Secuaz> equipo : asignacion.values()) {
            for (Secuaz s : equipo) {
                if (s == null || !vistos.add(s)) {
                    throw new IllegalArgumentException("Secuaz nulo o repetido");
                }
                if (!s.esLibre()) {
                    throw new IllegalStateException("El secuaz " + s.getId() + " no esta libre");
                }
            }
        }

        for (RequisitoMaterial r : mision.getRequisitosMateriales()) {
            if (!r.esSuficienteEnAlmacen()) {
                throw new IllegalStateException("Falta material: " + r.getObjeto().getNombre());
            }
        }

        // ---- Cambios (solo si todo lo anterior ha ido bien) ----
        Ejecucion e = new Ejecucion(id, mision, tipo, horaActual);
        e.probExito = calcularProbabilidadExito(mision, asignacion);

        for (Map.Entry<TRol, List<Secuaz>> puesto : asignacion.entrySet()) {
            for (Secuaz s : puesto.getValue()) {
                Participacion p = new Participacion(s, e, puesto.getKey());
                e.participaciones.add(p);
                s.ocupar();
            }
        }
        for (RequisitoMaterial r : mision.getRequisitosMateriales()) {
            r.getObjeto().descontar(r.getCantidad());
        }
        //mision.registrarEjecucion(e);
        return e;
    }

    public static double calcularProbabilidadExito(Mision mision,
                                                   Map<TRol, List<Secuaz>> asignacion) {
        int n = mision.getPersonalTotal();
        int m = 0;
        for (Map.Entry<TRol, List<Secuaz>> puesto : asignacion.entrySet()) {
            for (Secuaz s : puesto.getValue()) {
                if (s.getEspecialidad() == puesto.getKey()) {
                    m++;
                }
            }
        }
        return (double) m / n * (100 - mision.getProbFracaso());
    }

    public int getId() {return id; }
    public Mision getMision() {return mision; }
    public TEjecucion getTipo() {return tipo; }
    public int getInicio() {return inicio; }
    public int getFin() {return fin;}
    public double getProbExito() {return probExito; }
    public TResultado getResultado() { return resultado; }
    public boolean isLeida() { return leida; }
    public String getMaterialPerdido() { return materialPerdido; }
    public List<Participacion> getParticipaciones() {
        return Collections.unmodifiableList(participaciones);
    }

    public boolean estaEnCurso() {
        return resultado == TResultado.EN_CURSO;
    }

    public int getTiempoRestante(int horaActual) {
        return Math.max(0, fin - horaActual);
    }

    public void finalizar(int horaActual, Random random) {
        if (resultado != TResultado.EN_CURSO) {
            throw new IllegalStateException("La ejecucion ya ha terminado");
        }
        if (horaActual < fin) {
            throw new IllegalStateException("La ejecucion aun no ha llegado a su fin");
        }

        // 1. Probabilidad de incidente de cada participante (ANTES de cambiar el resultado)
        double[] probIncidente = new double[participaciones.size()];
        for (int i = 0; i < participaciones.size(); i++) {
            Secuaz s = participaciones.get(i).getSecuaz();
            probIncidente[i] = PROB_INCIDENTE_BASE / (1 + 0.1 * s.numMisiones());
        }

        // 2. Tirada de exito
        boolean exito = random.nextDouble() * 100 < probExito;
        resultado = exito ? TResultado.EXITO : TResultado.FRACASO;

        // 3. Material
        StringBuilder perdido = new StringBuilder();
        for (RequisitoMaterial r : mision.getRequisitosMateriales()) {
            Objeto objeto = r.getObjeto();
            int perdidas = objeto.calcularUnidadesPerdidas(r.getCantidad(), exito, random);
            if (r.getCantidad() - perdidas > 0) {
                objeto.añadir(r.getCantidad() - perdidas);
            }
            if (perdidas > 0) {
                if (perdido.length() > 0) perdido.append(", ");
                perdido.append(objeto.getNombre()).append(" x ").append(perdidas);
            }
        }
        materialPerdido = perdido.toString();

        // 4 y 5. Reparto o consecuencias
        if (exito) {
            long total = 0;
            for (Participacion p : participaciones) {
                total += p.liquidarCobro(mision.getBotin());
            }
            Don.getInstance().registrarComision(mision.getBotin() - total,
                    "Comisión " + mision.getNombreClave());
            for (Participacion p : participaciones) {
                p.aplicarConsecuencia(TConsecuencia.NINGUNA, horaActual);
            }
        } else {
            for (int i = 0; i < participaciones.size(); i++) {
                Participacion p = participaciones.get(i);
                TConsecuencia c = TConsecuencia.NINGUNA;
                if (random.nextDouble() * 100 < probIncidente[i]) {
                    c = p.getSecuaz().getEspecialidad()
                            .decidirConsecuencia(random.nextDouble() * 100);
                }
                p.aplicarConsecuencia(c, horaActual);
            }
        }

        // 6. Actualizar el estado de la mision
        mision.actualizarEstado();
    }


    public int contarConsecuencias(TConsecuencia c) {
        int cuenta = 0;
        for (Participacion p : participaciones) {
            if (p.getConsecuencia() == c) {
                cuenta++;
            }
        }
        return cuenta;
    }

    public long getTotalCobrado() {
        long total = 0;
        for (Participacion p : participaciones) {
            total += p.getCobrado();
        }
        return total;
    }

    public long getComisionDon() {
        if (resultado == TResultado.EXITO) {
            return mision.getBotin() - getTotalCobrado();
        }
        return 0;
    }

    public void marcarComoLeida() {
        if (!estaEnCurso()) {
            leida = true;
        }
    }


    //POL: Estas dos funciones ya estaban en el esqueleto, no se si son para que funcione otro programa pero yo no las he usado para nada (tampoco estan en el funciones.md)
    public void setResultado(TResultado resultado) {
        throw new UnsupportedOperationException("Pendiente 2.ª entrega");
    }


    public void añadirParticipacion(Participacion participacion) {
        throw new UnsupportedOperationException("Pendiente 2.ª entrega");
    }

}
