package test.model;

import main.model.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Alejandro
 */

public class AzarTest {
    private static final long SEMILLA = 67L;
    private static final int TIRADAS = 20;

    // Todos los tests usan la misma semilla para controlar los resultados
    @Before
    public void setUp() {
        Azar.setSemilla(SEMILLA);
    }

    // Extrae n numeros del Random recibido
    private static long[] tirar(Random random, int n) {
        long[] valores = new long[n];
        for (int i = 0; i < n; i++) {
            valores[i] = random.nextLong();
        }
        return valores;
    }

    // Comprueba que si no existe ningun Random, getInstance() crea uno nuevo
    @Test
    public void testGetInstanceCreaRandomSiNoExiste() {
        Azar.reiniciar();

        Random random = Azar.getInstance();

        assertNotNull(random);
        assertEquals(random, Azar.getInstance());
    }

    // Comprueba que getInstance devuelva siempre la misma instancia (singleton)
    @Test
    public void testSiempreMismaInstancia() {
        Random r1 = Azar.getInstance();
        Random r2 = Azar.getInstance();

        assertNotNull(r1);
        assertSame(r1, r2);
    }

    // Comprueba que reiniciar descarta la semilla y que dos Random diferentes no den la misma secuencia (que es practicamente imposible)
    @Test
    public void testReiniciarDescartaLaSemilla() {
        Azar.reiniciar();

        long[] sinSemilla = tirar(Azar.getInstance(), TIRADAS);
        long[] conSemilla = tirar(new Random(SEMILLA), TIRADAS);
        assertFalse(Arrays.equals(conSemilla, sinSemilla));
    }

    // Comprueba que con una semilla dada las tiradas son exactamente las de un Random creado con esa semilla
    @Test
    public void testSemillaFijaLaSecuenciaDeTiradas() {
        // Long.MAX_VALUE es -1 y Random solo usa 48 bits asi que estos valores prueban los tres posibles bugs
        long[] semillas = {SEMILLA, 0L, -1L, 5_000_000_000L};

        for (long semilla : semillas) {
            Azar.setSemilla(semilla);
            assertArrayEquals(tirar(new Random(semilla), TIRADAS), tirar(Azar.getInstance(), TIRADAS));
        }
    }

    // Comprueba que pedir getInstance varias veces no reinicia la secuencia
    @Test
    public void testGetInstanceNoReiniciaLaSecuencia() {
        Random esperado = new Random(SEMILLA);

        for (int i = 0; i < TIRADAS; i++) {
            assertEquals(esperado.nextDouble(), Azar.getInstance().nextDouble(), 0.0);
        }
    }



}