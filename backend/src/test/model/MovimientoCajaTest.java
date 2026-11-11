package main.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Alejandro
 */

// MovimientoCaja solo es construible por Don, por eso se hace package main.model
// y no package test.model
public class MovimientoCajaTest {
    private static final int HORA_ACTUAL = 5;
    private static final String CONCEPTO = "Compra de GTA VI";
    private static final TMovimiento[] GASTOS = {TMovimiento.COMPRA, TMovimiento.FICHAJE, TMovimiento.FIANZA};

    // No se mockea Reloj por que sus funciones son estaticas y no funciona con mockito 4.9 y TMovimiento tampoco porque es un enum

    @Before
    public void setUp() {
        Reloj.reiniciar();
        fijarHora(HORA_ACTUAL);
    }

    // Como Reloj es un singleton estatico se limpia para evitar interferir en otros tests
    @After
    public void tearDown() {
        Reloj.reiniciar();
    }

    // Avanza la hora hasta el parametro especificado
    private static void fijarHora(int hora) {
        Reloj reloj = Reloj.getInstance();
        for (int i = 0; i < hora; i++) {
            reloj.avanzar();
        }
    }

    // Comprueba que los getters devuelven cada valor recibido
    @Test
    public void testGettersDevuelvenLosValoresDelConstructor() {
        MovimientoCaja m = new MovimientoCaja(3, 2, "Fichaje de Alex Herrero", -250, TMovimiento.FICHAJE);

        assertEquals(3, m.getId());
        assertEquals(2, m.getMomento());
        assertEquals("Fichaje de Alex Herrero", m.getConcepto());
        assertEquals(-250, m.getImporte());
        assertEquals(TMovimiento.FICHAJE, m.getTipo());
    }

    // Comprueba que una comision admite cualquier importe distinto de 0 (positivos y negativos)
    @Test
    public void testComisionAdmiteImportePositivoYNegativo() {
        MovimientoCaja positiva = new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, 1, TMovimiento.COMISION);
        MovimientoCaja negativa = new MovimientoCaja(2, HORA_ACTUAL, CONCEPTO, -1, TMovimiento.COMISION);

        assertEquals(TMovimiento.COMISION, positiva.getTipo());
        assertEquals(1, positiva.getImporte());
        assertEquals(TMovimiento.COMISION, negativa.getTipo());
        assertEquals(-1, negativa.getImporte());
    }

    // Comprueba que cada tipo de gasto admite el importe negativo mas cercano a 0 (-1)
    @Test
    public void testGastosAdmitenImporteMenosUno() {
        for (TMovimiento tipo : GASTOS) {
            MovimientoCaja m = new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, -1, tipo);

            assertEquals(tipo, m.getTipo());
            assertEquals(-1, m.getImporte());
        }
    }

    // Comprueba que un importe que no cabe en un int se guarda entero (sin truncar)
    @Test
    public void testImporteMayorQueUnIntNoSeTrunca() { // Este test se hace pq los botines pueden ser de cientos de millones
        long grande = 5_000_000_000L;

        assertEquals(grande, new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, grande, TMovimiento.COMISION).getImporte());
        assertEquals(-grande, new MovimientoCaja(2, HORA_ACTUAL, CONCEPTO, -grande, TMovimiento.COMPRA).getImporte());
    }

    // Comprueba que el id 0 se rechaza (el minimo valido es 1)
    @Test(expected = IllegalArgumentException.class)
    public void testIdCeroLanzaExcepcion() {
        new MovimientoCaja(0, HORA_ACTUAL, CONCEPTO, -10, TMovimiento.COMPRA);
    }

    // Comprueba que un id negativo se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testIdNegativoLanzaExcepcion() {
        new MovimientoCaja(-1, HORA_ACTUAL, CONCEPTO, -10, TMovimiento.COMPRA);
    }

    // Comprueba que los valores de los limites se aceptan como momentos
    @Test
    public void testMomentoAceptaLosLimitesDelRango() {
        assertEquals(0, new MovimientoCaja(1, 0, CONCEPTO, -10, TMovimiento.COMPRA).getMomento());
        assertEquals(HORA_ACTUAL, new MovimientoCaja(2, HORA_ACTUAL, CONCEPTO, -10, TMovimiento.COMPRA).getMomento());
    }

    // Comprueba que un momento negativo se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testMomentoNegativoLanzaExcepcion() {
        new MovimientoCaja(1, -1, CONCEPTO, -10, TMovimiento.COMPRA);
    }

    // Comprueba que un momento por encima del limite se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testMomentoPosteriorALaHoraActualLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL + 1, CONCEPTO, -10, TMovimiento.COMPRA);
    }

    // Comprueba que el momento se valida contra la hora que marca el Reloj al crear el movimiento y no contra un valor fijo
    @Test
    public void testMomentoSeValidaContraLaHoraActualDelReloj() {
        Reloj.reiniciar();

        assertEquals(0, new MovimientoCaja(1, 0, CONCEPTO, -10, TMovimiento.COMPRA).getMomento());
        try {
            new MovimientoCaja(2, 1, CONCEPTO, -10, TMovimiento.COMPRA);
            fail("Se esperaba un IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Correcto: el momento 1 es posterior a la hora actual (0), el test continua avanzando el reloj
        }

        Reloj.getInstance().avanzar();

        assertEquals(1, new MovimientoCaja(2, 1, CONCEPTO, -10, TMovimiento.COMPRA).getMomento());
    }

    // Comprueba que un concepto null se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testConceptoNuloLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, null, -10, TMovimiento.COMPRA);
    }

    // Comprueba que un concepto vacio se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testConceptoVacioLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, "", -10, TMovimiento.COMPRA);
    }

    // Comprueba que un concepto formado solo por espacios se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testConceptoSoloEspaciosLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, "   ", -10, TMovimiento.COMPRA);
    }

    // Comprueba que un tipo null se rechaza con IllegalArgumentException (no NullPointerException)
    @Test(expected = IllegalArgumentException.class)
    public void testTipoNuloLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, -10, null);
    }

    // Comprueba que una compra con importe 0 se rechaza (un gasto debe ser estrictamente negativo)
    @Test(expected = IllegalArgumentException.class)
    public void testCompraConImporteCeroLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, 0, TMovimiento.COMPRA);
    }

    // Comprueba que una compra con importe positivo se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testCompraConImportePositivoLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, 1, TMovimiento.COMPRA);
    }

    // Comprueba que un fichaje con importe 0 se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testFichajeConImporteCeroLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, 0, TMovimiento.FICHAJE);
    }

    // Comprueba que un fichaje con importe positivo se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testFichajeConImportePositivoLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, 1, TMovimiento.FICHAJE);
    }

    // Comprueba que una fianza con importe 0 se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testFianzaConImporteCeroLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, 0, TMovimiento.FIANZA);
    }

    // Comprueba que una fianza con importe positivo se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testFianzaConImportePositivoLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, 1, TMovimiento.FIANZA);
    }

    // Comprueba que una comision con importe 0 se rechaza (el unico que no admite)
    @Test(expected = IllegalArgumentException.class)
    public void testComisionConImporteCeroLanzaExcepcion() {
        new MovimientoCaja(1, HORA_ACTUAL, CONCEPTO, 0, TMovimiento.COMISION);
    }
}
