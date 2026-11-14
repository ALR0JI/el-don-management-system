package drivers;

/**
 * @author Noa
 */
public class DriverPrincipal {

    //inout guarda en un buffer lo leido: con mas de una instancia se perderian lineas de la entrada
    static final inout io = new inout();

    public static void main(String[] args) throws Exception {
        io.writeln("=== DRIVER PRINCIPAL ===");
        mostrarMenu();

        boolean salir = false;
        while (!salir) {
            io.write("\n[Menu principal] Opcion: ");
            int opcion;
            try {
                opcion = io.readint();
            } catch (NumberFormatException e) {
                io.readln();
                io.writeln("Escribe un numero de los que aparecen como opcion");
                continue;
            }
            io.readln();

            boolean submenu = true;
            switch (opcion) {
                case 0 -> {
                    salir = true;
                    submenu = false;
                }
                case 1 -> DriverDatosUC.main(args);
                case 2 -> DriverTiempoUC.main(args);
                case 3 -> DriverSecuacesUC.main(args);
                case 4 -> DriverMisionesUC.main(args);
                case 5 -> DriverPlanificacionUC.main(args);
                case 6 -> DriverEjecucionUC.main(args);
                case 7 -> DriverSeguimientoUC.main(args);
                case 8 -> {
                    mostrarMenu();
                    submenu = false;
                }
                default -> {
                    io.writeln("El numero introducido no es una opcion valida");
                    submenu = false;
                }
            }
            if (submenu) {
                mostrarMenu();
            }
        }
        io.writeln("Fin del driver");
    }

    private static void mostrarMenu() throws Exception {
        io.writeln("\n--- Menu principal ---");
        io.writeln(" 1. Datos");
        io.writeln(" 2. Tiempo");
        io.writeln(" 3. Secuaces");
        io.writeln(" 4. Misiones");
        io.writeln(" 5. Planificacion de golpes");
        io.writeln(" 6. Ejecucion y asignacion automatica");
        io.writeln(" 7. Seguimiento");
        io.writeln(" 8. Mostrar este menu");
        io.writeln(" 0. Salir");
    }
}
