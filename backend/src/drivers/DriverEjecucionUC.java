package drivers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.model.Azar;
import main.uc.EjecucionUC;

/**
 * @author Pol
 */


public class DriverEjecucionUC {

    private static final inout io = DriverPrincipal.io;
    private static final EjecucionUC uc = new EjecucionUC();

    public static void main(String[] args) throws Exception {
        io.writeln("--- Driver de EjecucionUC ---");
        mostrarMenu();

        boolean salir = false;
        while (!salir) {
            io.write("\nOpcion: ");
            int opcion;
            try {
                opcion = io.readint();
            } catch (NumberFormatException e) {
                io.readln();
                io.writeln("Escribe un numero de los que aparecen como opcion");
                continue;
            }
            io.readln(); //descarta el resto de la linea

            try {
                switch (opcion) {
                    case 0 -> salir = true;
                    case 1 -> previsualizarProbabilidad();
                    case 2 -> comprarMaterialFaltante();
                    case 3 -> ejecutarMision();
                    case 4 -> proponerEjecucionAutomatica();
                    case 5 -> confirmarEjecucionAutomatica();
                    case 6 -> fijarSemilla();
                    case 7 -> {
                        mostrarMenu();
                        continue;
                    }
                    default -> {
                        io.writeln("El numero introducido no es una opcion");
                        continue;
                    }
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                //Errores del dominio: se muestra el mensaje y se sigue con la siguiente opcion
                io.writeln("ERROR: " + e.getMessage());
            }              catch (RuntimeException e) {
                io.writeln("ERROR INESPERADO: " + e);
                e.printStackTrace(System.out);   // temporal: mostra la classe i la línia exactes
            }
            salir = true;
        }
        io.writeln("Volviendo al Driver Principal");
    }

    private static void mostrarMenu() throws Exception {
        io.writeln(" 1. Previsualizar probabilidad de exito de un equipo");
        io.writeln(" 2. Comprar material faltante de una mision");
        io.writeln(" 3. Ejecutar mision manualmente");
        io.writeln(" 4. Proponer ejecucion automatica");
        io.writeln(" 5. Confirmar ejecucion automatica");
        io.writeln(" 6. Fijar semilla del azar");
        io.writeln(" 7. Mostrar este menu");
        io.writeln(" 0. Salir");
    }

    //Funciones privadas que evitan la repeticion de codigo:

    //Muestra la pregunta y devuelve la linea que escribe el usuario.
    private static String leerTexto(String pregunta) throws Exception {
        io.write(pregunta);
        return io.readline().trim();
    }

    //Muestra la pregunta y devuelve el numero entero que escribe el usuario.
    private static int leerEntero(String pregunta) throws Exception {
        io.write(pregunta);
        int valor;
        try {
            valor = io.readint();
        } catch (NumberFormatException e) {
            io.readln();
            throw new IllegalArgumentException("Hay que escribir un numero");
        }
        io.readln();
        return valor;
    }

    //Lee un equipo con el formato: ROL id id ; ROL id   (p. ej. CONDUCTOR 1 ; ASALTO 3 4)
    private static Map<String, List<Integer>> leerAsignacion() throws Exception {
        String texto = leerTexto("Equipo (formato: ROL id id ; ROL id ... p. ej. CONDUCTOR 1 ; ASALTO 3 4): ");
        Map<String, List<Integer>> asignacion = new LinkedHashMap<>();
        if (texto.isEmpty()) return asignacion;

        for (String puesto : texto.split(";")) {
            String[] palabras = puesto.trim().split("\\s+");
            List<Integer> ids = new ArrayList<>();
            for (int i = 1; i < palabras.length; i++) {
                ids.add(Integer.parseInt(palabras[i]));   // NumberFormatException es un IllegalArgumentException
            }
            asignacion.put(palabras[0].toUpperCase(), ids);
        }
        return asignacion;
    }

    private static void mostrarFilas(String cabecera, List<String[]> filas) throws Exception {
        io.writeln(cabecera);
        if (filas.isEmpty()) {
            io.writeln("  (ninguno)");
            return;
        }
        for (String[] fila : filas) {
            io.writeln("  " + String.join(" | ", fila));
        }
    }

    private static void mostrarAvisos(List<String> avisos) throws Exception {
        if (avisos.isEmpty()) {
            io.writeln("Sin avisos");
            return;
        }
        for (String aviso : avisos) {
            io.writeln("AVISO: " + aviso);
        }
    }

    //Opcion 1: previsualizarProbabilidad(nombreClave, asignacion)
    private static void previsualizarProbabilidad() throws Exception {
        String nombre = leerTexto("Nombre clave de la mision: ");
        Map<String, List<Integer>> asignacion = leerAsignacion();

        double probabilidad = uc.previsualizarProbabilidad(nombre, asignacion);
        io.writeln("Probabilidad de exito: " + String.format(java.util.Locale.ROOT, "%.2f", probabilidad) + " %");
    }

    //Opcion 2: comprarMaterialFaltante(nombreClave)
    private static void comprarMaterialFaltante() throws Exception {
        String nombre = leerTexto("Nombre clave de la mision: ");

        boolean exito = uc.comprarMaterialFaltante(nombre);
        if (exito) io.writeln("La mision tiene todo el material necesario");
        else io.writeln("Don no tiene suficiente dinero para comprar el material que falta");
    }

    //Opcion 3: ejecutarMision(nombreClave, asignacion)
    private static void ejecutarMision() throws Exception {
        String nombre = leerTexto("Nombre clave de la mision: ");
        Map<String, List<Integer>> asignacion = leerAsignacion();

        int id = uc.ejecutarMision(nombre, asignacion);
        io.writeln("Ejecucion iniciada con id " + id);
    }

    //Opcion 4: proponerEjecucionAutomatica() y consultarExcluidasPropuesta()
    private static void proponerEjecucionAutomatica() throws Exception {
        List<String[]> filas = uc.proponerEjecucionAutomatica();
        mostrarFilas("mision | probExito (%) | beneficioEsperado | equipo", filas);

        List<String> excluidas = uc.consultarExcluidasPropuesta();
        io.writeln("Misiones excluidas:");
        if (excluidas.isEmpty()) io.writeln("  (ninguna)");
        for (String excluida : excluidas) {
            io.writeln("  " + excluida);
        }
    }

    //Opcion 5: confirmarEjecucionAutomatica()
    private static void confirmarEjecucionAutomatica() throws Exception {
        List<String> avisos = uc.confirmarEjecucionAutomatica();
        io.writeln("Propuesta confirmada");
        mostrarAvisos(avisos);
    }

    //Opcion 6: fija la semilla del Random compartido para que el juego de pruebas sea reproducible
    private static void fijarSemilla() throws Exception {
        int semilla = leerEntero("Semilla: ");
        Azar.setSemilla(semilla);
        io.writeln("Semilla fijada a " + semilla);
    }
}