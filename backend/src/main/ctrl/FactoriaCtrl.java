package main.ctrl;


public class FactoriaCtrl {
    private static FactoriaCtrl instance;

    private CtrlMision ctrlMision;
    private CtrlSecuaz ctrlSecuaz;
    private CtrlEjecucion ctrlEjecucion;
    private CtrlObjeto ctrlObjeto;
    private CtrlCandidato ctrlCandidato;
    private CtrlFichero ctrlFichero;

    private FactoriaCtrl() {}

    public static  FactoriaCtrl getInstance() {
        if (instance == null) instance = new FactoriaCtrl();
        return instance;
    }

    public CtrlMision getCtrlMision() {
        if (ctrlMision == null) {
            ctrlMision = new CtrlMision();
        }
        return ctrlMision;
    }

    public CtrlSecuaz getCtrlSecuaz() {
        if (ctrlSecuaz == null) {
            ctrlSecuaz = new CtrlSecuaz();
        }
        return ctrlSecuaz;
    }

    public CtrlEjecucion getCtrlEjecucion() {
        if (ctrlEjecucion == null) {
            ctrlEjecucion = new CtrlEjecucion();
        }
        return ctrlEjecucion;
    }

    public CtrlObjeto getCtrlObjeto() {
        if (ctrlObjeto == null) {
            ctrlObjeto = new CtrlObjeto();
        }
        return ctrlObjeto;
    }

    public CtrlCandidato getCtrlCandidato() {
        if (ctrlCandidato == null) {
            ctrlCandidato = new CtrlCandidato();
        }
        return ctrlCandidato;
    }

    public CtrlFichero getCtrlFichero() {
        if (ctrlFichero == null) {
            ctrlFichero = new CtrlFichero();
        }
        return ctrlFichero;
    }

    public static void reiniciar() {
        instance = null;
        // ALEJANDRO: no hace falta hacer delete como en c++ por el trash collector de Java
    }
}