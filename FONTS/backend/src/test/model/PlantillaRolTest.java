package test.model;

import main.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Adrià
 */
@RunWith(MockitoJUnitRunner.class)
public class PlantillaRolTest {

    @Mock
    private Mision misionMock;

    private PlantillaRol plantilla;

    @Before
    public void setUp() {
        plantilla = new PlantillaRol(misionMock, TRol.ASALTO, 2);
    }

    @Test
    public void testEstadoInicial() {
        assertEquals(misionMock, plantilla.getMision());
        assertEquals(TRol.ASALTO, plantilla.getRol());
        assertEquals(2, plantilla.getCantidad());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMisionNulaLanzaExcepcion() {
        new PlantillaRol(null, TRol.CONDUCTOR, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRolNuloLanzaExcepcion() {
        new PlantillaRol(misionMock, null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorCantidadCeroLanzaExcepcion() {
        new PlantillaRol(misionMock, TRol.CONDUCTOR, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorCantidadNegativaLanzaExcepcion() {
        new PlantillaRol(misionMock, TRol.CONDUCTOR, -3);
    }

    @Test
    public void testSetCantidadValida() {
        when(misionMock.tieneEjecuciones()).thenReturn(false);

        plantilla.setCantidad(4);

        assertEquals(4, plantilla.getCantidad());
        verify(misionMock).tieneEjecuciones();
    }

    @Test(expected = IllegalStateException.class)
    public void testSetCantidadMisionYaEjecutadaLanzaExcepcion() {
        when(misionMock.tieneEjecuciones()).thenReturn(true);

        plantilla.setCantidad(4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCantidadCeroLanzaExcepcion() {
        when(misionMock.tieneEjecuciones()).thenReturn(false);

        plantilla.setCantidad(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCantidadNegativaLanzaExcepcion() {
        when(misionMock.tieneEjecuciones()).thenReturn(false);

        plantilla.setCantidad(-2);
    }
}