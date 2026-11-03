package model; // Su paquete según la carpeta test/model

// Importaciones de tus clases de dominio desde main.model
import main.model.*;

// Importaciones de JUnit
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Random;

public class ObjetoTest {

    private Objeto camion;
    private Objeto explosivo;

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

        assertFalse(camion.descontar(5)); // No hay suficiente
        assertEquals(1, camion.getStock()); // Se mantiene igual
    }

    @Test
    public void testCalcularUnidadesPerdidasConsumible() {
        explosivo.añadir(10);
        // Un objeto SE_GASTA pierde siempre todas sus unidades
        assertEquals(5, explosivo.calcularUnidadesPerdidas(5, true, new Random()));
        assertEquals(5, explosivo.calcularUnidadesPerdidas(5, false, new Random()));
    }

    @Test
    public void testCalcularUnidadesPerdidasReutilizableExito() {
        camion.añadir(2);
        // En éxito no pierde ninguna unidad
        assertEquals(0, camion.calcularUnidadesPerdidas(2, true, new Random()));
    }

    @Test
    public void testCalcularUnidadesPerdidasReutilizableFracaso() {
        // Semilla fija para tiradas deterministas
        Random random = new Random(42); 
        int perdidas = camion.calcularUnidadesPerdidas(10, false, random);
        assertTrue(perdidas >= 0 && perdidas <= 10);
    }
}
