package test.model;

import main.model.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Alejandro
 */

@RunWith(MockitoJUnitRunner.class) // Asi se inicializan todos los objetos antes de hacer los tests
public class DonTest {
    private static final long CAJA_INICIAL = 100;

    private Don don;

    // Mocks
    @Mock private MovimientoCaja movimientoCajaMock;
        // Reloj no tiene mock pq con mockito 4.9 Reloj.getInstance() falla por ser estatico

    @Before
    public void setUp() {
        Don.reiniciar();
        Reloj.reiniciar();
        don = Don.getInstance();
    }

    // Como son singletons y son estaticos se limpian para evitar interferir en otros tests
    @After
    public void tearDown() {
        Don.reiniciar();
        Reloj.reiniciar();
    }

    // Comprueba que la caja y el numero de movimientos son los esperados
    private void assertEstado(long cajaEsperada, int movimientosEsperados) {
        assertEquals(cajaEsperada, don.getCaja());
        assertEquals(movimientosEsperados, don.getMovimientos().size());
    }

    // La caja del Don solo crece con comisiones, asi que es la unica forma de dar fondos al Don
    private void llenarCaja(long importe) {
        don.registrarComision(importe, "Ingreso inicial");
    }

    private MovimientoCaja ultimoMovimiento() {
        List<MovimientoCaja> movimientos = don.getMovimientos();
        return movimientos.get(movimientos.size() - 1);
    }

    // Comprueba que getInstance devuelva siempre la misma instancia (singleton)
    @Test
    public void testSiempreMismaInstancia() {
        Don d1 = Don.getInstance();
        Don d2 = Don.getInstance();

        assertNotNull(d1);
        assertSame(d1, d2);
    }

    // Comprueba que el Don empieza con su nombre por defecto, la caja a 0 y sin movimientos
    @Test
    public void testEstadoInicial() {
        assertEquals("Don", don.getNombre());
        assertEstado(0, 0);
    }

    // Comprueba que reiniciar descarta el estado anterior y que el nuevo Don empieza de cero
    @Test
    public void testReiniciarDon() {
        don.setNombre("Alex");
        llenarCaja(CAJA_INICIAL);

        Don.reiniciar();  // Don y no don pq es una funcion estatica
        Don nuevo = Don.getInstance();

        assertNotSame(don, nuevo);
        assertEquals("Don", nuevo.getNombre());
        assertEquals(0, nuevo.getCaja());
        assertTrue(nuevo.getMovimientos().isEmpty());
    }

    // Comprueba que se puede cambiar el nombre por uno valido
    @Test
    public void testSetNombre() {
        don.setNombre("Alex");

        assertEquals("Alex", don.getNombre());
    }

    // Comprueba que un nombre null se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testSetNombreNuloLanzaExcepcion() {
        don.setNombre(null);
    }

    // Comprueba que un nombre vacio se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testSetNombreVacioLanzaExcepcion() {
        don.setNombre("");
    }

    // Comprueba que un nombre formado solo por espacios se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testSetNombreSoloEspaciosLanzaExcepcion() {
        don.setNombre("   ");
    }

    // Comprueba que se puede pagar cualquier importe positivo hasta el total de la caja (incluido)
    // ademas de comprobar que puedePagar() no cambia nada
    @Test
    public void testPuedePagarImporteHastaElTotalDeLaCaja() {
        llenarCaja(CAJA_INICIAL);

        assertTrue(don.puedePagar(1));
        assertTrue(don.puedePagar(CAJA_INICIAL - 1));
        assertTrue(don.puedePagar(CAJA_INICIAL));
        assertEstado(CAJA_INICIAL, 1);
    }

    // Comprueba que no se puede pagar mas de lo que hay en la caja, tampoco con la caja vacia
    @Test
    public void testNoPuedePagarMasDeLoQueHayEnLaCaja() {
        assertFalse(don.puedePagar(1));

        llenarCaja(CAJA_INICIAL);

        assertFalse(don.puedePagar(CAJA_INICIAL + 1));
        assertFalse(don.puedePagar(Long.MAX_VALUE));
    }

    // Comprueba que un importe 0 o negativo nunca se puede pagar, aunque la caja tenga fondos de sobra
    @Test
    public void testNoPuedePagarImportesNoPositivos() {
        llenarCaja(CAJA_INICIAL);

        assertFalse(don.puedePagar(0));
        assertFalse(don.puedePagar(-1));
        assertFalse(don.puedePagar(-CAJA_INICIAL));
        assertFalse(don.puedePagar(Long.MIN_VALUE));
    }

    // Comprueba que pagar con cada tipo valido devuelve true, resta de la caja
    // y registra un unico movimiento negativo de ese tipo
    @Test
    public void testPagarRegistraUnMovimientoNegativoDeCadaTipoValido() {
        TMovimiento[] tipos = {TMovimiento.COMPRA, TMovimiento.FICHAJE, TMovimiento.FIANZA};
        llenarCaja(CAJA_INICIAL);

        for (int i = 0; i < tipos.length; i++) {
            assertTrue(don.pagar(tipos[i], 10, "Pago " + i));

            MovimientoCaja m = ultimoMovimiento();
            assertEquals(tipos[i], m.getTipo());
            assertEquals(-10, m.getImporte());
            assertEquals("Pago " + i, m.getConcepto());
            assertEstado(CAJA_INICIAL - 10 * (i + 1), i + 2);
        }
    }

    // Comprueba que se puede pagar exactamente toda la caja y que esta queda a 0 (nunca negativa)
    @Test
    public void testPagarTodaLaCajaLaDejaACero() {
        llenarCaja(CAJA_INICIAL);

        assertTrue(don.pagar(TMovimiento.COMPRA, CAJA_INICIAL, "Toda la caja"));

        assertEstado(0, 2);
    }

    // Comprueba que sin fondos suficientes pagar devuelve false y no cambia nada
    @Test
    public void testPagarSinFondosDevuelveFalseYNoCambiaNada() {
        llenarCaja(CAJA_INICIAL);

        assertFalse(don.pagar(TMovimiento.COMPRA, CAJA_INICIAL + 1, "Mas del total de la caja"));

        assertEstado(CAJA_INICIAL, 1);
    }

    // Comprueba que no se pueda hacer un pago con el tipo Comision (solo pueden ser COMPRA, FICHAJE y FIANZA)
    @Test(expected = IllegalArgumentException.class)
    public void testPagarConTipoComisionLanzaExcepcion() {
        llenarCaja(CAJA_INICIAL);

        don.pagar(TMovimiento.COMISION, 10, "Pago");
    }

    // Comprueba que un tipo null no es un tipo de pago valido
    @Test(expected = IllegalArgumentException.class)
    public void testPagarConTipoNuloLanzaExcepcion() {
        llenarCaja(CAJA_INICIAL);

        don.pagar(null, 10, "Pago");
    }

    // Comprueba que un importe 0 lanza excepcion aunque haya fondos
    @Test(expected = IllegalArgumentException.class)
    public void testPagarConImporteCeroLanzaExcepcion() {
        llenarCaja(CAJA_INICIAL);

        don.pagar(TMovimiento.COMPRA, 0, "Pago");
    }

    // Comprueba que un importe negativo lanza excepcion aunque haya fondos
    @Test(expected = IllegalArgumentException.class)
    public void testPagarConImporteNegativoLanzaExcepcion() {
        llenarCaja(CAJA_INICIAL);

        don.pagar(TMovimiento.COMPRA, -1, "Pago");
    }

    // Comprueba que pagar con un concepto null lanza excepcion y que el movimiento fallido no llega a modifica la caja
    @Test
    public void testPagarConceptoNuloLanzaExcepcionYNoAlteraElEstado() {
        llenarCaja(CAJA_INICIAL);

        try {
            don.pagar(TMovimiento.COMPRA, 10, null);
            fail("Se esperaba un IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEstado(CAJA_INICIAL, 1);
        }
    }

    // Comprueba que pagar con un concepto null lanza excepcion y que el movimiento fallido no llega a modificar la caja
    @Test
    public void testPagarConceptoEnBlancoLanzaExcepcionYNoAlteraElEstado() {
        llenarCaja(CAJA_INICIAL);

        try {
            don.pagar(TMovimiento.COMPRA, 10, "   ");
            fail("Se esperaba un IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEstado(CAJA_INICIAL, 1);
        }
    }

    // Comprueba que una comision suma a la caja y queda registrada como movimiento COMISION con su importe y concepto
    @Test
    public void testRegistrarComision() {
        don.registrarComision(50, "Comision Robo_Edifici_Vertex");

        MovimientoCaja m = ultimoMovimiento();
        assertEquals(TMovimiento.COMISION, m.getTipo());
        assertEquals(50, m.getImporte());
        assertEquals("Comision Robo_Edifici_Vertex", m.getConcepto());
        assertEstado(50, 1);

        don.registrarComision(30, "Comision Robo_Edifici_Omega");

        assertEstado(80, 2);
    }

    // Comprueba que una comision negativa se admite, resta de la caja y puede dejarla por debajo de 0
    @Test
    public void testRegistrarComisionNegativaPuedeDejarLaCajaEnNegativo() {
        don.registrarComision(-30, "Ajuste");

        assertEquals(TMovimiento.COMISION, ultimoMovimiento().getTipo());
        assertEquals(-30, ultimoMovimiento().getImporte());
        assertEstado(-30, 1);
        assertFalse(don.puedePagar(1));
    }

    // Comprueba que una comision de 0 se ignora sin registrar ni movimiento ni cambio en la caja
    @Test
    public void testRegistrarComisionCeroNoRegistraNada() {
        don.registrarComision(0, "Comision sin sobrante");

        assertEstado(0, 0);
    }

    // Comprueba que registrar una comision con concepto null lanza excepcion y no llega a modificar la caja
    @Test
    public void testRegistrarComisionConceptoNuloLanzaExcepcionYNoAlteraElEstado() {
        llenarCaja(CAJA_INICIAL);

        try {
            don.registrarComision(10, null);
            fail("Se esperaba un IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEstado(CAJA_INICIAL, 1);
        }
    }

    // Comprueba que registrar una comision con concepto en blanco lanza excepcion y no llega a modificar la caja
    @Test
    public void testRegistrarComisionConceptoEnBlancoLanzaExcepcionYNoAlteraElEstado() {
        llenarCaja(CAJA_INICIAL);

        try {
            don.registrarComision(10, "   ");
            fail("Se esperaba un IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEstado(CAJA_INICIAL, 1);
        }
    }

    // Comprueba que cada movimiento guarda la hora que marca el Reloj en el momento de registrarlo
    @Test
    public void testMovimientoGuardaLaHoraActual() {
        Reloj reloj = Reloj.getInstance();
        for (int i = 0; i < 3; i++) {
            reloj.avanzar();
        }
        llenarCaja(CAJA_INICIAL);

        assertEquals(3, ultimoMovimiento().getMomento());

        reloj.avanzar();
        reloj.avanzar();
        don.pagar(TMovimiento.COMPRA, 10, "Pago");

        assertEquals(5, ultimoMovimiento().getMomento());
        assertEquals(3, don.getMovimientos().get(0).getMomento());  // el primero conserva su hora
    }

    // Comprueba que los ids van desde 1 y en orden de registro
    // y que la caja siempre es la suma de los importes de los movimientos
    @Test
    public void testIdsCorrelativosYCajaIgualASumaDeMovimientos() {
        llenarCaja(CAJA_INICIAL);                                                           // id 1, +100
        don.pagar(TMovimiento.COMPRA, 30, "Compra");                       // id 2, -30
        assertFalse(don.pagar(TMovimiento.FICHAJE, 1000, "Imposible"));    // rechazado (no registra nada)
        don.registrarComision(5, "Comision");                              // id 3, +5
        don.pagar(TMovimiento.FIANZA, 20, "Fianza");                       // id 4, -20

        List<MovimientoCaja> movimientos = don.getMovimientos();
        long suma = 0;
        assertEquals(4, movimientos.size());
        for (int i = 0; i < movimientos.size(); i++) {
            assertEquals(i + 1, movimientos.get(i).getId());
            suma += movimientos.get(i).getImporte();
        }

        assertEquals(55, suma);
        assertEquals(suma, don.getCaja());
    }

    // Comprueba que getMovimientos devuelve una lista inmodificable
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMovimientosEsInmodificable() {
        llenarCaja(CAJA_INICIAL);

        don.getMovimientos().add(movimientoCajaMock);
    }

    // Comprueba que movimientosPorTipo devuelve solo los movimientos de ese tipo
    // y en orden de registr y una lista vacia si no hay ninguno
    @Test
    public void testMovimientosPorTipoFiltraYConservaElOrden() {
        llenarCaja(CAJA_INICIAL);                           // COMISION
        don.pagar(TMovimiento.COMPRA, 10, "Compra 1");      // COMPRA
        don.registrarComision(5, "Comision");               // COMISION
        don.pagar(TMovimiento.COMPRA, 10, "Compra 2");      // COMPRA
        don.pagar(TMovimiento.FICHAJE, 10, "Fichaje");      // FICHAJE
        List<MovimientoCaja> todos = don.getMovimientos();

        // MovimientoCaja no define equals, asi que la comparacion debe comprobar que son los mismos objetos)
        assertEquals(List.of(todos.get(0), todos.get(2)), don.movimientosPorTipo(TMovimiento.COMISION));
        assertEquals(List.of(todos.get(1), todos.get(3)), don.movimientosPorTipo(TMovimiento.COMPRA));
        assertEquals(List.of(todos.get(4)), don.movimientosPorTipo(TMovimiento.FICHAJE));
        assertTrue(don.movimientosPorTipo(TMovimiento.FIANZA).isEmpty());
    }

    // Comprueba que movimientosPorTipo con un tipo null lanza excepcion
    @Test(expected = IllegalArgumentException.class)
    public void testMovimientosPorTipoNuloLanzaExcepcion() {
        don.movimientosPorTipo(null);
    }
}
