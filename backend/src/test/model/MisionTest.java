package test.model;

import main.model.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MisionTest {

    private Map<TRol, Integer> plantillaValida;
    private Mision misionPrincipal;

    @Mock
    private Ejecucion ejecucionMock;

    @Before
    public void setUp() {
        plantillaValida = new HashMap<>();
        plantillaValida.put(TRol.CONDUCTOR, 1);
        plantillaValida.put(TRol.ASALTO, 2);

        misionPrincipal = new Mision("Robo_Licoreria", "Centro", 20000L, 10.0, 12, plantillaValida);
    }

    //test de creacion
    @Test
    public void testCrearMisionValida() {
        assertEquals("Robo_Licoreria", misionPrincipal.getNombreClave());
        assertEquals("Centro", misionPrincipal.getUbicacion());
        assertEquals(20000L, misionPrincipal.getBotin());
        assertEquals(10.0, misionPrincipal.getProbFracaso(), 0.001);
        assertEquals(12, misionPrincipal.getDuracion());
        assertEquals(TEstado.DISPONIBLE, misionPrincipal.getEstado());
        assertEquals(3, misionPrincipal.getPersonalTotal());
        assertEquals(1, misionPrincipal.getCantidadRol(TRol.CONDUCTOR));
        assertEquals(2, misionPrincipal.getCantidadRol(TRol.ASALTO));
        assertEquals(0, misionPrincipal.getCantidadRol(TRol.HACKER));
    }

    @Test
    public void testDuracionPorDefecto() {
        Mision m = new Mision("Golpe_Rapido", "Norte", 10000L, 5.0, null, plantillaValida);
        assertEquals(Mision.DURACION_POR_DEFECTO, m.getDuracion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarNombreClaveVacioONulo() {
        new Mision("   ", "Centro", 20000L, 10.0, 12, plantillaValida);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarDuracionInvalida() {
        new Mision("Robo_Licoreria", "Centro", 20000L, 10.0, 0, plantillaValida);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarPlantillaSinConductor() {
        Map<TRol, Integer> sinConductor = new HashMap<>();
        sinConductor.put(TRol.ASALTO, 2);
        new Mision("Robo_Licoreria", "Centro", 20000L, 10.0, 12, sinConductor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarPlantillaVacia() {
        new Mision("Robo_Licoreria", "Centro", 20000L, 10.0, 12, new HashMap<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarBotinCero() {
        new Mision("Robo_Licoreria", "Centro", 0L, 10.0, 12, plantillaValida);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarProbFracasoFueraDeRango() {
        new Mision("Robo_Licoreria", "Centro", 20000L, 105.0, 12, plantillaValida);
    }

    //test de prerrequisitos y ciclos
    @Test
    public void testAñadirPrerrequisitoYCambioEstadoABloqueada() {
        Mision m2 = new Mision("Banco_Central", "Sur", 500000L, 20.0, 24, plantillaValida);
        
        m2.añadirPrerrequisito(misionPrincipal);

        assertEquals(1, m2.getPrerrequisitos().size());
        assertEquals(misionPrincipal, m2.getPrerrequisitos().get(0));
        assertEquals(1, misionPrincipal.getDependientes().size());
        assertEquals(TEstado.BLOQUEADA, m2.getEstado());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarAutodependencia() {
        misionPrincipal.añadirPrerrequisito(misionPrincipal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarCicloPrerrequisitos() {
        Mision m2 = new Mision("Mision_2", "Sur", 50000L, 20.0, 24, plantillaValida);
        Mision m3 = new Mision("Mision_3", "Este", 60000L, 15.0, 10, plantillaValida);

        m2.añadirPrerrequisito(misionPrincipal); //m2 depende de misionPrincipal
        m3.añadirPrerrequisito(m2);              //m3 depende de m2
        
        misionPrincipal.añadirPrerrequisito(m3); //intentamos que misionPrincipal dependa de m3
    }

    @Test
    public void testEliminarPrerrequisitoYVolverADisponible() {
        Mision m2 = new Mision("Banco_Central", "Sur", 500000L, 20.0, 24, plantillaValida);
        m2.añadirPrerrequisito(misionPrincipal);
        assertEquals(TEstado.BLOQUEADA, m2.getEstado());

        m2.eliminarPrerrequisito(misionPrincipal);
        assertEquals(0, m2.getPrerrequisitos().size());
        assertEquals(TEstado.DISPONIBLE, m2.getEstado());
    }

    @Test
    public void testObtenerCadenaPrerrequisitosYDesbloqueos() {
        Mision m2 = new Mision("Mision_2", "Sur", 50000L, 20.0, 24, plantillaValida);
        Mision m3 = new Mision("Mision_3", "Este", 60000L, 15.0, 10, plantillaValida);

        m2.añadirPrerrequisito(misionPrincipal);
        m3.añadirPrerrequisito(m2);

        List<Mision> cadena = m3.obtenerCadenaPrerrequisitos();
        assertEquals(2, cadena.size());
        assertTrue(cadena.contains(misionPrincipal));
        assertTrue(cadena.contains(m2));

        List<Mision> desbloqueos = misionPrincipal.obtenerDesbloqueosPotenciales();
        assertEquals(1, desbloqueos.size());
        assertEquals(m2, desbloqueos.get(0));
    }

    @Test
    public void testDesvincularExito() {
        Mision m2 = new Mision("Mision_2", "Sur", 50000L, 20.0, 24, plantillaValida);
        m2.añadirPrerrequisito(misionPrincipal);

        m2.desvincular();
        assertEquals(0, m2.getPrerrequisitos().size());
        assertEquals(0, misionPrincipal.getDependientes().size());
    }

    @Test(expected = IllegalStateException.class)
    public void testDesvincularFallaSiTieneDependientes() {
        Mision m2 = new Mision("Mision_2", "Sur", 50000L, 20.0, 24, plantillaValida);
        m2.añadirPrerrequisito(misionPrincipal);

        misionPrincipal.desvincular();
    }
    
    //test de plantillas 
    @Test
    public void testAñadirPlantillaRolValida() {
        misionPrincipal.añadirPlantillaRol(TRol.HACKER, 1);
        assertEquals(1, misionPrincipal.getCantidadRol(TRol.HACKER));
        assertEquals(4, misionPrincipal.getPersonalTotal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAñadirPlantillaRolDuplicada() {
        misionPrincipal.añadirPlantillaRol(TRol.ASALTO, 1);
    }

    @Test
    public void testEliminarPlantillaRolValida() {
        misionPrincipal.eliminarPlantillaRol(TRol.ASALTO);
        assertEquals(0, misionPrincipal.getCantidadRol(TRol.ASALTO));
        assertEquals(1, misionPrincipal.getPersonalTotal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEliminarPlantillaRolConductorRechazado() {
        misionPrincipal.eliminarPlantillaRol(TRol.CONDUCTOR);
    }


    //test de setters
    @Test
    public void testSettersValidos() {
        misionPrincipal.setUbicacion("Norte");
        misionPrincipal.setBotin(30000L);
        misionPrincipal.setProbFracaso(15.0);
        misionPrincipal.setDuracion(20);

        assertEquals("Norte", misionPrincipal.getUbicacion());
        assertEquals(30000L, misionPrincipal.getBotin());
        assertEquals(15.0, misionPrincipal.getProbFracaso(), 0.001);
        assertEquals(20, misionPrincipal.getDuracion());
    }

    @Test(expected = IllegalStateException.class)
    public void testNoEditableSiTieneEjecuciones() {
        when(ejecucionMock.estaEnCurso()).thenReturn(true);
        misionPrincipal.registrarEjecucion(ejecucionMock);

        misionPrincipal.setBotin(40000L);
    }

    //test de estados despues de ejecucioens
    @Test
    public void testEstadoEnCursoConEjecucion() {
        when(ejecucionMock.estaEnCurso()).thenReturn(true);
        
        misionPrincipal.registrarEjecucion(ejecucionMock);

        assertEquals(TEstado.EN_CURSO, misionPrincipal.getEstado());
        assertFalse(misionPrincipal.puedeIniciarse());
    }

    @Test
    public void testEstadoCompletadaYDesbloqueo() {
        Mision m2 = new Mision("Mision_2", "Sur", 50000L, 20.0, 24, plantillaValida);
        m2.añadirPrerrequisito(misionPrincipal);
        assertEquals(TEstado.BLOQUEADA, m2.getEstado());

        when(ejecucionMock.estaEnCurso()).thenReturn(false);
        when(ejecucionMock.getResultado()).thenReturn(TResultado.EXITO);

        misionPrincipal.registrarEjecucion(ejecucionMock);

        assertEquals(TEstado.COMPLETADA, misionPrincipal.getEstado());
        //si se completa msiion principal m2 debe pasar a DISPONIBLE
        assertEquals(TEstado.DISPONIBLE, m2.getEstado());
    }

    @Test
    public void testEstadoFallidaYPuedeReiniciarse() {
        when(ejecucionMock.estaEnCurso()).thenReturn(false);
        when(ejecucionMock.getResultado()).thenReturn(TResultado.FRACASO);

        misionPrincipal.registrarEjecucion(ejecucionMock);

        assertEquals(TEstado.FALLIDA, misionPrincipal.getEstado());
        assertTrue(misionPrincipal.puedeIniciarse());
        assertEquals(1, misionPrincipal.getNumFracasos());
        assertEquals(1, misionPrincipal.getNumIntentos());
    }
}