package drivers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.uc.MisionesUC;


/**
 * @author Adrià
 */
public class DriverMisionesUC {

    private static final inout io = new inout();
    private static final MisionesUC uc = new MisionesUC();

    public static void main(String[] args) throws Exception {
        io.writeln("--- Driver de MisionesUC --- \n En futuras implementaciones exsitirá la opcion de añadir / quitar material \n");
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
                    case 1 -> crearMision();
                    case 2 -> modificarMision();
                    case 3 -> eliminarMision();
                    case 4 -> añadirPrerrequisito();
                    case 5 -> eliminarPrerrequisito();
                    case 6 -> definirRolPlantilla();
                    case 7 -> eliminarRolPlantilla();
                    //case 8 -> definirMaterial();
                    //case 9 -> eliminarMaterial();
                    case 8 -> consultarMision();
                    case 9 -> listarMisiones();
                    case 10 -> mostrarMenu();
                    default -> io.writeln("El número introducido no es una opción válida");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                io.writeln("ERROR: " + e.getMessage());
            }
        }
        io.writeln("Fin del driver");
    }

    // opcion 10
    private static void mostrarMenu() throws Exception {
        io.writeln(" 1. Crear misión");
        io.writeln(" 2. Modificar misión");
        io.writeln(" 3. Eliminar misión");
        io.writeln(" 4. Añadir prerrequisito");
        io.writeln(" 5. Eliminar prerrequisito");
        io.writeln(" 6. Definir rol en plantilla");
        io.writeln(" 7. Eliminar rol de plantilla");
        //io.writeln(" 8. Definir material requisito (no esta implementado para la 1a entrega)");
        //io.writeln(" 9. Eliminar material requisito (no esta implementado para la 1a entrega)");
        io.writeln(" 8. Consultar misión");
        io.writeln(" 9. Listar misiones (con filtro de estado)");
        io.writeln("10. Mostrar este menú");
        io.writeln(" 0. Salir");
    }

    // funciones auxiliares para lectura con inout

    private static String leerTexto(String pregunta) throws Exception {
        io.write(pregunta);
        return io.readline().trim();
    }

    private static int leerEntero(String pregunta) throws Exception {
        io.write(pregunta);
        int valor;
        try {
            valor = io.readint();
        } catch (Exception e) {
            io.readln();
            throw new IllegalArgumentException("Hay que escribir un número entero");
        }
        io.readln();
        return valor;
    }

    private static long leerLong(String pregunta) throws Exception {
        io.write(pregunta);
        long valor;
        try {
            valor = Long.parseLong(io.readword());
        } catch (Exception e) {
            io.readln();
            throw new IllegalArgumentException("Hay que escribir un número entero válido (botín)");
        }
        io.readln();
        return valor;
    }

    private static double leerDouble(String pregunta) throws Exception {
        io.write(pregunta);
        double valor;
        try {
            valor = io.readdouble();
        } catch (Exception e) {
            io.readln();
            throw new IllegalArgumentException("Hay que escribir un número decimal válido");
        }
        io.readln();
        return valor;
    }

    private static void mostrarFilas(String cabecera, List<String[]> filas) throws Exception {
        io.writeln(cabecera);
        if (filas.isEmpty()) {
            io.writeln("  (ninguna)");
            return;
        }
        for (String[] fila : filas) {
            io.writeln("  " + String.join(" | ", fila));
        }
    }

    // opciones del controlador

    // 1
    private static void crearMision() throws Exception {
        String nombreClave = leerTexto("Nombre clave: ");
        String ubicacion = leerTexto("Ubicación (vacío si no tiene): ");
        long botin = leerLong("Botín: ");
        double probFracaso = leerDouble("Probabilidad de fracaso (0.0 - 100.0): ");
        
        String durStr = leerTexto("Duración en horas (vacío para usar valor el por defecto 12): ");
        Integer duracion = null;
        if (!durStr.isEmpty()) {
            duracion = Integer.parseInt(durStr);
        }

        String plantillaInput = leerTexto("Plantilla de roles (ej: CONDUCTOR 1 ASALTO 2): ");
        Map<String, Integer> plantilla = new HashMap<>();
        if (!plantillaInput.isEmpty()) {
            String[] partes = plantillaInput.split("\\s+");
            for (int i = 0; i < partes.length - 1; i += 2) {
                plantilla.put(partes[i].toUpperCase(), Integer.parseInt(partes[i + 1]));
            }
        }

        String prereqsInput = leerTexto("Prerrequisitos separados por un espacio (vacío si no tiene): ");
        List<String> prerrequisitos = new ArrayList<>();
        if (!prereqsInput.isEmpty()) {
            for (String p : prereqsInput.split("\\s+")) {
                prerrequisitos.add(p);
            }
        }
        
        String materialInput = "";
        Map<String, Integer> material = new HashMap<>();
        if (!materialInput.isEmpty()) {
            String[] partes = materialInput.split("\\s+");
            for (int i = 0; i < partes.length - 1; i += 2) {
                material.put(partes[i], Integer.parseInt(partes[i + 1]));
            }
        }
        

        uc.crearMision(nombreClave, ubicacion, botin, probFracaso, duracion, plantilla, prerrequisitos, material);
        io.writeln("Misión creada con éxito.");
    }

    // 2
    private static void modificarMision() throws Exception {
        String nombreClave = leerTexto("Nombre clave de la misión que se quiere modificar: ");
        String ubicacion = leerTexto("Nueva ubicación: ");
        long botin = leerLong("Nuevo botín: ");
        double probFracaso = leerDouble("Nueva probabilidad de fracaso: ");
        String durStr = leerTexto("Nueva duración (vacío para usar valor el por defecto 12): ");
        Integer duracion = null;
        if (!durStr.isEmpty()) {
            duracion = Integer.parseInt(durStr);
        }
        uc.modificarMision(nombreClave, ubicacion, botin, probFracaso, duracion);
        io.writeln("Misión modificada con éxito.");
    }

    // 3
    private static void eliminarMision() throws Exception {
        String nombreClave = leerTexto("Nombre clave de la misión que se quiere eliminar: ");
        uc.eliminarMision(nombreClave);
        io.writeln("Misión eliminada con éxito.");
    }

    // 4
    private static void añadirPrerrequisito() throws Exception {
        String nombreClave = leerTexto("Nombre clave de la misión principal: ");
        String nombrePrerrequisito = leerTexto("Nombre clave de la misión prerrequisito: ");

        uc.añadirPrerrequisito(nombreClave, nombrePrerrequisito);
        io.writeln("Prerrequisito añadido con éxito.");
    }

    // 5 
    private static void eliminarPrerrequisito() throws Exception {
        String nombreClave = leerTexto("Nombre clave de la misión principal: ");
        String nombrePrerrequisito = leerTexto("Nombre clave de la misión prerrequisito que se quiere eliminar: ");

        uc.eliminarPrerrequisito(nombreClave, nombrePrerrequisito);
        io.writeln("Prerrequisito eliminado con éxito.");
    }

    // 6
    private static void definirRolPlantilla() throws Exception {
        String nombreClave = leerTexto("Nombre clave de la misión: ");
        String rol = leerTexto("Rol (CONDUCTOR, ASALTO, HACKER, CERRAJERO, NEGOCIADOR) (solo uno): ");
        int cantidad = leerEntero("Cantidad: ");

        uc.definirRolPlantilla(nombreClave, rol.toUpperCase(), cantidad);
        io.writeln("Rol de plantilla configurado con éxito.");
    }

    // 7
    private static void eliminarRolPlantilla() throws Exception {
        String nombreClave = leerTexto("Nombre clave de la misión: ");
        String rol = leerTexto("Rol que se quiere eliminar de la plantilla: ");

        uc.eliminarRolPlantilla(nombreClave, rol.toUpperCase());
        io.writeln("Rol eliminado de la plantilla con éxito.");
    }

    /* 
    private static void definirMaterial() throws Exception {
        String nombreClave = leerTexto("Nombre clave de la misión: ");
        String nombreObjeto = leerTexto("Nombre del objeto material: ");
        int cantidad = leerEntero("Cantidad: ");

        uc.definirMaterial(nombreClave, nombreObjeto, cantidad);
        io.writeln("Requisito de material configurado con éxito.");
    }

    
    private static void eliminarMaterial() throws Exception {
        String nombreClave = leerTexto("Nombre clave de la misión: ");
        String nombreObjeto = leerTexto("Nombre del objeto material a eliminar: ");

        uc.eliminarMaterial(nombreClave, nombreObjeto);
        io.writeln("Requisito de material eliminado con éxito.");
    }
    */

    // 8
    private static void consultarMision() throws Exception {
        String nombreClave = leerTexto("Nombre clave de la misión a consultar: ");
        String[] datos = uc.consultarMision(nombreClave);
        
        List<String[]> lista = new ArrayList<>();
        lista.add(datos);
        mostrarFilas("nombreClave | ubicacion | botin | probFracaso | duracion | estado", lista);
    }

    // 9
    private static void listarMisiones() throws Exception {
        String estado = leerTexto("Estado a filtrar (DISPONIBLE, BLOQUEADA, EN_CURSO, COMPLETADA, FALLIDA) (vacío = todos): ");
        String filtroEstado = null;
        if (!estado.trim().isEmpty()) {
            filtroEstado = estado.toUpperCase();
        }

        List<String[]> filas = uc.listarMisiones(filtroEstado);
        mostrarFilas("nombreClave | ubicacion | botin | probFracaso | duracion | estado", filas);
    }
}