package main.uc;

import main.model.*;
import main.ctrl.FactoriaCtrl;
import main.ctrl.CtrlFichero;
import main.ctrl.CtrlMision;
import main.ctrl.CtrlSecuaz;

import java.util.*;

/**
 * @author Alejandro
 */
public class DatosUC {
    private final CtrlFichero ctrlFichero;
    private final CtrlMision ctrlMision;
    private final CtrlSecuaz ctrlSecuaz;

    public DatosUC() {
        FactoriaCtrl factoria = FactoriaCtrl.getInstance();
        this.ctrlFichero = factoria.getCtrlFichero();
        this.ctrlMision = factoria.getCtrlMision();
        this.ctrlSecuaz = factoria.getCtrlSecuaz();
    }

    public List<String[]> leerSecuaces(String ruta, List<String> avisos) {
        comprobarLectura(ruta, avisos);
        avisos.clear(); // Garantizamos que no se guarden avisos indeseados

        List<String> lineas = ctrlFichero.leerLineas(ruta);
        List<String[]> filas = new ArrayList<>();

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

            String[] palabras = linea.split("\\s+");
            if (palabras.length < 2) {
                avisos.add("Linea " + numLinea + ": falta el nombre o el rol");
                continue;
            }

            // Se cogen todas las palabras menos la ultima, que es el rol, así que el nombre puede tener una o varias palabras
            String nombre = String.join(" ", Arrays.copyOf(palabras, palabras.length - 1));
            String textoRol = palabras[palabras.length - 1];

            TRol rol;
            try {
                rol = StringToTRol(textoRol);
                filas.add(new String[] {nombre, rol.name()});
            } catch (IllegalArgumentException e) {                      // Si hay una excepcion se recoge para meterla en avisos
                avisos.add("Linea " + numLinea + ": rol invalido");
            }
        }

        return filas;
    }

    public void confirmarSecuaces(List<String[]> filas) {
        if (filas == null) {
            throw new IllegalArgumentException("La lista de misiones no puede ser null");
        }

        // filas viene de leerSecuaces asi que la primera posicion es el nombre y la segunda el rol
        List<String> nombres = new ArrayList<>();
        List<TRol> roles = new ArrayList<>();
        for (int i = 0; i < filas.size(); i++) {
            try {
                String[] fila = filas.get(i);
                comprobarFila(fila, 2);
                if (fila[0].isBlank()) {
                    throw new IllegalArgumentException("El nombre no puede estar vacio");
                }
                nombres.add(fila[0].trim());
                roles.add(StringToTRol(fila[1]));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Secuaz en posicion " + (i + 1) + ": " + e.getMessage());
            }
        }

        for (int i = 0; i < filas.size(); i++) {
            Secuaz secuaz = new Secuaz(ctrlSecuaz.siguienteId(), nombres.get(i), null, roles.get(i));
            ctrlSecuaz.insertar(secuaz);
        }
    }

    public List<String[]> leerMisiones(String ruta, List<String> avisos) {
        comprobarLectura(ruta, avisos);
        avisos.clear(); // Garantizamos que no se guarden avisos indeseados

        List<String> lineas = ctrlFichero.leerLineas(ruta);
        List<String[]> filas = new ArrayList<>();

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

            // Antes de devolver la fila se revisa que todos los parametros sean validos
            try {
                String[] fila = separarMision(linea);
                StringToBotin(fila[1]);
                StringToPlantilla(fila[2]);
                StringToProbabilidad(fila[3]);
                filas.add(fila);
            } catch (IllegalArgumentException e) {
                avisos.add("Linea " + numLinea + ": " + e.getMessage());
            }
        }

        return filas;
    }

    public void confirmarMisiones(List<String[]> filas) {
        if (filas == null) {
            throw new IllegalArgumentException("La lista de secuaces no puede ser null");
        }

        List<Mision> misiones = new ArrayList<>();
        Map<String, List<String>> dependencias = new LinkedHashMap<>();

        for (int i = 0; i < filas.size(); i++) {
            try {
                String[] fila = filas.get(i);
                comprobarFila(fila, 5);
                String nombre = fila[0].trim();

                if (ctrlMision.exists(nombre)) { // Solo se pueden agregar misiones nuevas
                    throw new IllegalArgumentException("Ya existe una mision llamada " + nombre);
                }
                if (dependencias.containsKey(nombre)) {
                    throw new IllegalArgumentException("La mision " + nombre + " esta repetida en el fichero");
                }

                Mision mision = new Mision( nombre,
                                   null,
                                            StringToBotin(fila[1]),
                                            StringToProbabilidad(fila[3]),
                                    null,
                                            StringToPlantilla(fila[2]));
                misiones.add(mision);
                dependencias.put(nombre, convertirDependencias(fila[4]));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Mision en posicion " + (i + 1) + ": " + e.getMessage());
            }
        }

        // Comrpobacion de dependencias
        for (Map.Entry<String, List<String>> entrada : dependencias.entrySet()) {
            for (String dependencia : entrada.getValue()) {
                if (!dependencias.containsKey(dependencia) && !ctrlMision.exists(dependencia)) {
                    throw new IllegalArgumentException("La mision " + entrada.getKey() + " depende de " + dependencia + " que no existe");
                }
            }
        }

        if (hayCiclo(dependencias)) {
            throw  new IllegalArgumentException("Las dependencias forman un ciclo");
        }

        for (Mision mision : misiones) {
            ctrlMision.insertar(mision);
        }
        for (Mision mision : misiones) {
            for (String dependencia : dependencias.get(mision.getNombreClave())) {
                mision.añadirPrerrequisito(ctrlMision.get(dependencia));
            }
        }
    }

    public void empezarPartida() {
        throw new UnsupportedOperationException("Entrega 2");
    }

    private void comprobarLectura(String ruta, List<String> avisos) {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("La ruta del fichero no puede ser null ni vacia");
        }
        if (avisos == null) {
            throw new IllegalArgumentException("La lista de avisos no puede ser null");
        }
    }

    private void comprobarFila(String[] fila, int columnas) {
        if (fila == null || fila.length != columnas) {
            throw new IllegalArgumentException("la fila debe tener " + columnas + " columnas");
        }
        for (String celda : fila) {
            if (celda == null) {
                throw new IllegalArgumentException("la fila tiene una columna null");
            }
        }
    }

    /**
     * Alejandro:
     * Todas las funciones para cambiar entre tipos son privadas para evitar que la
     * capa de presentacion se acople con la de dominio
     */

    private TRol StringToTRol(String texto) {
        try {
            // trim quita los espacios de los extremos y toUpperCase lo pasa a mayus
            return TRol.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("rol invalido: " + texto);
        }
    }

    private long StringToBotin(String texto){
        try {
            return Long.parseLong(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("el botin no es un numero entero: " + texto);
        }
    }

    private double StringToProbabilidad(String texto) {
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("la probabilidad de fracaso no es un numero: " + texto);
        }
    }

    private Map<TRol, Integer> StringToPlantilla(String texto) {
        // Limpieza del string
        String textoLimpio = texto.trim();

        if (textoLimpio.isEmpty()) {
            throw new IllegalArgumentException("la plantilla esta vacia");
        }

        // \s dice que corte textoLimpio por cada espacio y con el + se consigue que si hay
        // dos seguidos los identifique como uno solo
        String[] palabras = textoLimpio.split("\\s+");
        if (palabras.length % 2 != 0) {
            throw new IllegalArgumentException("la plantilla debe ser una lista de pares ROL cantidad");
        }

        // Creacion de la plantilla
        Map<TRol, Integer> plantilla = new LinkedHashMap<>();
        for (int i = 0; i < palabras.length; i += 2) {
            TRol rol = StringToTRol(palabras[i]);

            if (plantilla.containsKey(rol)) {
                throw new IllegalArgumentException("el rol " + rol + " esta repetido en la plantilla");
            }

            int cantidad;
            try {
                cantidad = Integer.parseInt(palabras[i+1]);
                plantilla.put(rol, cantidad);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("cantidad invalida para " + rol + ": " + palabras[i + 1]);
            }
        }

        return plantilla;
    }

    private List<String> convertirDependencias(String texto) {
        List<String> dependencias = new ArrayList<>();
        String textoLimpio = texto.trim();
        if (textoLimpio.isEmpty()) {
            return dependencias;
        }

        for (String dependencia : textoLimpio.split("\\s+")) {
            if (dependencias.contains(dependencia)) {
                throw new IllegalArgumentException("la dependencia " + dependencia + " esta repetida");
            }
            dependencias.add(dependencia);
        }
        return dependencias;
    }

    private boolean hayCiclo(Map<String, List<String>> dependencias) {
        Map<String, Integer> pendientes = new HashMap<>();
        Map<String, List<String>> dependientes = new HashMap<>();

        // Preparacion y inicializacion del grafo
        for (String nombre : dependencias.keySet()) {
            pendientes.put(nombre, 0);
            dependientes.put(nombre, new ArrayList<>());
        }

        for (Map.Entry<String, List<String>> entrada : dependencias.entrySet()) {
            for (String dependencia : entrada.getValue()) {
                if (dependencias.containsKey(dependencia)) {
                    pendientes.put(entrada.getKey(), pendientes.get(entrada.getKey()) + 1);   // para cada mision nueva se añade uno al numero de misiones de las que depende
                    dependientes.get(dependencia).add(entrada.getKey());                      // se registra la relacion de forma inversa
                }
            }
        }

        Deque<String> cola = new ArrayDeque<>();  // Deque se utiliza como una cola FIFO para hacer el BFS
        for (Map.Entry<String, Integer> entrada : pendientes.entrySet()) {
            if (entrada.getValue() == 0) {
                cola.add(entrada.getKey());
            }
        }

        // Algoritmo BFS para buscar ciclos
        int procesadas = 0;
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            procesadas++;
            for (String siguiente : dependientes.get(actual)) {
                pendientes.put(siguiente, pendientes.get(siguiente) - 1);
                if (pendientes.get(siguiente) == 0) {
                    cola.add(siguiente);
                }
            }
        }

        return procesadas < dependencias.size();
    }

    private String[] separarMision(String linea) {
        // Se identifican los cuatro corchetes
        int abre1 = linea.indexOf('[');
        int cierra1 = linea.indexOf(']');
        int abre2 = linea.lastIndexOf('[');
        int cierra2 = linea.lastIndexOf(']');

        boolean ordenCorrecto = abre1 >= 0 && abre1 < cierra1 && cierra1 < abre2 && abre2 < cierra2;
        boolean solo2Pares = linea.indexOf('[', abre1 + 1) == abre2 && linea.indexOf(']', cierra1 + 1) == cierra2;   // no hay mas de dos pares de corhcetes

        if (!ordenCorrecto || !solo2Pares || !linea.substring(cierra2 + 1).isBlank()) {                                               // no hay nada mas despues del ultimo par de corchetes
            throw new IllegalArgumentException("Formato incorrecto: se esperaba Nombre Botin [ROL n ...] k [Dependencias]");
        }

        String[] cabecera = linea.substring(0, abre1).trim().split("\\s+");
        String[] probabilidad = linea.substring(cierra1 + 1, abre2).trim().split("\\s+");
        if (cabecera.length != 2 || probabilidad.length != 1 || probabilidad[0].isEmpty()) {
            throw new IllegalArgumentException("Formato incorrecto: se esperaba Nombre Botin [ROL n ...] k [Dependencias]");
        }

        return new String[] {
                cabecera[0],
                cabecera[1],
                linea.substring(abre1 + 1, cierra1).trim(),
                probabilidad[0],
                linea.substring(abre2 + 1, cierra2).trim()
        };
    }
}