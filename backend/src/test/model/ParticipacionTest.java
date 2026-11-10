package test.model;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import main.model.Ejecucion;
import main.model.Participacion;
import main.model.Secuaz;
import main.model.TConsecuencia;
import main.model.TResultado;
import main.model.TRol;

public class ParticipacionTest {

    @Mock private Secuaz secuaz;
    @Mock private Ejecucion ejecucion;
    private Participacion participacion;


    //Por defecto el secuaz es hacker, hace de hacker y la ejecución ha acabado con éxito.
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(secuaz.getEspecialidad()).thenReturn(TRol.HACKER);
        when(ejecucion.getResultado()).thenReturn(TResultado.EXITO);

        participacion = new Participacion(secuaz, ejecucion, TRol.HACKER);
    }

    //Cambia el resultado de la ejecución simulada.
    private void resultadoEjecucion(TResultado resultado) {
        when(ejecucion.getResultado()).thenReturn(resultado);
    }

    //Comprueba que el constructor inicializa correctamente todos los atributos.
    @Test
    public void testCreacionParticipacion() {
        assertSame(secuaz, participacion.getSecuaz());
        assertSame(ejecucion, participacion.getEjecucion());
        assertEquals(TRol.HACKER, participacion.getRolDesempenado());
        assertEquals(0L, participacion.getCobrado());
        assertEquals(TConsecuencia.NINGUNA, participacion.getConsecuencia());
    }

    //Comprueba que no se puede crear una participación con secuaz nulo.
    @Test(expected = IllegalArgumentException.class)
    public void testSecuazNuloExcepcion() {
        new Participacion(null, ejecucion, TRol.HACKER);
    }

    //Comprueba que no se puede crear una participación con ejecución nula.
    @Test(expected = IllegalArgumentException.class)
    public void testEjecucionNulaExcepcion() {
        new Participacion(secuaz, null, TRol.HACKER);
    }

    //Comprueba que no se puede crear una participación con rol nulo.
    @Test(expected = IllegalArgumentException.class)
    public void testRolNuloExcepcion() {
        new Participacion(secuaz, ejecucion, null);
    }

    //Comprueba que es en su especialidad si el rol coincide con la especialidad del secuaz.
    @Test
    public void testEsEnSuEspecialidad() {
        assertTrue(participacion.esEnSuEspecialidad());
    }

    //Comprueba que no es en su especialidad si el rol no coincide.
    @Test
    public void testNoEsEnSuEspecialidad() {
        Participacion otroRol = new Participacion(secuaz, ejecucion, TRol.CONDUCTOR);
        assertFalse(otroRol.esEnSuEspecialidad());
    }

    //Comprueba que el cobro es el porcentaje de comisión del rol sobre el botín y que se guarda.
    @Test
    public void testLiquidarCobro() {
        long esperado = (long) (10000L * TRol.HACKER.getComision() / 100);

        assertEquals(esperado, participacion.liquidarCobro(10000L));
        assertEquals(esperado, participacion.getCobrado());
    }

    //Comprueba que el cobro se trunca y no se redondea.
    @Test
    public void testLiquidarCobroTrunca() {
        //3% de 1050 = 31.5
        assertEquals(31L, participacion.liquidarCobro(1050L));
        assertEquals(31L, participacion.getCobrado());
    }

    //Comprueba que la comisión depende del rol de espacialidad, no del desempeñado
    @Test
    public void testLiquidarCobroUsaRolEspecialidad() {
        Participacion comoConductor = new Participacion(secuaz, ejecucion, TRol.CONDUCTOR);
        long esperado = (long) (10000L * TRol.HACKER.getComision() / 100);

        assertEquals(esperado, comoConductor.liquidarCobro(10000L));
    }

    //Comprueba que con botín 0 se cobra 0.
    @Test
    public void testLiquidarCobroBotinCero() {
        assertEquals(0L, participacion.liquidarCobro(0L));
        assertEquals(0L, participacion.getCobrado());
    }

    //Comprueba que no se puede cobrar de una misión en curso y que el cobro sigue a 0.
    @Test
    public void testLiquidarCobroEnCursoExcepcion() {
        resultadoEjecucion(TResultado.EN_CURSO);

        try {
            participacion.liquidarCobro(10000L);
            fail("Debería lanzar IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertEquals(0L, participacion.getCobrado());
    }

    //Comprueba que no se puede cobrar de una misión fracasada y que el cobro sigue a 0.
    @Test
    public void testLiquidarCobroFracasoExcepcion() {
        resultadoEjecucion(TResultado.FRACASO);

        try {
            participacion.liquidarCobro(10000L);
            fail("Debería lanzar IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertEquals(0L, participacion.getCobrado());
    }

    //Comprueba que una misión con éxito puede acabar sin consecuencias.
    @Test
    public void testAplicarConsecuenciaNingunaExito() {
        participacion.aplicarConsecuencia(TConsecuencia.NINGUNA, 10);

        assertEquals(TConsecuencia.NINGUNA, participacion.getConsecuencia());
    }

    //Comprueba que una misión fracasada también puede acabar sin consecuencias.
    @Test
    public void testAplicarConsecuenciaNingunaFracaso() {
        resultadoEjecucion(TResultado.FRACASO);
        participacion.aplicarConsecuencia(TConsecuencia.NINGUNA, 10);

        assertEquals(TConsecuencia.NINGUNA, participacion.getConsecuencia());
    }

    //Comprueba que en una misión fracasada el secuaz puede ser detenido.
    @Test
    public void testAplicarConsecuenciaDetenidoFracaso() {
        resultadoEjecucion(TResultado.FRACASO);
        participacion.aplicarConsecuencia(TConsecuencia.DETENIDO, 10);

        assertEquals(TConsecuencia.DETENIDO, participacion.getConsecuencia());
    }

    //Comprueba que en una misión fracasada el secuaz puede morir.
    @Test
    public void testAplicarConsecuenciaMuertoFracaso() {
        resultadoEjecucion(TResultado.FRACASO);
        participacion.aplicarConsecuencia(TConsecuencia.MUERTO, 10);

        assertEquals(TConsecuencia.MUERTO, participacion.getConsecuencia());
    }

    //Comprueba que no se puede aplicar una consecuencia nula.
    @Test
    public void testAplicarConsecuenciaNulaExcepcion() {
        try {
            participacion.aplicarConsecuencia(null, 10);
            fail("Debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(TConsecuencia.NINGUNA, participacion.getConsecuencia());
    }

    //Comprueba que no se puede aplicar una consecuencia si la misión sigue en curso.
    @Test
    public void testAplicarConsecuenciaEnCursoExcepcion() {
        resultadoEjecucion(TResultado.EN_CURSO);

        try {
            participacion.aplicarConsecuencia(TConsecuencia.NINGUNA, 10);
            fail("Debería lanzar IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertEquals(TConsecuencia.NINGUNA, participacion.getConsecuencia());
    }

    //Comprueba que no se puede detener al secuaz si la misión ha tenido éxito.
    @Test
    public void testAplicarConsecuenciaDetenidoExitoExcepcion() {
        try {
            participacion.aplicarConsecuencia(TConsecuencia.DETENIDO, 10);
            fail("Debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(TConsecuencia.NINGUNA, participacion.getConsecuencia());
    }

    //Comprueba que el secuaz no puede morir si la misión ha tenido éxito.
    @Test
    public void testAplicarConsecuenciaMuertoExitoExcepcion() {
        try {
            participacion.aplicarConsecuencia(TConsecuencia.MUERTO, 10);
            fail("Debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(TConsecuencia.NINGUNA, participacion.getConsecuencia());
    }
}
