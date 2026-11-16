package test.model;

import org.junit.*;
import static org.junit.Assert.*;

import main.model.Candidato;
import main.model.TRol;

/**
 * @author Noa
 */
public class CandidatoTest {

    private Candidato candidato;

    @Before
    public void setUp() {
        candidato = new Candidato("Carlos", "El Rapido", 1000000L, TRol.CONDUCTOR);
    }

    //Comprueba que el constructor inicializa correctamente todos los atributos
    //y que el candidato empieza sin estar en oferta ni fichado.
    @Test
    public void testCreacionCandidato() {
        assertEquals("Carlos", candidato.getNombre());
        assertEquals("El Rapido", candidato.getApodo());
        assertEquals(1000000L, candidato.getPrecioFichaje());
        assertEquals(TRol.CONDUCTOR, candidato.getEspecialidad());
        assertFalse(candidato.isEnOferta());
        assertFalse(candidato.isFichado());
    }

    //Comprueba que el candidato es elegible al crearse.
    @Test
    public void testEsElegible() {
        assertTrue(candidato.esElegible());
    }

    //Comprueba que no se puede crear un candidato con nombre nulo.
    @Test(expected = IllegalArgumentException.class)
    public void testNombreNuloExcepcion() {
        new Candidato(null, "El Rapido", 1000000L, TRol.CONDUCTOR);
    }

    //Comprueba que no se puede crear un candidato con especialidad nula.
    @Test(expected = IllegalArgumentException.class)
    public void testEspecialidadNulaExcepcion() {
        new Candidato("Carlos", "El Rapido", 1000000L, null);
    }

    //Comprueba que no se puede crear un candidato con precio de fichaje cero.
    @Test(expected = IllegalArgumentException.class)
    public void testPrecioCeroExcepcion() {
        new Candidato("Carlos", "El Rapido", 0L, TRol.CONDUCTOR);
    }

    //Comprueba que no se puede crear un candidato con precio negativo.
    @Test(expected = IllegalArgumentException.class)
    public void testPrecioNegativoExcepcion() {
        new Candidato("Carlos", "El Rapido", -100L, TRol.CONDUCTOR);
    }

    //Comprueba que crear un candidato sin apodo no hace
    //saltar ninguna excepción.
    @Test
    public void testApodoNuloValido() {
        Candidato sinApodo = new Candidato("Carlos", null, 100L, TRol.CONDUCTOR);
        assertNull(sinApodo.getApodo());
    }

    //Comprueba que se puede poner un candidato elegible en oferta.
    @Test
    public void testPonerEnOferta() {
        candidato.ponerEnOferta();

        assertTrue(candidato.isEnOferta());
        assertFalse(candidato.isFichado());
        assertTrue(candidato.esElegible());
    }

    //Comprueba que retirar un candidato de la oferta cambia su estado.
    @Test
    public void testRetirarDeOferta() {
        candidato.ponerEnOferta();
        candidato.retirarDeOferta();

        assertFalse(candidato.isEnOferta());
        assertFalse(candidato.isFichado());
        assertTrue(candidato.esElegible());
    }

    //Comprueba que se puede fichar un candidato que está en oferta.
    @Test
    public void testMarcarFichado() {
        candidato.ponerEnOferta();
        candidato.marcarFichado();

        assertFalse(candidato.isEnOferta());
        assertTrue(candidato.isFichado());
        assertFalse(candidato.esElegible());
    }

    //Comprueba que no se puede fichar un candidato que no está en oferta.
    @Test(expected = IllegalStateException.class)
    public void testMarcarFichadoSinOfertaExcepcion() {
        candidato.marcarFichado();
    }

    //Comprueba que no se puede volver a poner en oferta un candidato fichado.
    @Test(expected = IllegalStateException.class)
    public void testPonerEnOfertaCandidatoFichadoExcepcion() {
        candidato.ponerEnOferta();
        candidato.marcarFichado();

        candidato.ponerEnOferta();
    }

    //Comprueba que retirar de oferta un candidato que no está en oferta
    //no provoca ninguna excepción y mantiene su estado.
    @Test
    public void testRetirarSinEstarEnOferta() {
        candidato.retirarDeOferta();

        assertFalse(candidato.isEnOferta());
        assertFalse(candidato.isFichado());
    }

    //Comprueba que no se puede fichar a un candidato fichado.
    @Test(expected = IllegalStateException.class)
    public void testMarcarFichadoDosVecesExcepcion() {
        candidato.ponerEnOferta();
        candidato.marcarFichado();
        candidato.marcarFichado();
    }

    //Comprueba que el límite bajo del precio.
    @Test
    public void testPrecioMinimoValido() {
        Candidato barato = new Candidato("Carlos", "El Rapido", 1L, TRol.CONDUCTOR);
        assertEquals(1L, barato.getPrecioFichaje());
    }

    //Comprueba que retirar un candidato fichado de la tienda no
    //desficha al candidato.
    @Test
    public void testRetirarDeOfertaCandidatoFichado() {
        candidato.ponerEnOferta();
        candidato.marcarFichado();
        candidato.retirarDeOferta();

        assertTrue(candidato.isFichado());
        assertFalse(candidato.esElegible());
    }
}
