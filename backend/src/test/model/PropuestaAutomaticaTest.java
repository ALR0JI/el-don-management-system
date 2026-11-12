package test.model;

import main.model.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Pol
 */

@RunWith(MockitoJUnitRunner.class)
public class PropuestaAutomaticaTest {

    @Mock private AsignacionPropuesta asignacion1;
    @Mock private AsignacionPropuesta asignacion2;

    // Comprueba que los getters devuelven las listas recibidas, en el mismo orden
    @Test
    public void testCreacionConservaDatosYOrden() {
        PropuestaAutomatica p = new PropuestaAutomatica(
                List.of(asignacion1, asignacion2), List.of("M3: necesita 5 secuaces y solo hay 4 libres"));

        assertEquals(List.of(asignacion1, asignacion2), p.getAsignaciones());
        assertEquals(List.of("M3: necesita 5 secuaces y solo hay 4 libres"), p.getExcluidas());
    }

    // Comprueba que una propuesta vacia (sin asignaciones ni excluidas) es valida
    @Test
    public void testPropuestaVacia() {
        PropuestaAutomatica p = new PropuestaAutomatica(new ArrayList<>(), new ArrayList<>());

        assertTrue(p.getAsignaciones().isEmpty());
        assertTrue(p.getExcluidas().isEmpty());
    }

    // Comprueba que una propuesta con asignaciones pero sin excluidas, y al reves, tambien son validas
    @Test
    public void testSoloAsignacionesOSoloExcluidas() {
        PropuestaAutomatica soloAsignaciones = new PropuestaAutomatica(List.of(asignacion1), new ArrayList<>());
        PropuestaAutomatica soloExcluidas = new PropuestaAutomatica(new ArrayList<>(), List.of("M1: sin secuaces"));

        assertEquals(1, soloAsignaciones.getAsignaciones().size());
        assertTrue(soloAsignaciones.getExcluidas().isEmpty());
        assertTrue(soloExcluidas.getAsignaciones().isEmpty());
        assertEquals(1, soloExcluidas.getExcluidas().size());
    }

    // Comprueba que la lista de asignaciones nula se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testAsignacionesNulaExcepcion() {
        new PropuestaAutomatica(null, new ArrayList<>());
    }

    // Comprueba que la lista de excluidas nula se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testExcluidasNulaExcepcion() {
        new PropuestaAutomatica(new ArrayList<>(), null);
    }

    // Comprueba que modificar la lista de asignaciones original despues de crear la propuesta no la cambia
    @Test
    public void testCopiaDefensivaDeLasAsignaciones() {
        List<AsignacionPropuesta> original = new ArrayList<>(List.of(asignacion1));
        PropuestaAutomatica p = new PropuestaAutomatica(original, new ArrayList<>());

        original.add(asignacion2);

        assertEquals(List.of(asignacion1), p.getAsignaciones());
    }

    // Comprueba que modificar la lista de excluidas original despues de crear la propuesta no la cambia
    @Test
    public void testCopiaDefensivaDeLasExcluidas() {
        List<String> original = new ArrayList<>(List.of("M1: sin secuaces"));
        PropuestaAutomatica p = new PropuestaAutomatica(new ArrayList<>(), original);

        original.add("M2: sin secuaces");
        original.clear();

        assertEquals(List.of("M1: sin secuaces"), p.getExcluidas());
    }

    // Comprueba que getAsignaciones devuelve una lista inmodificable
    @Test(expected = UnsupportedOperationException.class)
    public void testGetAsignacionesEsInmodificable() {
        new PropuestaAutomatica(new ArrayList<>(List.of(asignacion1)), new ArrayList<>())
                .getAsignaciones().add(asignacion2);
    }

    // Comprueba que getExcluidas devuelve una lista inmodificable
    @Test(expected = UnsupportedOperationException.class)
    public void testGetExcluidasEsInmodificable() {
        new PropuestaAutomatica(new ArrayList<>(), new ArrayList<>(List.of("M1: sin secuaces")))
                .getExcluidas().add("M2: sin secuaces");
    }
}