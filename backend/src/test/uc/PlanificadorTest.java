package test.uc;

import main.model.Mision;
import main.uc.Planificador;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PlanificadorTest {

    private Planificador planificador;

    @Mock
    private Mision misionA;

    @Mock
    private Mision misionB;

    @Mock
    private Mision misionC;

    @Before
    public void setUp() {
        planificador = new Planificador();
    }

    @Test
    //comprueba que se ordenan por botin en caso de que no haya prerrequisitos
    public void testPlanificarSinPrerrequisitosOrdenaPorBotin() {
        when(misionA.getBotin()).thenReturn(100000L);
        when(misionB.getBotin()).thenReturn(500000L);
        when(misionC.getBotin()).thenReturn(300000L);

        List<Mision> resultado = planificador.planificarGolpes(Arrays.asList(misionA, misionB, misionC));

        assertEquals(3, resultado.size());
        assertEquals(misionB, resultado.get(0)); 
        assertEquals(misionC, resultado.get(1)); 
        assertEquals(misionA, resultado.get(2)); 
    }

    @Test
    //comprueba que aunque A (10.000) tenga menos botin que B (500.000) respeta
    //la dependencias y devuelve A antes que B (B depende de A)
    public void testPlanificarConPrerrequisitosRespetaDependencia() {
        //when(misionA.getBotin()).thenReturn(10000L);
        //when(misionB.getBotin()).thenReturn(500000L);
        //comento estas lineas ya que realmente solo se llama a la funcion getbotin de las misiones cuando hay 2 o mas misiones 
        //sin dependencias de forma simultanea, en este caso solo esta A y luego cuando A se elimina esta B
        when(misionB.getPrerrequisitos()).thenReturn(Collections.singletonList(misionA));

        List<Mision> resultado = planificador.planificarGolpes(Arrays.asList(misionB, misionA));

        assertEquals(2, resultado.size());
        assertEquals(misionA, resultado.get(0));
        assertEquals(misionB, resultado.get(1));
    }

    @Test
    public void testPlanificarPriorizaBotinEnNodosLibres() {
        // A (10.000) desbloquea a B (500.000)
        // C (200.000) esta libre desde el inicio
        when(misionA.getBotin()).thenReturn(10000L);
        //when(misionB.getBotin()).thenReturn(500000L);
        when(misionC.getBotin()).thenReturn(200000L);

        when(misionB.getPrerrequisitos()).thenReturn(Collections.singletonList(misionA));

        List<Mision> resultado = planificador.planificarGolpes(Arrays.asList(misionA, misionB, misionC));

        assertEquals(3, resultado.size());
        assertEquals(misionC, resultado.get(0)); 
        assertEquals(misionA, resultado.get(1)); 
        assertEquals(misionB, resultado.get(2)); 
    }

    @Test(expected = IllegalStateException.class)
    public void testPlanificarCicloLanzaExcepcion() {
        when(misionA.getPrerrequisitos()).thenReturn(Collections.singletonList(misionB));
        when(misionB.getPrerrequisitos()).thenReturn(Collections.singletonList(misionA));

        planificador.planificarGolpes(Arrays.asList(misionA, misionB));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlanificarListaNulaLanzaExcepcion() {
        planificador.planificarGolpes(null);
    }

    @Test
    public void testPlanificarListaVaciaDevuelveVacia() {
        List<Mision> resultado = planificador.planificarGolpes(Collections.emptyList());
        assertTrue(resultado.isEmpty());
    }
}