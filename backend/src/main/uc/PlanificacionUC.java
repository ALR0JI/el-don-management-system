package main.uc;

import main.model.Mision;
import main.ctrl.CtrlMision;

import java.util.ArrayList;
import java.util.List;

public class PlanificacionUC {

    private final Planificador planificador;
    private final CtrlMision ctrlMision;

    public PlanificacionUC(Planificador planificador, CtrlMision ctrlMision) {
        if (planificador == null || ctrlMision == null) {
            throw new IllegalArgumentException("Ninguno de los parametros de entrada pueden ser nulos.");
        }
        this.planificador = planificador;
        this.ctrlMision = ctrlMision;
    }

    //convierte un objeto Mision en una fila de strings: {nombreClave, ubicacion, botin, probFracaso, duracion, estado}
    private String[] filaMision(Mision m) {
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

    //llama al algoritmo planificador secuencial
    public List<String[]> planificarEstrategiaGlobal() {
        List<Mision> todasLasMisiones = ctrlMision.all();
        List<Mision> planOrdenado = planificador.planificarGolpes(todasLasMisiones);

        List<String[]> resultado = new ArrayList<>();
        for (Mision m : planOrdenado) {
            resultado.add(filaMision(m));
        }

        return resultado;
    }
}