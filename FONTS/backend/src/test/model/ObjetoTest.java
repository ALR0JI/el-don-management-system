package test.model;

import main.model.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
/**
 * @author Adrià 
 */
@RunWith(MockitoJUnitRunner.class)
public class ObjetoTest {

    private Objeto camion;
    private Objeto explosivo;
    private Random random;

    @Before
    public void setUp() {
        random = mock(Random.class, withSettings().withoutAnnotations());   // No se puede hacer @mock de Random en Java 21
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

    @Test(expected = IllegalArgumentException.class)
    public void testAñadirCantidadInvalidaLanzaExcepcion() {
        camion.añadir(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDescontarCantidadInvalidaLanzaExcepcion() {
        camion.descontar(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalcularUnidadesPerdidasRandomNuloLanzaExcepcion() {
        camion.calcularUnidadesPerdidas(2, false, null);
    }

    @Test
    public void testCalcularUnidadesPerdidasConsumible() {
        explosivo.añadir(10);
        assertEquals(5, explosivo.calcularUnidadesPerdidas(5, true, random));
        assertEquals(5, explosivo.calcularUnidadesPerdidas(5, false, random));
        
        verifyNoInteractions(random);
    }

    @Test
    public void testCalcularUnidadesPerdidasReutilizableExito() {
        camion.añadir(2);
        assertEquals(0, camion.calcularUnidadesPerdidas(2, true, random));
        
        verifyNoInteractions(random);
    }

    @Test
    public void testCalcularUnidadesPerdidasReutilizableFracasoDeterminista() {
        //se simulan 10 tiradas de nextDouble() 4 menores que 0.25 (se pierde el objeto) y 6 mayores que 0.25 (se salva el objeto)
        when(random.nextDouble()).thenReturn(
            0.1, 0.1, 0.1, 0.1,    
            0.5, 0.5, 0.5, 0.5, 0.5, 0.5            
        );

        int perdidas = camion.calcularUnidadesPerdidas(10, false, random);
        assertEquals(4, perdidas);

        verify(random, times(10)).nextDouble();
    }
}