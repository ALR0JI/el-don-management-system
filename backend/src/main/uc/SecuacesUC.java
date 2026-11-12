
package main.uc;

import main.ctrl.FactoriaCtrl;
import main.ctrl.CtrlSecuaz;
import main.ctrl.CtrlCandidato;
import main.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * @author Noa
 */
public class SecuacesUC {

    public static final int MAX_EN_OFERTA = 4;

    //Devuelve el secuaz con el id indicado o lanza excepción si no existe.
    private Secuaz obtenerSecuaz(int idSecuaz) {
        Secuaz s = FactoriaCtrl.getInstance().getCtrlSecuaz().get(idSecuaz);
        if (s == null) throw new IllegalArgumentException("No existe ningún secuaz con id " + idSecuaz);
        return s;
    }

    //Devuelve el candidato con el nombre indicado o lanza excepción si no existe.
    private Candidato obtenerCandidato(String nombreCandidato) {
        Candidato c = FactoriaCtrl.getInstance().getCtrlCandidato().get(nombreCandidato);
        if (c == null) throw new IllegalArgumentException("No existe ningún candidato con nombre " + nombreCandidato);
        return c;
    }

    //Devuelve los objetos Candidato en oferta, se usa internamente.
    private static List<Candidato> candidatosEnOferta(){
        CtrlCandidato ctrlCandidato = FactoriaCtrl.getInstance().getCtrlCandidato();
        List<Candidato> candidatos = ctrlCandidato.all();
        List<Candidato> enOferta = new ArrayList<>();
        for(Candidato c: candidatos){
            if(c.isEnOferta()) enOferta.add(c);
        }
        return enOferta;
    }

    //Convierte el texto de una especialidad en TRol o lanza excepción si no existe.
    private TRol convertirRol(String especialidad) {
        if (especialidad == null) throw new IllegalArgumentException("La especialidad no puede ser nula");
        try {
            return TRol.valueOf(especialidad);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No existe la especialidad " + especialidad);
        }
    }

    //Convierte el texto de un estado en TEstadoSec o lanza excepción si no existe.
    private TEstadoSec convertirEstado(String estado) {
        if (estado == null) throw new IllegalArgumentException("El estado no puede ser nulo");
        try {
            return TEstadoSec.valueOf(estado);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No existe el estado " + estado);
        }
    }

    //Convierte un secuaz en una fila de datos: {id, nombre, apodo, especialidad, estado}
    private String[] filaSecuaz(Secuaz s) {
        return new String[] {
            String.valueOf(s.getId()),
            s.getNombre(),
            s.getApodo() == null ? "" : s.getApodo(),
            s.getEspecialidad().name(),
            s.getEstado().name()
        };
    }

    //Crea un secuaz con los parametros dados
    public int añadirSecuaz(String nombre, String apodo, String especialidad) {
        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        int nuevoId = ctrlSecuaz.siguienteId();
        Secuaz s = new Secuaz(nuevoId, nombre, apodo, convertirRol(especialidad));
        ctrlSecuaz.insertar(s);
        return nuevoId;
    }

    //Modifica el secuaz con idSecuaz y le pone los atributos marcados
    public void modificarSecuaz(int idSecuaz, String nombre, String apodo){
        Secuaz s = obtenerSecuaz(idSecuaz);
        s.setNombre(nombre);
        s.setApodo(apodo);
    }

    //Expulsa al secuaz con idSecuaz
    public boolean expulsar(int idSecuaz){
        Secuaz s = obtenerSecuaz(idSecuaz);
        return  s.expulsar();
    }

    //Libera al secuaz con idSecuaz y le cobra el dinero a Don
    public boolean pagarFianza(int idSecuaz){
        Secuaz s = obtenerSecuaz(idSecuaz);
        if(s.getEstado() != TEstadoSec.DETENIDO) throw new IllegalStateException("El secuaz debe estar DETENIDO para pagarle la fianza");
        boolean exito = Don.getInstance().pagar(TMovimiento.FIANZA, s.getFianza(),"Fianza de " + s.getNombre());
        if(exito){
            s.liberar();
            return true;
        }
        else return false;
    }

    //Devuelve la lista de secuaces filtrados por las condiciones de parametros
    //Si una condicion es "null" se ignora
    //Ej: especialidad = null y estados = "LIBRE", "OCUPADO", se cogen los libres y ocupados
    //independientemente de su especialidad
    //Cada fila: {id, nombre, apodo, especialidad, estado}
    public List<String[]> listarSecuaces(String especialidad, String... estados){
        TRol rol = null;
        if (especialidad != null) rol = convertirRol(especialidad);

        List<TEstadoSec> estadosFiltro = new ArrayList<>();
        if (estados != null) {
            for (String e : estados) estadosFiltro.add(convertirEstado(e));
        }

        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        List<String[]> resultado = new ArrayList<>();
        for(Secuaz s: ctrlSecuaz.all()){
            boolean cumpleRol = rol == null || s.getEspecialidad() == rol;
            boolean cumpleEstado = estadosFiltro.isEmpty() || estadosFiltro.contains(s.getEstado());
            if (cumpleRol && cumpleEstado) resultado.add(filaSecuaz(s));
        }
        return resultado;
    }

    //Devuelve los candidatos en oferta de la especialidad indicada
    //Cada fila: {nombre, apodo, especialidad, precioFichaje}
    public List<String[]> listarEnOfertaEspecialidad(String especialidad){
        if(especialidad == null) throw new IllegalArgumentException("La especialidad no puede ser nula");
        TRol rol = convertirRol(especialidad);

        List<String[]> enOfertaRol = new ArrayList<>();
        for(Candidato c: candidatosEnOferta()){
            if(c.getEspecialidad() == rol) {
                String[] fila = {
                    c.getNombre(),
                    c.getApodo() == null ? "" : c.getApodo(),
                    c.getEspecialidad().name(),
                    String.valueOf(c.getPrecioFichaje())
                };
                enOfertaRol.add(fila);
            }
        }
        return enOfertaRol;
    }

    //Devuelve todos los candidatos en oferta.
    //Cada fila: {nombre, apodo, especialidad, precioFichaje}
    public List<String[]> listarEnOferta(){
        List<String[]> enOferta = new ArrayList<>();
        for(Candidato c: candidatosEnOferta()){
            String[] fila = {
                c.getNombre(),
                c.getApodo() == null ? "" : c.getApodo(),
                c.getEspecialidad().name(),
                String.valueOf(c.getPrecioFichaje())
            };
            enOferta.add(fila);
        }
        return enOferta;
    }

    //Coge el candidato nombreCandidato, lo convierte en secuaz, le quita su precio a Don y lo
    //inserta en la lista de secuaces de Don
    public boolean fichar(String nombreCandidato){
        Candidato candidato = obtenerCandidato(nombreCandidato);
        if(!candidato.isEnOferta()) throw new IllegalStateException("El candidato no esta en oferta");
        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        int id = ctrlSecuaz.siguienteId();
        Secuaz secuaz = new Secuaz(id, nombreCandidato, candidato.getApodo(), candidato.getEspecialidad());
        boolean exito = Don.getInstance().pagar(TMovimiento.FICHAJE,candidato.getPrecioFichaje(),"Fichaje de " + nombreCandidato);
        if(!exito) return false;
        candidato.marcarFichado();
        ctrlSecuaz.insertar(secuaz);
        return true;
    }

    //Cada vez que se llame a esta funcion se pone en oferta a nuevos candidatos
    public static void renovarOferta(){
        List<Candidato> candidatosOferta = candidatosEnOferta();
        for(Candidato c : candidatosOferta){
            c.retirarDeOferta();
        }
        List<Candidato> candidatosElegibles = new ArrayList<>();
        CtrlCandidato ctrlCandidato = FactoriaCtrl.getInstance().getCtrlCandidato();
        List<Candidato> candidatos = ctrlCandidato.all();
        for(Candidato c :candidatos){
            if(c.esElegible()) candidatosElegibles.add(c);
        }
        Collections.shuffle(candidatosElegibles, Azar.getInstance());
        int cantidad = Math.min(MAX_EN_OFERTA, candidatosElegibles.size());

        for (int i = 0; i < cantidad; i++) {
            candidatosElegibles.get(i).ponerEnOferta();
        }
    }


}
