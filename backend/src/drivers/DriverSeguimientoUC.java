package drivers;

import main.uc.SeguimientoUC;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alejandro
 */
public class DriverSeguimientoUC {
    // Constantes
    private static final String CABECERA_EJECUCION =
            "ID | Nombre | Tipo | Inicio | Fin | Tiempo Restante | Resultado | Leida "
                    + "| Botin | Total Cobrado | Comision Don | Num Muertos | Num Detenidos "
                    + "| Material Perdido | Probabilidad Exito";

    private static final String CABECERA_MOVIMIENTO =
            "ID | Momento | Tipo | Concepto | Importe";

    private static final String CABECERA_SECUAZ =
            "ID | Nombre | Apodo | Especialidad | Estado | Num Misiones";

    private static final String CABECERA_MISION =
            "Nombre Clave | Ubicacion | Botin | Estado | Num Intentos | Num Fracasos";

    //Utilizamos el fichero inout colgado en la web de prop
    private static final inout io = new inout();
    private static final SeguimientoUC uc = new SeguimientoUC();

    public static void main(String[] args) throws Exception {
        io.writeln("--- Driver de SeguimientoUC ---");
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
                    case 1 -> consultarCaja();
                    case 2 -> consultarMovimientos();
                    case 3 -> listarEjecucionesEnCurso();
                    case 4 -> consultarHistorial();
                    case 5 -> numeroNoLeidas();
                    case 6 -> abrirNotificaciones();
                    case 7 -> estadoDeCuentas();
                    case 8 -> obtenerCompliceHabitual();
                    case 9 -> hojaDeServicios();
                    case 10 -> top3Operativos();
                    case 11 -> top3GolpesHistoricos();
                    case 12 -> top3PuntosCalientes();
                    case 13 -> mostrarMenu();
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
        io.writeln(" 1. Consultar Caja");
        io.writeln(" 2. Consultar Movimientos");
        io.writeln(" 3. Listar Ejecuciones En Curso");
        io.writeln(" 4. Consultar Historial");
        io.writeln(" 5. Numero No Leidas");
        io.writeln(" 6. Abrir Notificaciones");
        io.writeln(" 7. Estado de Cuentas");
        io.writeln(" 8. Obtener Complice Habitual");
        io.writeln(" 9. Hoja de Servicios");
        io.writeln(" 10. Top 3 Operativos");
        io.writeln(" 11. Top 3 Golpes Historicos");
        io.writeln(" 12. Top 3 Puntos Calientes");
        io.writeln(" 13. Mostrar Menu");
    }

    //Funciones privadas que evitan la repetición de código:

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

    private static void consultarCaja() throws Exception {
        long caja = uc.consultarCaja();
        io.writeln("El valor de caja es: " + caja);
    }

    private static void consultarMovimientos() throws Exception {
        List<String[]> movimientos = uc.consultarMovimientos();
        mostrarFilas(CABECERA_MOVIMIENTO, movimientos);
    }

    private static void listarEjecucionesEnCurso() throws Exception {
        List<String[]> ejecuciones = uc.listarEjecucionesEnCurso();
        mostrarFilas(CABECERA_EJECUCION, ejecuciones);
    }

    private static void consultarHistorial() throws Exception {
        List<String[]> misionesHistorial = uc.consultarHistorial();
        mostrarFilas(CABECERA_EJECUCION, misionesHistorial);
    }

    private static void numeroNoLeidas() throws Exception {
        int num = uc.numeroNoLeidas();
        io.writeln("Hay " + num + " Ejecuciones no leidas");
    }

    private static void abrirNotificaciones() throws Exception {
        List<String[]> notificaciones = uc.abrirNotificaciones();
        io.writeln("Notificaciones (antes de marcarlas como leidas): ");
        mostrarFilas(CABECERA_EJECUCION, notificaciones);
    }

    private static void estadoDeCuentas() throws Exception{
        int idSecuaz = leerEntero("Introduce el id del Secuaz: ");
        long dinero = uc.estadoDeCuentas(idSecuaz);
        io.writeln("El Secuaz con id: " + idSecuaz + " tiene " + dinero + " euros");
    }

    private static void obtenerCompliceHabitual() throws Exception {
        int idSecuaz = leerEntero("Introduce el id del Secuaz: ");
        String[] complice = uc.obtenerCompliceHabitual(idSecuaz);
        if (complice == null) {
            io.writeln("El secuaz " + idSecuaz + " no tiene complice habitual");
        }
        else {
            List<String[]> compliceEnFormato = new ArrayList<>();       // De esta forma se puede usar la funcion mostrarFilas()
            compliceEnFormato.add(complice);
            mostrarFilas(CABECERA_SECUAZ, compliceEnFormato);
        }
    }

    private static void hojaDeServicios() throws Exception {
        int idSecuaz = leerEntero("Introduce el id del Secuaz: ");
        List<String[]> participaciones = uc.hojaDeServicios(idSecuaz);
        mostrarFilas("ID | Nombre Clave | Resultado | Rol Desempeñado | Cobrado | Consecuencia" , participaciones);
    }

    private static void top3Operativos() throws Exception {
        List<String[]> secuaces = uc.top3Operativos();
        mostrarFilas(CABECERA_SECUAZ, secuaces);
    }

    private static void top3GolpesHistoricos() throws Exception {
        List<String[]> misiones = uc.top3GolpesHistoricos();
        mostrarFilas(CABECERA_MISION ,misiones);
    }

    private static void top3PuntosCalientes() throws Exception {
        List<String[]> misiones = uc.top3PuntosCalientes();
        mostrarFilas(CABECERA_MISION ,misiones);
    }

}
