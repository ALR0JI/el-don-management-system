package test.model;

import main.model.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ObjetoTest {

    private Objeto camion;
    private Objeto explosivo;

    @Mock
    private Random randomMock;

    @Before
    public void setUp() {
        camion = new Objeto("Camion blindado", "Vehiculo de escape", 50000, TUso.REUTILIZABLE, TCategoria.VEHICULO);
        explosivo = new Objeto("C4", "Explosivo plastico", 2000, TUso.SE_GASTA, TCategoria.HERRAMIENTA);
    }

    @Test
    public void testEstadoInicial() {
        assertEquals("Camion blindado", camion.getNombre());
        assertEquals(50000, camion.getPrecio());
        assertEquals(0, camion.getStock());
        assertTrue(camion.esReutilizable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrecioInvalido() {
        new Objeto("Ilegal", "Test", 0, TUso.REUTILIZABLE, TCategoria.ARMA);
    }

    @Test
    public void testAñadirYDescontarStock() {
        camion.añadir(3);
        assertEquals(3, camion.getStock());
        assertTrue(camion.estaDisponible(2));

        assertTrue(camion.descontar(2));
        assertEquals(1, camion.getStock());

        assertFalse(camion.descontar(5));
        assertEquals(1, camion.getStock());
    }

    @Test
    public void testCalcularUnidadesPerdidasConsumible() {
        explosivo.añadir(10);
        assertEquals(5, explosivo.calcularUnidadesPerdidas(5, true, randomMock));
        assertEquals(5, explosivo.calcularUnidadesPerdidas(5, false, randomMock));
        
        verifyNoInteractions(randomMock);
    }

    @Test
    public void testCalcularUnidadesPerdidasReutilizableExito() {
        camion.añadir(2);
        assertEquals(0, camion.calcularUnidadesPerdidas(2, true, randomMock));
        
        verifyNoInteractions(randomMock);
    }

    @Test
    public void testCalcularUnidadesPerdidasReutilizableFracasoDeterminista() {
        //se simulan 10 tiradas de nextDouble() 4 menores que 0.25 (se pierde el objeto) y 6 mayores que 0.25 (se salva el objeto)
        when(randomMock.nextDouble()).thenReturn(
            0.1, 0.1, 0.1, 0.1,    // 4 perdidas (0.1 * 100 < 25)
            0.5, 0.5, 0.5, 0.5, 0.5, 0.5            // 6 conservadas (0.5 * 100 >= 25)
        );

        int perdidas = camion.calcularUnidadesPerdidas(10, false, randomMock);
        assertEquals(4, perdidas);

        verify(randomMock, times(10)).nextDouble();
    }
}