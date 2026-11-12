package drivers;

import java.util.List;

import main.uc.SecuacesUC;
import main.model.Don;
import main.ctrl.FactoriaCtrl;
import main.model.Reloj;
import main.model.Secuaz;
import main.model.TConsecuencia;



//Pendiente: Preguntar si puedo añadir funciones para darle dinero a Don,
//detener a un secuaz(solo se hace al acabar una mision) y ver la caja de Don
//Además de interactivo, se puede introducir un fichero de texto si se añade el final del comando:
//  java -cp out/backend:out/drivers drivers.DriverSecuacesUC < entrada.txt
//El fichero de texto siempre debe acabar en 0.

/**
 * @author Noa
 */
public class DriverSecuacesUC {

    //Utilizamos el fichero inout colgado en la web de prop
    private static final inout io = new inout();
    private static final SecuacesUC uc = new SecuacesUC();
    private static final Don don = Don.getInstance();
    private static final long DINERO_INICIAL = 1000000L;
    public static void main(String[] args) throws Exception {
        io.writeln("--- Driver de SecuacesUC ---");
        mostrarMenu();
        don.registrarComision(DINERO_INICIAL,"Inicio prueba");
        SecuacesUC.renovarOferta();

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
                    case 1 -> añadirSecuaz();
                    case 2 -> modificarSecuaz();
                    case 3 -> expulsar();
                    case 4 -> pagarFianza();
                    case 5 -> listarSecuaces();
                    case 6 -> listarEnOferta();
                    case 7 -> listarEnOfertaEspecialidad();
                    case 8 -> fichar();
                    case 9 -> renovarOferta();
                    case 10 -> mostrarMenu();
                    case 11 -> consultarCaja();
                    case 12 -> arrestarSecuaz();
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
        io.writeln(" 1. Añadir secuaz");
        io.writeln(" 2. Modificar secuaz");
        io.writeln(" 3. Expulsar secuaz");
        io.writeln(" 4. Pagar fianza");
        io.writeln(" 5. Listar secuaces (con filtros)");
        io.writeln(" 6. Listar candidatos en oferta");
        io.writeln(" 7. Listar candidatos en oferta por especialidad");
        io.writeln(" 8. Fichar candidato");
        io.writeln(" 9. Renovar oferta");
        io.writeln("10. Mostrar este menu");
        io.writeln("Los siguientes no pertenecen a SecuacesUC pero lo complementan.");
        io.writeln("11. Consultar caja");
        io.writeln("12. Arrestar secuaz (obligatorio para pagar fianza)");

    }

    //Funciones privadas que evitan la repetición de código:

    //Muestra la pregunta y devuelve la línea que escribe el usuario.
    private static String leerTexto(String pregunta) throws Exception {
        io.write(pregunta);
        return io.readline().trim();
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

    //Opción 1: añadirSecuaz(nombre, apodo, especialidad)
    private static void añadirSecuaz() throws Exception {
        String nombre = leerTexto("Nombre: ");
        String apodo = leerTexto("Apodo (vacio si no tiene): ");
        String especialidad = leerTexto("Especialidad (CONDUCTOR, ASALTO, HACKER, CERRAJERO, NEGOCIADOR): ");

        int id = uc.añadirSecuaz(nombre, apodo, especialidad.toUpperCase());
        io.writeln("Secuaz añadido con id " + id);
    }

    //Opción 2: modificarSecuaz(id, nombre, apodo)
    private static void modificarSecuaz() throws Exception {
        int id = leerEntero("Id: ");
        String nombre = leerTexto("Nombre: ");
        String apodo = leerTexto("Apodo (vacio si no tiene): ");

        uc.modificarSecuaz(id, nombre, apodo);
        io.writeln("Secuaz modificado");
    }

    //Opción 3: expulsar(id)
    private static void expulsar() throws Exception {
        int id = leerEntero("Id: ");

        boolean exito = uc.expulsar(id);
        if(exito) io.writeln("El secuaz ha sido expulsado con exito");
        else io.writeln("El secuaz no esta libre y no puede ser expulsado");
    }

    //Opción 4: pagarFianza(id)
    private static void pagarFianza() throws Exception {
        int id = leerEntero("Id: ");

        boolean exito = uc.pagarFianza(id);
        if(exito) io.writeln("El secuaz ha sido liberado con exito");
        else io.writeln("Don no tiene suficiente dinero para pagar su fianza");
    }

    //Opción 5: listarSecuaces(especialidad, estados...)
    private static void listarSecuaces() throws Exception {
        String especialidad = leerTexto("Especialidad (CONDUCTOR, ASALTO, HACKER, CERRAJERO, " +
                "NEGOCIADOR) (vacio = todas): ");
        String estados = leerTexto("Estados separados por espacio (LIBRE, OCUPADO, DETENIDO, MUERTO, " +
                "EXPULSADO) (vacio = todos): ");

        String filtroEspecialidad = null;
        if(!especialidad.isEmpty()) filtroEspecialidad = especialidad.toUpperCase();
        String[] filtroEstados = new String[0];
        if(!estados.isEmpty()) filtroEstados = estados.toUpperCase().split("\\s+");

        List<String[]> filas = uc.listarSecuaces(filtroEspecialidad, filtroEstados);
        mostrarFilas("id | nombre | apodo | especialidad | estado", filas);
    }

    //Opción 6: listarEnOferta()
    private static void listarEnOferta() throws Exception {
        List<String[]> filas = uc.listarEnOferta();
        mostrarFilas("nombre | apodo | especialidad | precioFichaje", filas);
    }

    //Opción 7: listarEnOfertaEspecialidad(especialidad)
    private static void listarEnOfertaEspecialidad() throws Exception {
        String especialidad = leerTexto("Especialidad (CONDUCTOR, ASALTO, HACKER, CERRAJERO, " +
                "NEGOCIADOR):");
        List<String[]> filas;
        if (especialidad.isEmpty()) {
            filas = uc.listarEnOferta();
        } else {
            filas = uc.listarEnOfertaEspecialidad(especialidad.toUpperCase());
        }
        mostrarFilas("nombre | apodo | especialidad | precioFichaje", filas);
    }

    //Opción 8: fichar(nombreCandidato)
    private static void fichar() throws Exception {
        String nombre = leerTexto("Nombre: ");

        boolean exito = uc.fichar(nombre);
        if(exito) io.writeln("Ahora " + nombre + ", forma parte de tus secuaces!");
        else io.writeln("Don no tiene suficiente dinero para pagar este candidato");
    }

    //Opción 9: renovarOferta()
    private static void renovarOferta() throws Exception {
        SecuacesUC.renovarOferta();
        io.writeln("Se ha renovado la oferta");
    }

    //Funciones ajenas a SecuacesUC pero que facilitan el entendimiento
    //Opción 10: consultarCaja()
    private static void consultarCaja() throws Exception {
        io.writeln("Te quedan " + don.getCaja() + "€");
    }

    //Arresta a un secuaz (no existe una funcion que lo haga porque solo se puede
    //arrestar a alguien al acabar una mision)
    //Opción 11: arrestar()
    private static void arrestarSecuaz() throws Exception {
        int id = leerEntero("Id: ");
        Secuaz s = FactoriaCtrl.getInstance().getCtrlSecuaz().get(id);
        if (s == null) {
            throw new IllegalArgumentException("No existe ningún secuaz con id " + id);
        }
        if (!s.ocupar()) {
            throw new IllegalStateException("El secuaz tiene que estar LIBRE para poder arrestarlo");
        }
        s.procesarConsecuencia(TConsecuencia.DETENIDO, Reloj.getInstance().horaActual());
        io.writeln("Secuaz arrestado. Su fianza es de " + s.getFianza() + "€");
    }

}
