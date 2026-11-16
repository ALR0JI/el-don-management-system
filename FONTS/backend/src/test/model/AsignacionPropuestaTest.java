package test.model;

import main.model.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Pol
 */

@RunWith(MockitoJUnitRunner.class)
public class AsignacionPropuestaTest {

    @Mock private Mision mision;
    @Mock private Secuaz conductor;
    @Mock private Secuaz hacker;

    private Map<TRol, List<Secuaz>> asignacion;

    // Por defecto: un conductor y un hacker en listas modificables
    @Before
    public void setUp() {
        asignacion = new HashMap<>();
        asignacion.put(TRol.CONDUCTOR, new ArrayList<>(List.of(conductor)));
        asignacion.put(TRol.HACKER, new ArrayList<>(List.of(hacker)));
    }

    // Comprueba que los getters devuelven todos los valores recibidos
    @Test
    public void testCreacionConservaTodosLosDatos() {
        AsignacionPropuesta a = new AsignacionPropuesta(mision, asignacion, 80.0, 7500.5);

        assertSame(mision, a.getMision());
        assertEquals(80.0, a.getProbExito(), 0.001);
        assertEquals(7500.5, a.getBeneficioEsperado(), 0.001);
        assertEquals(asignacion, a.getAsignacion());
    }

    // Comprueba que una asignacion sin ningun puesto es valida
    @Test
    public void testAsignacionVaciaValida() {
        AsignacionPropuesta a = new AsignacionPropuesta(mision, new HashMap<>(), 0.0, 0.0);

        assertTrue(a.getAsignacion().isEmpty());
    }

    // Comprueba que una mision nula se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testMisionNulaExcepcion() {
        new AsignacionPropuesta(null, asignacion, 80.0, 100.0);
    }

    // Comprueba que una asignacion nula se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testAsignacionNulaExcepcion() {
        new AsignacionPropuesta(mision, null, 80.0, 100.0);
    }

    // Comprueba que añadir un puesto al mapa original despues de crear la asignacion no la cambia
    @Test
    public void testCopiaDefensivaDelMapa() {
        AsignacionPropuesta a = new AsignacionPropuesta(mision, asignacion, 80.0, 100.0);

        asignacion.put(TRol.ASALTO, new ArrayList<>());

        assertEquals(2, a.getAsignacion().size());
        assertFalse(a.getAsignacion().containsKey(TRol.ASALTO));
    }

    // Comprueba que modificar una lista de secuaces original despues de crear la asignacion no la cambia
    @Test
    public void testCopiaDefensivaDeLasListas() {
        AsignacionPropuesta a = new AsignacionPropuesta(mision, asignacion, 80.0, 100.0);

        asignacion.get(TRol.CONDUCTOR).add(hacker);

        assertEquals(List.of(conductor), a.getAsignacion().get(TRol.CONDUCTOR));
    }

    // Comprueba que getAsignacion devuelve un mapa inmodificable
    @Test(expected = UnsupportedOperationException.class)
    public void testGetAsignacionEsInmodificable() {
        new AsignacionPropuesta(mision, asignacion, 80.0, 100.0)
                .getAsignacion().put(TRol.ASALTO, new ArrayList<>());
    }

    // Comprueba que las listas de secuaces que devuelve getAsignacion tambien son inmodificables
    @Test(expected = UnsupportedOperationException.class)
    public void testListasInternasSonInmodificables() {
        new AsignacionPropuesta(mision, asignacion, 80.0, 100.0)
                .getAsignacion().get(TRol.CONDUCTOR).add(hacker);
    }
}