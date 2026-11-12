package drivers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.model.Azar;
import main.uc.DatosUC;
import main.uc.EjecucionUC;
import main.uc.MisionesUC;
import main.uc.SecuacesUC;
import main.uc.SeguimientoUC;
import main.uc.TiempoUC;

/**
 * @author Pol
 */


public class DriverEjecucionUC {

    private static final inout io = new inout();
    private static final EjecucionUC uc = new EjecucionUC();

    private static final DatosUC datosUC = new DatosUC();
    private static final MisionesUC misionesUC = new MisionesUC();
    private static final SecuacesUC secuacesUC = new SecuacesUC();
    private static final SeguimientoUC seguimientoUC = new SeguimientoUC();
    private static final TiempoUC tiempoUC = new TiempoUC();

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
                    case 1 -> cargarSecuaces();
                    case 2 -> cargarMisiones();
                    case 3 -> listarSecuaces();
                    case 4 -> listarMisiones();
                    case 5 -> previsualizarProbabilidad();
                    case 6 -> comprarMaterialFaltante();
                    case 7 -> ejecutarMision();
                    case 8 -> proponerEjecucionAutomatica();
                    case 9 -> confirmarEjecucionAutomatica();
                    case 10 -> avanzarHoras();
                    case 11 -> verEjecucionesEnCurso();
                    case 12 -> verHistorial();
                    case 13 -> verCaja();
                    case 14 -> fijarSemilla();
                    case 15 -> mostrarMenu();
                    default -> io.writeln("El numero introducido no es una opcion");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                //Errores del dominio: se muestra el mensaje y se sigue con la siguiente opcion
                io.writeln("ERROR: " + e.getMessage());
            }              catch (RuntimeException e) {
                io.writeln("ERROR INESPERADO: " + e);
                e.printStackTrace(System.out);   // temporal: mostra la classe i la línia exactes
            }
        }
        io.writeln("Fin del driver");
    }

    private static void mostrarMenu() throws Exception {
        io.writeln(" 1. Cargar secuaces desde fichero");
        io.writeln(" 2. Cargar misiones desde fichero");
        io.writeln(" 3. Listar secuaces (con filtro de estados)");
        io.writeln(" 4. Listar misiones (con filtro de estado)");
        io.writeln(" 5. Previsualizar probabilidad de exito de un equipo");
        io.writeln(" 6. Comprar material faltante de una mision");
        io.writeln(" 7. Ejecutar mision manualmente");
        io.writeln(" 8. Proponer ejecucion automatica");
        io.writeln(" 9. Confirmar ejecucion automatica");
        io.writeln("10. Avanzar horas");
        io.writeln("11. Ver ejecuciones en curso");
        io.writeln("12. Ver historial de ejecuciones");
        io.writeln("13. Ver caja del Don");
        io.writeln("14. Fijar semilla del azar");
        io.writeln("15. Mostrar este menu");
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

    private static final String CABECERA_EJECUCIONES = "id | mision | tipo | inicio | fin | restante | resultado | leida | "
            + "botin | cobrado | comisionDon | muertes | detenidos | materialPerdido | probExito";

    //Opcion 1: DatosUC.leerSecuaces + confirmarSecuaces
    private static void cargarSecuaces() throws Exception {
        String ruta = leerTexto("Ruta del fichero de secuaces: ");
        List<String> avisos = new ArrayList<>();
        List<String[]> filas = datosUC.leerSecuaces(ruta, avisos);
        mostrarAvisos(avisos);
        datosUC.confirmarSecuaces(filas);
        io.writeln("Secuaces cargados: " + filas.size());
    }

    //Opcion 2: DatosUC.leerMisiones + confirmarMisiones
    private static void cargarMisiones() throws Exception {
        String ruta = leerTexto("Ruta del fichero de misiones: ");
        List<String> avisos = new ArrayList<>();
        List<String[]> filas = datosUC.leerMisiones(ruta, avisos);
        mostrarAvisos(avisos);
        datosUC.confirmarMisiones(filas);
        io.writeln("Misiones cargadas: " + filas.size());
    }

    //Opcion 3: SecuacesUC.listarSecuaces
    private static void listarSecuaces() throws Exception {
        String estados = leerTexto("Estados separados por espacio (LIBRE, OCUPADO, DETENIDO, MUERTO, "
                + "EXPULSADO) (vacio = todos): ");
        String[] filtroEstados = new String[0];
        if (!estados.isEmpty()) filtroEstados = estados.toUpperCase().split("\\s+");

        List<String[]> filas = secuacesUC.listarSecuaces(null, filtroEstados);
        mostrarFilas("id | nombre | apodo | especialidad | estado", filas);
    }

    //Opcion 4: MisionesUC.listarMisiones
    private static void listarMisiones() throws Exception {
        String estado = leerTexto("Estado (BLOQUEADA, DISPONIBLE, EN_CURSO, COMPLETADA, FALLIDA) (vacio = todos): ");
        List<String[]> filas = misionesUC.listarMisiones(estado.isEmpty() ? null : estado.toUpperCase());
        mostrarFilas("nombreClave | ubicacion | botin | probFracaso | duracion | estado", filas);
    }

    //Opcion 5: previsualizarProbabilidad(nombreClave, asignacion)
    private static void previsualizarProbabilidad() throws Exception {
        String nombre = leerTexto("Nombre clave de la mision: ");
        Map<String, List<Integer>> asignacion = leerAsignacion();

        double probabilidad = uc.previsualizarProbabilidad(nombre, asignacion);
        io.writeln("Probabilidad de exito: " + String.format(java.util.Locale.ROOT, "%.2f", probabilidad) + " %");
    }

    //Opcion 6: comprarMaterialFaltante(nombreClave)
    private static void comprarMaterialFaltante() throws Exception {
        String nombre = leerTexto("Nombre clave de la mision: ");

        boolean exito = uc.comprarMaterialFaltante(nombre);
        if (exito) io.writeln("La mision tiene todo el material necesario");
        else io.writeln("Don no tiene suficiente dinero para comprar el material que falta");
    }

    //Opcion 7: ejecutarMision(nombreClave, asignacion)
    private static void ejecutarMision() throws Exception {
        String nombre = leerTexto("Nombre clave de la mision: ");
        Map<String, List<Integer>> asignacion = leerAsignacion();

        int id = uc.ejecutarMision(nombre, asignacion);
        io.writeln("Ejecucion iniciada con id " + id);
    }

    //Opcion 8: proponerEjecucionAutomatica() y consultarExcluidasPropuesta()
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

    //Opcion 9: confirmarEjecucionAutomatica()
    private static void confirmarEjecucionAutomatica() throws Exception {
        List<String> avisos = uc.confirmarEjecucionAutomatica();
        io.writeln("Propuesta confirmada");
        mostrarAvisos(avisos);
    }

    //Opcion 10: TiempoUC.avanzarHora() tantas veces como se pida
    private static void avanzarHoras() throws Exception {
        int horas = leerEntero("Horas a avanzar: ");
        if (horas < 0) throw new IllegalArgumentException("Las horas no pueden ser negativas");

        for (int i = 0; i < horas; i++) {
            for (String[] terminada : tiempoUC.avanzarHora()) {
                io.writeln("Hora " + tiempoUC.horaActual() + ": ejecucion " + terminada[0]
                        + " (" + terminada[1] + ") terminada con " + terminada[2]);
            }
        }
        io.writeln("Hora actual: " + tiempoUC.horaActual());
    }

    //Opcion 11: SeguimientoUC.listarEjecucionesEnCurso
    private static void verEjecucionesEnCurso() throws Exception {
        mostrarFilas(CABECERA_EJECUCIONES, seguimientoUC.listarEjecucionesEnCurso());
    }

    //Opcion 12: SeguimientoUC.consultarHistorial
    private static void verHistorial() throws Exception {
        mostrarFilas(CABECERA_EJECUCIONES, seguimientoUC.consultarHistorial());
    }

    //Opcion 13: SeguimientoUC.consultarCaja
    private static void verCaja() throws Exception {
        io.writeln("Caja del Don: " + seguimientoUC.consultarCaja());
    }

    //Opcion 14: fija la semilla del Random compartido para que el juego de pruebas sea reproducible
    private static void fijarSemilla() throws Exception {
        int semilla = leerEntero("Semilla: ");
        Azar.setSemilla(semilla);
        io.writeln("Semilla fijada a " + semilla);
    }
}