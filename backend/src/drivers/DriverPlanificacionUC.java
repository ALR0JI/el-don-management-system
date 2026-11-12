package drivers;

import java.util.List;

import main.ctrl.FactoriaCtrl;
import main.uc.PlanificacionUC;
import main.uc.Planificador;

/**
 * @author Adrià 
*/
public class DriverPlanificacionUC {

    private static final inout io = new inout();
    private static final PlanificacionUC uc = new PlanificacionUC(
        new Planificador(), 
        FactoriaCtrl.getInstance().getCtrlMision()
    );

    public static void main(String[] args) throws Exception {
        io.writeln("--- Driver de PlanificacionUC ---");
        mostrarMenu();

        boolean salir = false;
        while (!salir) {
            io.write("\nOpcion: ");
            int opcion;
            try {
                opcion = io.readint();
            } catch (NumberFormatException e) {
                io.readln();
                io.writeln("Escribe un numero de los que aparecen como opción");
                continue;
            }
            io.readln(); // descarta el resto de la linea

            try {
                switch (opcion) {
                    case 0 -> salir = true;
                    case 1 -> planificarEstrategiaGlobal();
                    case 2 -> mostrarMenu();
                    default -> io.writeln("El número introducido no es una opción válida");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                io.writeln("ERROR: " + e.getMessage());
            }
        }
        io.writeln("Fin del driver");
    }

    // opcion 2
    private static void mostrarMenu() throws Exception {
        io.writeln(" 1. Planificar estrategia global de misiones");
        io.writeln(" 2. Mostrar este menú");
        io.writeln(" 0. Salir");
    }

    private static void mostrarFilas(String cabecera, List<String[]> filas) throws Exception {
        io.writeln(cabecera);
        if (filas.isEmpty()) {
            io.writeln("  (ninguna misión registrada para planificar)");
            return;
        }
        int orden = 1;
        for (String[] fila : filas) {
            io.writeln("  #" + orden + " -> " + String.join(" | ", fila));
            orden++;
        }
    }
     // 1
    private static void planificarEstrategiaGlobal() throws Exception {
        List<String[]> plan = uc.planificarEstrategiaGlobal();
        mostrarFilas("Secuencia optima de ejecución (nombreClave | ubicacion | botin | probFracaso | duracion | estado):", plan);
    }
}