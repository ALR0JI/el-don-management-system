package drivers;

import main.uc.DatosUC;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alejandro
 */
public class DriverDatosUC {
    // Constantes
    private static final String RUTA_SECUACES_DEFAULT = "ejemplos/secuaces_enunciado.txt";
    private static final String RUTA_MISIONES_DEFAULT = "ejemplos/misiones_driver.txt";

    //Utilizamos el fichero inout colgado en la web de prop
    private static final inout io = new inout();
    private static final DatosUC uc = new DatosUC();

    public static void main(String[] args) throws Exception {
        io.writeln("--- Driver de DatosUC ---");
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
                    case 1 -> leerSecuaces();
                    case 2 -> confirmarSecuaces();
                    case 3 -> leerMisiones();
                    case 4 -> confirmarMisiones();
                    case 5 -> empezarPartida();
                    case 6 -> mostrarMenu();
                    default -> io.writeln("El número introducido no es una opción");
                }
            } catch (IllegalArgumentException | IllegalStateException | UncheckedIOException e) { // Asi un fichero mal escrito no tumba el driver
                //Esta línea muestra las excepciones de las propias funciones
                io.writeln("ERROR: " + e.getMessage());
            }
        }
        io.writeln("Fin del driver");
    }

    private static void mostrarMenu() throws Exception {
        io.writeln(" 0. Salir");
        io.writeln(" 1. Leer Secuaces");
        io.writeln(" 2. Confirmar Secuaces");
        io.writeln(" 3. Leer Misiones");
        io.writeln(" 4. Confirmar Misiones");
        io.writeln(" 5. Empezar Partida");
        io.writeln(" 6. Mostrar Menu");
    }

    //Funciones privadas que evitan la repetición de código:

    //Muestra la pregunta y devuelve la línea que escribe el usuario.
    private static String leerTexto(String pregunta) throws Exception {
        io.write(pregunta);
        return io.readline().trim();
    }

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

    //Muestra los avisos de las lineas invalidas de un fichero (si los hay).
    private static void mostrarAvisos(List<String> avisos) throws Exception {
        if (avisos.isEmpty()) {
            return;
        }
        io.writeln("Avisos:");
        for (String aviso : avisos) {
            io.writeln("  " + aviso);
        }
    }

    private static void leerSecuaces() throws Exception {
        String ruta = leerTexto("Introduce la ruta del archivo: ");
        List<String> avisos = new ArrayList<>();
        List<String[]> filas = uc.leerSecuaces(ruta, avisos);
        mostrarFilas("Nombre | Rol", filas);
        mostrarAvisos(avisos);
    }

    private static void confirmarSecuaces() throws Exception {
        String ruta = leerTexto("Introduce la ruta del archivo a confirmar ('Default' para la ruta por defecto): ");
        List<String> avisos = new ArrayList<>();
        List<String[]> filas;
        if (ruta.equals("Default")) {
            filas = uc.leerSecuaces(RUTA_SECUACES_DEFAULT, avisos);
        }
        else {
            filas = uc.leerSecuaces(ruta, avisos);
        }
        mostrarAvisos(avisos);
        uc.confirmarSecuaces(filas);
        io.writeln("Se han confirmado los secuaces");
    }

    private static void leerMisiones() throws Exception {
        String ruta = leerTexto("Introduce la ruta del archivo: ");
        List<String> avisos = new ArrayList<>();
        List<String[]> filas = uc.leerMisiones(ruta, avisos);
        mostrarFilas("Nombre | Botin | Plantilla | Probabilidad | Dependencias", filas);
        mostrarAvisos(avisos);
    }

    private static void confirmarMisiones() throws Exception {
        String ruta = leerTexto("Introduce la ruta del archivo a confirmar ('Default' para la ruta por defecto): ");
        List<String> avisos = new ArrayList<>();
        List<String[]> filas;
        if (ruta.equals("Default")) {
            filas = uc.leerMisiones(RUTA_MISIONES_DEFAULT, avisos);
        }
        else {
            filas = uc.leerMisiones(ruta, avisos);
        }
        mostrarAvisos(avisos);
        uc.confirmarMisiones(filas);
        io.writeln("Se han confirmado las misiones");
    }

    private static void empezarPartida() throws Exception {
        io.writeln("(ERROR: Esta funcionalidad es de la segunda entrega)");
    }
}
