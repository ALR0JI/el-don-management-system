package main.uc;

import main.ctrl.CtrlEjecucion;
import main.ctrl.CtrlSecuaz;
import main.ctrl.FactoriaCtrl;
import main.model.Azar;
import main.model.Ejecucion;
import main.model.Reloj;
import main.model.Secuaz;

import java.util.ArrayList;
import java.util.List;

public class TiempoUC {
    private static final int HORAS_RENOVACION_TIENDA = 48;
    private final CtrlEjecucion ctrlEjecucion;
    private final CtrlSecuaz ctrlSecuaz;

    public TiempoUC() {
        FactoriaCtrl factoriaCtrl = FactoriaCtrl.getInstance();
        ctrlEjecucion = factoriaCtrl.getCtrlEjecucion();
        ctrlSecuaz = factoriaCtrl.getCtrlSecuaz();
    }

    public int horaActual() {
        return Reloj.getInstance().horaActual();
    }

    public List<String[]> avanzarHora() {
        // Avanza la hora
        Reloj reloj = Reloj.getInstance();
        reloj.avanzar();

        // Actualiza estado de las ejecuciones
        int hora = reloj.horaActual();
        List<String[]> terminadas = new ArrayList<>();
        for (Ejecucion e : ctrlEjecucion.all()) {
            if (e.estaEnCurso() && e.getFin() <= hora) {
                e.finalizar(hora, Azar.getInstance());
                String[] ejecucionString = {String.valueOf(e.getId()), e.getMision().getNombreClave(), e.getResultado().name()};
                terminadas.add(ejecucionString);
            }
        }

        for (Secuaz s : ctrlSecuaz.all()) {
            s.comprobarLiberacionAutomatica(hora);
        }

        if (hora%HORAS_RENOVACION_TIENDA == 0) {
            SecuacesUC.renovarOferta();
            //ALEJANDRO: Hay que revisar si hacerlo asi
            // PD: Llamando a renovarOferta() he tenido que hacer estatica esta funcion y candidatosEnOferta
        }

        return terminadas;
    }
}