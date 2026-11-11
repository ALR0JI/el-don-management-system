package test.model;

import main.model.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Alejandro
 */

public class DatosMisionTest {
    private static final String NOMBRE = "Robo_Edifici_Vertex";
    private static final long BOTIN = 5_000_000_000L;   // Mayor que Integer.MAX_VALUE para detectar que no se trunca a int
    private static final double PROB_FRACASO = 12.5;

    // Plantilla valida y modificable que se usa cuando el test no trata sobre ella
    private static Map<TRol, Integer> plantillaValida() {
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);
        return plantilla;
    }

    // Comprueba que el constructor guarda todos los atributos y con las dependencias en orden
    @Test
    public void testConstructorGuardaTodosLosAtributos() {
        Map<TRol, Integer> plantilla = Map.of(TRol.CONDUCTOR, 1, TRol.ASALTO, 2);
        List<String> dependencias = List.of("Robo_Edifici_Omega", "Robo_Bar_FIB");

        DatosMision datos = new DatosMision(NOMBRE, BOTIN, plantilla, PROB_FRACASO, dependencias);

        assertEquals(NOMBRE, datos.getNombreClave());
        assertEquals(BOTIN, datos.getBotin());
        assertEquals(plantilla, datos.getPlantilla());
        assertEquals(PROB_FRACASO, datos.getProbFracaso(), 0.0);
        assertEquals(dependencias, datos.getDependencias());
    }

    // Comprueba que un nombre clave null se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testNombreClaveNullLanzaExcepcion() {
        new DatosMision(null, BOTIN, plantillaValida(), PROB_FRACASO, new ArrayList<>());
    }

    // Comprueba que un nombre clave vacio se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testNombreClaveVacioLanzaExcepcion() {
        new DatosMision("", BOTIN, plantillaValida(), PROB_FRACASO, new ArrayList<>());
    }

    // Comprueba que un nombre clave formado solo por espacios se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testNombreClaveSoloEspaciosLanzaExcepcion() {
        new DatosMision("   ", BOTIN, plantillaValida(), PROB_FRACASO, new ArrayList<>());
    }

    // Comprueba que una plantilla null se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testPlantillaNullLanzaExcepcion() {
        new DatosMision(NOMBRE, BOTIN, null, PROB_FRACASO, new ArrayList<>());
    }

    // Comprueba que unas dependencias null se rechazan
    @Test(expected = IllegalArgumentException.class)
    public void testDependenciasNullLanzaExcepcion() {
        new DatosMision(NOMBRE, BOTIN, plantillaValida(), PROB_FRACASO, null);
    }

    // Comprueba que el constructor NO valida botin, probFracaso ni el contenido de la plantilla (lo hace DatosUC.confirmarMisiones)
    @Test
    public void testAceptaValoresQueSeValidanEnConfirmarMisiones() {
        // Botin negativo, probFracaso por encima de 100 y plantilla sin CONDUCTOR
        DatosMision fueraPorArriba = new DatosMision(NOMBRE, -1, Map.of(TRol.HACKER, 1), 100.5, List.of());
        assertEquals(-1, fueraPorArriba.getBotin());
        assertEquals(100.5, fueraPorArriba.getProbFracaso(), 0.0);
        assertEquals(Map.of(TRol.HACKER, 1), fueraPorArriba.getPlantilla());

        // Botin 0, probFracaso negativa y plantilla vacia
        DatosMision fueraPorAbajo = new DatosMision(NOMBRE, 0, Map.of(), -0.5, List.of());
        assertEquals(0, fueraPorAbajo.getBotin());
        assertEquals(-0.5, fueraPorAbajo.getProbFracaso(), 0.0);
        assertTrue(fueraPorAbajo.getPlantilla().isEmpty());
        assertTrue(fueraPorAbajo.getDependencias().isEmpty());
    }

    // Comprueba que modificar la plantilla y las dependencias originales despues de construir el objeto no le afecta
    @Test
    public void testCopiaDefensivaNoSeVeAfectadaPorCambiosEnLosOriginales() {
        Map<TRol, Integer> plantilla = plantillaValida();
        List<String> dependencias = new ArrayList<>(List.of("Robo_Edifici_Omega"));
        DatosMision datos = new DatosMision(NOMBRE, BOTIN, plantilla, PROB_FRACASO, dependencias);

        plantilla.put(TRol.HACKER, 3);
        dependencias.add("Robo_Bar_FIB");

        assertEquals(Map.of(TRol.CONDUCTOR, 1), datos.getPlantilla());
        assertEquals(List.of("Robo_Edifici_Omega"), datos.getDependencias());
    }

    // Comprueba que la plantilla que devuelve getPlantilla no se puede modificar
    @Test(expected = UnsupportedOperationException.class)
    public void testGetPlantillaEsInmodificable() {
        DatosMision datos = new DatosMision(NOMBRE, BOTIN, plantillaValida(), PROB_FRACASO, new ArrayList<>());

        datos.getPlantilla().put(TRol.HACKER, 1);
    }

    // Comprueba que la lista que devuelve getDependencias no se puede modificar
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDependenciasEsInmodificable() {
        List<String> dependencias = new ArrayList<>(List.of("Robo_Edifici_Omega"));
        DatosMision datos = new DatosMision(NOMBRE, BOTIN, plantillaValida(), PROB_FRACASO, dependencias);

        datos.getDependencias().add("Robo_Bar_FIB");
    }
}
