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
public class RequisitoMaterialTest {

    @Mock
    private Mision misionMock;

    @Mock
    private Objeto objetoMock;

    private RequisitoMaterial requisito;

    @Before
    public void setUp() {
        requisito = new RequisitoMaterial(misionMock, objetoMock, 2);
    }

    @Test
    public void testEstadoInicial() {
        assertEquals(misionMock, requisito.getMision());
        assertEquals(objetoMock, requisito.getObjeto());
        assertEquals(2, requisito.getCantidad());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisionNula() {
        new RequisitoMaterial(null, objetoMock, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testObjetoNulo() {
        new RequisitoMaterial(misionMock, null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCantidadInvalidaConstructor() {
        new RequisitoMaterial(misionMock, objetoMock, 0);
    }

    @Test
    public void testSetCantidadValida() {
        when(misionMock.tieneEjecuciones()).thenReturn(false);
        requisito.setCantidad(5);
        assertEquals(5, requisito.getCantidad());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetCantidadMisionYaEjecutada() {
        when(misionMock.tieneEjecuciones()).thenReturn(true);
        requisito.setCantidad(5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCantidadInvalida() {
        requisito.setCantidad(-2);
    }

    @Test
    public void testEsSuficienteEnAlmacenYUnidadesFaltantes() {
        //stock insuficiente (0 unidades)
        when(objetoMock.estaDisponible(2)).thenReturn(false);
        when(objetoMock.getStock()).thenReturn(0);
        assertFalse(requisito.esSuficienteEnAlmacen());
        assertEquals(2, requisito.unidadesFaltantes());

        //stock a medias (1 unidad)
        when(objetoMock.estaDisponible(2)).thenReturn(false);
        when(objetoMock.getStock()).thenReturn(1);
        assertFalse(requisito.esSuficienteEnAlmacen());
        assertEquals(1, requisito.unidadesFaltantes());

        //stock suficiente (2 unidades)
        when(objetoMock.estaDisponible(2)).thenReturn(true);
        when(objetoMock.getStock()).thenReturn(2);
        assertTrue(requisito.esSuficienteEnAlmacen());
        assertEquals(0, requisito.unidadesFaltantes());

        verify(objetoMock, times(3)).estaDisponible(2);
        verify(objetoMock, times(3)).getStock();
    }

    @Test
    public void testUnidadesFaltantesConStockSuperiorAlNecesarioDevuelveCero() {
        when(objetoMock.estaDisponible(2)).thenReturn(true);
        when(objetoMock.getStock()).thenReturn(5);

        assertTrue(requisito.esSuficienteEnAlmacen());
        assertEquals(0, requisito.unidadesFaltantes());
    }
}