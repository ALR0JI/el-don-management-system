package drivers;

import main.uc.TiempoUC;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alejandro
 */
public class DriverTiempoUC {
    //Utilizamos el fichero inout colgado en la web de prop
    private static final inout io = new inout();
    private static final TiempoUC uc = new TiempoUC();

    public static void main(String[] args) throws Exception {
        io.writeln("--- Driver de TiempoUC ---");
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
            io.readln(); //descarta el resto de la linea

            try {
                switch (opcion) {
                    case 0 -> salir = true;
                    case 1 -> horaActual();
                    case 2 -> avanzarHora();
                    case 3 -> avanzarNHoras();
                    case 4 -> mostrarMenu();
                    default -> io.writeln("El número introducido no es una opción");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                //Esta línea muestra las excepciones de las propias funciones
                io.writeln("ERROR: " + e.getMessage());
            }
        }
        io.writeln("Fin del driver");
    }

    private static void mostrarMenu() throws Exception {
        io.writeln(" 0. Salir");
        io.writeln(" 1. Hora Actual");
        io.writeln(" 2. Avanzar Hora");
        io.writeln(" 3. Avanzar N Horas");
        io.writeln(" 4. Mostrar Menu");
    }
    //Funciones privadas que evitan la repetición de código:

    //Muestra las filas de una lista, una por línea, con las columnas separadas por " | ".
    private static void mostrarFilas(String cabecera, List<String[]> filas) throws Exception {
        io.writeln(cabecera);
        if (filas.isEmpty()) {
            io.writeln("  (ninguno)");
            return;
        }
        for (String[] fila : filas) {
            io.writeln(String.join(" | ", fila));
        }
    }

    //Muestra la pregunta y devuelve el número entero que escribe el usuario.
    private static int leerEntero(String pregunta) throws Exception {
        io.write(pregunta);
        int valor;
        try {
            valor = io.readint();
        } catch (NumberFormatException e) {
            io.readln();
            throw new IllegalArgumentException("Hay que escribir un número");
        }
        io.readln();
        return valor;
    }

    private static void horaActual() throws Exception {
        int hora = uc.horaActual();
        io.writeln("Hora actual obtenida: " + hora);
    }

    private static void avanzarHora() throws Exception {
        List<String[]> misionesTerminadas = uc.avanzarHora();
        io.writeln("Se ha avanzado la hora y han terminado las siguientes misiones: ");
        mostrarFilas("ID | Nombre Clave | Resultado", misionesTerminadas);
    }

    private static void avanzarNHoras() throws Exception {
        int n = leerEntero("Introduce el numero de horas a avanzar: ");
        if (n <= 0) {
            throw new IllegalArgumentException("El numero de horas debe ser mayor que 0");
        }
        List<String[]> misionesTerminadas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            misionesTerminadas.addAll(uc.avanzarHora());
        }
        io.writeln("Se han avanzado " + n + " horas y han terminado las siguientes misiones: ");
        mostrarFilas("ID | Nombre Clave | Resultado", misionesTerminadas);
    }

}
