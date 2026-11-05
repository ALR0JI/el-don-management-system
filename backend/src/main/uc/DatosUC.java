package main.uc;

import main.model.DatosSecuaz;
import main.model.ResumenImportacion;
import main.model.TRol;
import main.ctrl.FactoriaCtrl;
import main.ctrl.CtrlFichero;
import main.ctrl.CtrlMision;
import main.ctrl.CtrlSecuaz;
import main.ctrl.CtrlCandidato;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatosUC {
    private final CtrlFichero ctrlFichero;
    private final CtrlMision ctrlMision;
    private final CtrlSecuaz ctrlSecuaz;
    private final CtrlCandidato ctrlCandidato;

    public DatosUC() {
        FactoriaCtrl factoria = FactoriaCtrl.getInstance();
        this.ctrlFichero = factoria.getCtrlFichero();
        this.ctrlMision = factoria.getCtrlMision();
        this.ctrlSecuaz = factoria.getCtrlSecuaz();
        this.ctrlCandidato = factoria.getCtrlCandidato();
    }
    public ResumenImportacion<DatosSecuaz> leerSecuaces(String ruta) {
        List<String> lineas = ctrlFichero.leerLineas(ruta);
        List<DatosSecuaz> datos = new ArrayList<>();
        List<String> avisos = new ArrayList<>();

        if (lineas.isEmpty()) {
            avisos.add("El fichero esta vacio");
        }

        for (int i = 0; i < lineas.size(); i++) {
            int numLinea = i + 1;
            String linea = lineas.get(i).trim();    // con trim() se eliminan espacios al inicio y al final para detectar lineas vacias

            if (linea.isEmpty()) {
                avisos.add("Linea " + numLinea + ": esta vacia");
                continue;
            }

            int posUltimoEspacio = linea.lastIndexOf(' ');  // Posicion del ultimo espacio
            if (posUltimoEspacio == -1) {                       // Si es -1 quiere decir que solo hay una palabra
                avisos.add("Linea " + numLinea + ": falta el nombre o el rol");
                continue;
            }

            String nombre = linea.substring(0, posUltimoEspacio).trim();        // de inicio al ultimo espacio es el nombre
            String textoRol = linea.substring(posUltimoEspacio + 1);  // el texto posterior es el rol

            TRol rol;
            try {
                rol = TRol.valueOf(textoRol.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {                      // Si hay una excepcion se recoge para meterla en avisos
                avisos.add("Linea " + numLinea + ": rol invalido");
                continue;
            }

            datos.add(new DatosSecuaz(nombre, rol));
        }

        return new ResumenImportacion<DatosSecuaz>(datos, avisos);
    }
}