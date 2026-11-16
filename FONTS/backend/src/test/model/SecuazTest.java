package test.model;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import main.model.Ejecucion;
import main.model.Participacion;
import main.model.Secuaz;
import main.model.TConsecuencia;
import main.model.TEstadoSec;
import main.model.TResultado;
import main.model.TRol;

/**
 * @author Noa
 */
public class SecuazTest {

    private Secuaz secuaz;
    private Secuaz ana;
    private Secuaz bea;

    //Una ejecución simulada por cada resultado posible.
    @Mock private Ejecucion ejecExito;
    @Mock private Ejecucion ejecFracaso;
    @Mock private Ejecucion ejecEnCurso;
    @Mock private Ejecucion ejecEnCurso2; //Esta se usa para demostrar que no se cuentan las EnCurso


    //Participaciones simuladas del secuaz, una en cada ejecución.
    @Mock private Participacion partExito;
    @Mock private Participacion partFracaso;
    @Mock private Participacion partEnCurso;
    @Mock private Participacion partEnCurso2; //Complementa a ejecEnCurso2

    //Participaciones simuladas para el cómplice habitual.
    @Mock private Participacion partAna;
    @Mock private Participacion partBea;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        secuaz = new Secuaz(1, "Carlos", "El Rapido", TRol.CONDUCTOR);
        ana = new Secuaz(2, "Ana", null, TRol.HACKER);
        bea = new Secuaz(3, "Bea", null, TRol.ASALTO);

        when(ejecExito.getResultado()).thenReturn(TResultado.EXITO);
        when(ejecFracaso.getResultado()).thenReturn(TResultado.FRACASO);
        when(ejecEnCurso.getResultado()).thenReturn(TResultado.EN_CURSO);
        when(ejecEnCurso2.getResultado()).thenReturn(TResultado.EN_CURSO);

        when(partExito.getSecuaz()).thenReturn(secuaz);
        when(partExito.getEjecucion()).thenReturn(ejecExito);
        when(partFracaso.getSecuaz()).thenReturn(secuaz);
        when(partFracaso.getEjecucion()).thenReturn(ejecFracaso);
        when(partEnCurso.getSecuaz()).thenReturn(secuaz);
        when(partEnCurso.getEjecucion()).thenReturn(ejecEnCurso);
        when(partEnCurso2.getSecuaz()).thenReturn(secuaz);
        when(partEnCurso2.getEjecucion()).thenReturn(ejecEnCurso2);

        when(partAna.getSecuaz()).thenReturn(ana);
        when(partBea.getSecuaz()).thenReturn(bea);
    }

    //Para no repetir el proceso de detención
    private void detener(Secuaz s, int horaActual) {
        s.ocupar();
        s.procesarConsecuencia(TConsecuencia.DETENIDO, horaActual);
    }

    //Comprueba que el constructor inicializa correctamente todos los atributos.
    @Test
    public void testCreacionSecuaz() {
        assertEquals(1, secuaz.getId());
        assertEquals("Carlos", secuaz.getNombre());
        assertEquals("El Rapido", secuaz.getApodo());
        assertEquals(TRol.CONDUCTOR, secuaz.getEspecialidad());
        assertEquals(TEstadoSec.LIBRE, secuaz.getEstado());
        assertEquals(0L, secuaz.getDineroAcumulado());
        assertEquals(0L, secuaz.getFianza());
        assertEquals(0, secuaz.getLiberacion());
        assertTrue(secuaz.getParticipaciones().isEmpty());
        assertTrue(secuaz.esLibre());
    }

    //Comprueba que no se puede crear un secuaz con id cero.
    @Test(expected = IllegalArgumentException.class)
    public void testIdCeroExcepcion() {
        new Secuaz(0, "Carlos", "El Rapido", TRol.CONDUCTOR);
    }

    //Comprueba que no se puede crear un secuaz con id negativo.
    @Test(expected = IllegalArgumentException.class)
    public void testIdNegativoExcepcion() {
        new Secuaz(-1, "Carlos", "El Rapido", TRol.CONDUCTOR);
    }

    //Comprueba que no se puede crear un secuaz con nombre nulo.
    @Test(expected = IllegalArgumentException.class)
    public void testNombreNuloExcepcion() {
        new Secuaz(1, null, "El Rapido", TRol.CONDUCTOR);
    }

    //Comprueba que no se puede crear un secuaz con nombre vacío.
    @Test(expected = IllegalArgumentException.class)
    public void testNombreVacioExcepcion() {
        new Secuaz(1, "", "El Rapido", TRol.CONDUCTOR);
    }

    //Comprueba que no se puede crear un secuaz con un nombre formado solo por espacios.
    @Test(expected = IllegalArgumentException.class)
    public void testNombreSoloEspaciosExcepcion() {
        new Secuaz(1, "   ", "El Rapido", TRol.CONDUCTOR);
    }

    //Comprueba que no se puede crear un secuaz con especialidad nula.
    @Test(expected = IllegalArgumentException.class)
    public void testEspecialidadNulaExcepcion() {
        new Secuaz(1, "Carlos", "El Rapido", null);
    }

    //Comprueba que se puede crear un secuaz sin apodo.
    @Test
    public void testApodoNuloValido() {
        Secuaz sinApodo = new Secuaz(1, "Carlos", null, TRol.CONDUCTOR);
        assertNull(sinApodo.getApodo());
    }

    //Comprueba que 1 es un id válido, es el limite inferior.
    @Test
    public void testIdMinimoValido() {
        Secuaz primero = new Secuaz(1, "Carlos", "El Rapido", TRol.CONDUCTOR);
        assertEquals(1, primero.getId());
    }

    //Comprueba que se puede cambiar el nombre por uno válido.
    @Test
    public void testSetNombre() {
        secuaz.setNombre("Pedro");
        assertEquals("Pedro", secuaz.getNombre());
    }

    //Comprueba que no se puede cambiar el nombre a nulo y que el nombre anterior se conserva.
    @Test
    public void testSetNombreNuloExcepcion() {
        try {
            secuaz.setNombre(null);
            fail("Debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertEquals("Carlos", secuaz.getNombre());
    }

    //Comprueba que no se puede cambiar el nombre a uno vacío o solo con espacios.
    @Test
    public void testSetNombreVacioExcepcion() {
        try {
            secuaz.setNombre("");
            fail("Debería lanzar IllegalArgumentException con nombre vacío");
        } catch (IllegalArgumentException e) {
        }
        try {
            secuaz.setNombre("   ");
            fail("Debería lanzar IllegalArgumentException con nombre solo con espacios");
        } catch (IllegalArgumentException e) {
        }
        assertEquals("Carlos", secuaz.getNombre());
    }

    //Comprueba que se puede cambiar el apodo
    @Test
    public void testSetApodo() {
        secuaz.setApodo("El Lento");
        assertEquals("El Lento", secuaz.getApodo());
    }

    //Comprueba que se puede cambiar el apodo a nulo.
    @Test
    public void testSetApodoNulo() {

        secuaz.setApodo(null);
        assertNull(secuaz.getApodo());
    }

    //Comprueba que un secuaz libre pasa a ocupado.
    @Test
    public void testOcuparLibre() {
        assertTrue(secuaz.ocupar());
        assertEquals(TEstadoSec.OCUPADO, secuaz.getEstado());
        assertFalse(secuaz.esLibre());
    }

    //Comprueba que ocupar a un secuaz ocupado devuelve falso.
    @Test
    public void testOcuparOcupado() {
        secuaz.ocupar();

        assertFalse(secuaz.ocupar());
        assertEquals(TEstadoSec.OCUPADO, secuaz.getEstado());
    }

    //Comprueba que no se puede ocupar a un secuaz detenido.
    @Test
    public void testOcuparDetenido() {
        detener(secuaz, 0);

        assertFalse(secuaz.ocupar());
        assertEquals(TEstadoSec.DETENIDO, secuaz.getEstado());
    }

    //Comprueba que no se puede ocupar a un secuaz expulsado.
    @Test
    public void testOcuparExpulsado() {
        secuaz.expulsar();

        assertFalse(secuaz.ocupar());
        assertEquals(TEstadoSec.EXPULSADO, secuaz.getEstado());
    }

    //Comprueba que se puede expulsar a un secuaz libre.
    @Test
    public void testExpulsarLibre() {
        assertTrue(secuaz.expulsar());
        assertEquals(TEstadoSec.EXPULSADO, secuaz.getEstado());
    }

    //Comprueba que no se puede expulsar a un secuaz ocupado.
    @Test
    public void testExpulsarOcupado() {
        secuaz.ocupar();

        assertFalse(secuaz.expulsar());
        assertEquals(TEstadoSec.OCUPADO, secuaz.getEstado());
    }

    //Comprueba que no se puede expulsar a un secuaz detenido.
    @Test
    public void testExpulsarDetenido() {
        detener(secuaz, 0);

        assertFalse(secuaz.expulsar());
        assertEquals(TEstadoSec.DETENIDO, secuaz.getEstado());
    }

    //Comprueba que expulsar a un secuaz expulsado devuelve falso.
    @Test
    public void testExpulsarDosVeces() {
        secuaz.expulsar();

        assertFalse(secuaz.expulsar());
        assertEquals(TEstadoSec.EXPULSADO, secuaz.getEstado());
    }

    //Comprueba que las ganancias se suman.
    @Test
    public void testAcumularGanancia() {
        secuaz.acumularGanancia(1000L);
        secuaz.acumularGanancia(500L);

        assertEquals(1500L, secuaz.getDineroAcumulado());
    }

    //Comprueba que acumular 0 es válido y no cambia nada.
    @Test
    public void testAcumularGananciaCero() {
        secuaz.acumularGanancia(0L);
        assertEquals(0L, secuaz.getDineroAcumulado());
    }

    //Comprueba que no se pueden acumular ganancias negativas y que el dinero no cambia.
    @Test
    public void testAcumularGananciaNegativaExcepcion() {
        secuaz.acumularGanancia(1000L);

        try {
            secuaz.acumularGanancia(-1L);
            fail("Debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(1000L, secuaz.getDineroAcumulado());
    }

    //Comprueba que se puede añadir una participación correctamente.
    @Test
    public void testAñadirParticipacion() {
        secuaz.añadirParticipacion(partExito);

        assertEquals(1, secuaz.getParticipaciones().size());
        assertSame(partExito, secuaz.getParticipaciones().get(0));
    }

    //Comprueba que no se puede añadir una participación nula.
    @Test
    public void testAñadirParticipacionNulaExcepcion() {
        try {
            secuaz.añadirParticipacion(null);
            fail("Debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertTrue(secuaz.getParticipaciones().isEmpty());
    }

    //Comprueba que un secuaz sin participaciones no tiene misiones.
    @Test
    public void testSinMisiones() {
        assertEquals(0, secuaz.numMisiones());
    }

    //Comprueba que un secuaz sin participaciones no tiene hoja de servicios.
    @Test
    public void testSinHojaServicios() {
        assertTrue(secuaz.hojaDeServicios().isEmpty());
    }

    //Comprueba que solo cuentan las misiones terminadas, no las que están en curso.
    @Test
    public void testNumMisionesIgnoraEnCurso() {
        secuaz.añadirParticipacion(partExito);
        secuaz.añadirParticipacion(partFracaso);
        secuaz.añadirParticipacion(partEnCurso);

        assertEquals(2, secuaz.numMisiones());
    }

    //Comprueba que la hoja de servicios solo incluye las participaciones terminadas.
    @Test
    public void testHojaDeServiciosIgnoraEnCurso() {
        secuaz.añadirParticipacion(partExito);
        secuaz.añadirParticipacion(partEnCurso);
        secuaz.añadirParticipacion(partFracaso);

        assertEquals(Arrays.asList(partExito, partFracaso), secuaz.hojaDeServicios());
    }

    //Comprueba que sin misiones ni dinero la fianza es la fianza base de su especialidad.
    @Test
    public void testCalcularFianzaBase() {
        assertEquals(TRol.CONDUCTOR.getFianzaBase(), secuaz.calcularFianza());
    }

    //Comprueba que la fórmula (base + 100 por misión terminada + 10% del dinero acumulado)
    //se aplica correctamente.
    @Test
    public void testCalcularFianzaConMisionesYDinero() {
        secuaz.añadirParticipacion(partExito);
        secuaz.añadirParticipacion(partFracaso);
        secuaz.añadirParticipacion(partEnCurso); //no cuenta
        secuaz.acumularGanancia(5000L);

        long esperado = TRol.CONDUCTOR.getFianzaBase() + 100L * 2 + 500L;
        assertEquals(esperado, secuaz.calcularFianza());
    }

    //Comprueba que el 10% del dinero se redondea al entero más cercano.
    @Test
    public void testCalcularFianzaRedondeo() {
        secuaz.acumularGanancia(15L);

        assertEquals(TRol.CONDUCTOR.getFianzaBase() + 2L, secuaz.calcularFianza());
    }

    //Comprueba que sin consecuencias el secuaz vuelve a estar libre.
    @Test
    public void testProcesarConsecuenciaNinguna() {
        secuaz.ocupar();
        secuaz.procesarConsecuencia(TConsecuencia.NINGUNA, 10);

        assertEquals(TEstadoSec.LIBRE, secuaz.getEstado());
        assertEquals(0L, secuaz.getFianza());
        assertEquals(0, secuaz.getLiberacion());
    }

    //Comprueba que al ser detenido se calcula la fianza y la hora de liberación.
    @Test
    public void testProcesarConsecuenciaDetenido() {
        secuaz.acumularGanancia(1000L);
        long fianzaEsperada = secuaz.calcularFianza();

        detener(secuaz, 10);

        assertEquals(TEstadoSec.DETENIDO, secuaz.getEstado());
        assertEquals(fianzaEsperada, secuaz.getFianza());
        assertEquals(10 + Secuaz.HORAS_PRISION, secuaz.getLiberacion());
    }

    //Comprueba que al morir el secuaz queda muerto sin fianza ni hora de liberación.
    @Test
    public void testProcesarConsecuenciaMuerto() {
        secuaz.ocupar();
        secuaz.procesarConsecuencia(TConsecuencia.MUERTO, 10);

        assertEquals(TEstadoSec.MUERTO, secuaz.getEstado());
        assertEquals(0L, secuaz.getFianza());
        assertEquals(0, secuaz.getLiberacion());
    }

    //Comprueba que no se puede procesar una consecuencia si el secuaz no está ocupado.
    @Test
    public void testProcesarConsecuenciaSinOcuparExcepcion() {
        try {
            secuaz.procesarConsecuencia(TConsecuencia.NINGUNA, 10);
            fail("Debería lanzar IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertEquals(TEstadoSec.LIBRE, secuaz.getEstado());
    }

    //Comprueba que no se puede procesar una consecuencia nula.
    @Test
    public void testProcesarConsecuenciaNulaExcepcion() {
        secuaz.ocupar();

        try {
            secuaz.procesarConsecuencia(null, 10);
            fail("Debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(TEstadoSec.OCUPADO, secuaz.getEstado());
    }

    //Comprueba que un secuaz muerto no puede volver a ocuparse.
    @Test
    public void testMuertoNoSePuedeOcupar() {
        secuaz.ocupar();
        secuaz.procesarConsecuencia(TConsecuencia.MUERTO, 10);

        assertFalse(secuaz.ocupar());
        assertEquals(TEstadoSec.MUERTO, secuaz.getEstado());
    }

    //Comprueba que un secuaz muerto no puede expulsarse.
    @Test
    public void testMuertoNoSePuedeExpulsar() {
        secuaz.ocupar();
        secuaz.procesarConsecuencia(TConsecuencia.MUERTO, 10);

        assertFalse(secuaz.expulsar());
        assertEquals(TEstadoSec.MUERTO, secuaz.getEstado());
    }

    //Comprueba que un secuaz no detenido no tiene tiempo de prisión.
    @Test
    public void testTiempoRestanteNoDetenido() {
        assertEquals(0, secuaz.getTiempoRestantePrision(100));
    }

    //Comprueba que un secuaz detenido devuelve las horas que le quedan.
    @Test
    public void testTiempoRestanteDetenido() {
        detener(secuaz, 10);

        assertEquals(Secuaz.HORAS_PRISION, secuaz.getTiempoRestantePrision(10));
        assertEquals(1, secuaz.getTiempoRestantePrision(10 + Secuaz.HORAS_PRISION - 1));
    }

    // Comprueba que si ya ha pasado la hora de liberación devuelve -1.
    @Test
    public void testTiempoRestanteCumplido() {
        detener(secuaz, 10);
        assertEquals(-1, secuaz.getTiempoRestantePrision(500));
    }

    //Comprueba que si es la hora de liberación devuelve -1.
    @Test
    public void testTiempoRestanteExactoCumplido() {
        detener(secuaz, 10);
        assertEquals(-1, secuaz.getTiempoRestantePrision(10 + Secuaz.HORAS_PRISION));
    }

    //Comprueba que no se libera antes de la hora de liberación.
    @Test
    public void testLiberacionAutomaticaAntesDeTiempo() {
        detener(secuaz, 10);

        assertFalse(secuaz.comprobarLiberacionAutomatica(10 + Secuaz.HORAS_PRISION - 1));
        assertEquals(TEstadoSec.DETENIDO, secuaz.getEstado());
    }

    //Comprueba que se libera justo a la hora de liberación.
    @Test
    public void testLiberacionAutomaticaHoraExacta() {
        detener(secuaz, 10);

        assertTrue(secuaz.comprobarLiberacionAutomatica(10 + Secuaz.HORAS_PRISION));
        assertEquals(TEstadoSec.LIBRE, secuaz.getEstado());
        assertEquals(0L, secuaz.getFianza());
        assertEquals(0, secuaz.getLiberacion());
    }

    //Comprueba no cambia de estado a un secuaz no detenido.
    @Test
    public void testLiberacionAutomaticaNoDetenido() {
        assertFalse(secuaz.comprobarLiberacionAutomatica(1000));
        assertEquals(TEstadoSec.LIBRE, secuaz.getEstado());
    }

    //Comprueba que liberar a un detenido lo deja libre y sin fianza.
    @Test
    public void testLiberar() {
        detener(secuaz, 10);
        secuaz.liberar();

        assertEquals(TEstadoSec.LIBRE, secuaz.getEstado());
        assertEquals(0L, secuaz.getFianza());
        assertEquals(0, secuaz.getLiberacion());
    }

    //Comprueba que no se puede liberar a un secuaz que no está detenido.
    @Test
    public void testLiberarNoDetenidoExcepcion() {
        try {
            secuaz.liberar();
            fail("Debería lanzar IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertEquals(TEstadoSec.LIBRE, secuaz.getEstado());
    }

    //Comprueba que no se puede liberar a un secuaz muerto.
    @Test
    public void testLiberarMuertoExcepcion() {
        secuaz.ocupar();
        secuaz.procesarConsecuencia(TConsecuencia.MUERTO, 10);

        try {
            secuaz.liberar();
            fail("Debería lanzar IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertEquals(TEstadoSec.MUERTO, secuaz.getEstado());
    }

    //Comprueba que sin misiones no hay cómplice habitual.
    @Test
    public void testCompliceHabitualSinMisiones() {
        assertNull(secuaz.obtenerCompliceHabitual());
    }

    //Comprueba que devuelve el compañero con el que más misiones terminadas ha compartido.
    //Ana coincide en 2 misiones y Bea en 1.
    @Test
    public void testCompliceHabitual() {
        //La participacion que fue un exito fue con Ana
        when(ejecExito.getParticipaciones()).thenReturn(Arrays.asList(partExito, partAna));
        //La participacion que fue un fracaso fue con ambas
        when(ejecFracaso.getParticipaciones()).thenReturn(Arrays.asList(partFracaso, partAna, partBea));
        secuaz.añadirParticipacion(partExito);
        secuaz.añadirParticipacion(partFracaso);

        assertSame(ana, secuaz.obtenerCompliceHabitual());
    }

    //Comprueba que las misiones en curso no cuentan para el cómplice habitual.
    @Test
    public void testCompliceHabitualIgnoraEnCurso() {
        when(ejecEnCurso.getParticipaciones()).thenReturn(Arrays.asList(partEnCurso, partBea));
        when(ejecEnCurso2.getParticipaciones()).thenReturn(Arrays.asList(partEnCurso2, partBea));
        when(ejecExito.getParticipaciones()).thenReturn(Arrays.asList(partExito, partAna));
        secuaz.añadirParticipacion(partEnCurso);
        secuaz.añadirParticipacion(partEnCurso2);
        secuaz.añadirParticipacion(partExito);

        assertSame(ana, secuaz.obtenerCompliceHabitual());
    }

    //Comprueba que ante un empate devuelve el compañero que aparece antes en la lista.
    @Test
    public void testCompliceHabitualEmpate() {
        when(ejecExito.getParticipaciones()).thenReturn(Arrays.asList(partExito, partAna));
        when(ejecFracaso.getParticipaciones()).thenReturn(Arrays.asList(partFracaso, partBea));
        secuaz.añadirParticipacion(partExito);
        secuaz.añadirParticipacion(partFracaso);

        assertSame(ana, secuaz.obtenerCompliceHabitual());
    }
}
