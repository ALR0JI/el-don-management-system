package test.model;

import main.model.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Alejandro
*/

public class RelojTest {
    @Before
    public void setUp() {
        Reloj.reiniciar();
    }

    // Comprueba que el reloj incialmente tiene el valor de hora como 0
    @Test
    public void testHoraIncialEsCero() {
        assertEquals(0, Reloj.getInstance().horaActual());
    }

    // Comprueba que getInstance devuelva siempre la misma instancia y, por lo tanto, sea un singleton
    @Test
    public void testSiempreMismaInstancia() {
        Reloj r1 = Reloj.getInstance();
        Reloj r2 = Reloj.getInstance();

        assertNotNull(r1);
        assertSame(r1, r2);
    }

    // Comprueba que consultar la hora no modifica el valor
    @Test
    public void testConsultarHoraNoCambia() {
        Reloj reloj = Reloj.getInstance();

        for (int i = 0; i < 5; i++) {
            assertEquals(0, reloj.horaActual());
        }
    }

    // Comprueba que cada avance de hora suma exactamente 1 tambien al pasar de las 24, 48 y 72 horas
    @Test
    public void testAvanzarSumaUnaHoraCadaVez() {
        Reloj reloj = Reloj.getInstance();

        for (int esperada = 1; esperada <= 200; esperada++) {
            reloj.avanzar();
            assertEquals(esperada, reloj.horaActual());
        }
    }

    // Comprueba que la hora avanza desde cualquier referencia a reloj
    @Test
    public void testHoraAvanzaDesdeCualquierReferencia() {
        Reloj r1 = Reloj.getInstance();
        Reloj r2 = Reloj.getInstance();

        r1.avanzar(); r1.avanzar(); r2.avanzar();
        assertEquals(3, r1.horaActual());
        assertEquals(3, r2.horaActual());
        assertEquals(3, Reloj.getInstance().horaActual());
    }

    // Comprueba que reinciar devuelve la hora a 0 y que el conteo empieza desde ahi
    @Test
    public void testReinciarPoneHoraACero() {
        Reloj reloj = Reloj.getInstance();
        for (int i = 0; i < 5; i++) {
            reloj.avanzar();
        }

        assertEquals(5, reloj.horaActual());

        Reloj.reiniciar();  // Reloj y no reloj pq es una funcion estatica

        // No se puede usar reloj porque apunta a una variable antigua con hora 5
        assertEquals(0, Reloj.getInstance().horaActual());
        Reloj.getInstance().avanzar();
        assertEquals(1, Reloj.getInstance().horaActual());
    }

    // Comprueba que reiniciar varias veces seguidas y sin haber usado el reloj no falla.
    @Test
    public void testReiniciarRepetidoNoFalla() {
        Reloj.reiniciar();
        Reloj.reiniciar();

        assertEquals(0, Reloj.getInstance().horaActual());
    }



}
