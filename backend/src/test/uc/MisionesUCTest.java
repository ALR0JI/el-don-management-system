package test.uc;

import main.ctrl.*;
import main.model.*;
import main.uc.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MisionesUCTest {

    @Mock
    private CtrlMision ctrlMisionMock;

    @Mock
    private CtrlObjeto ctrlObjetoMock;

    private MisionesUC misionesUC;

    @Before
    public void setUp() {
        misionesUC = new MisionesUC(ctrlMisionMock, ctrlObjetoMock);
    }

    @Test
    public void testCrearMisionExitosa() {
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);

        when(ctrlMisionMock.exists("Golpe Central")).thenReturn(false);

        Mision m = misionesUC.crearMision("Golpe Central", "Banco", 500000, 15.0, 10, plantilla, null, null);

        assertNotNull(m);
        assertEquals("Golpe Central", m.getNombreClave());

        verify(ctrlMisionMock, times(1)).exists("Golpe Central");
        verify(ctrlMisionMock, times(1)).insertar(any(Mision.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrearMisionDuplicada() {
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);

        when(ctrlMisionMock.exists("Golpe Central")).thenReturn(true);

        misionesUC.crearMision("Golpe Central", "Banco", 500000, 15.0, 10, plantilla, null, null);
    }

    @Test
    public void testListarMisionesOrdenadasPorBotin() {
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);

        Mision mPequena = new Mision("Golpe Pequeño", "Jueria", 100000L, 10.0, 5, plantilla);
        Mision mGrande = new Mision("Golpe Grande", "Banco", 1000000L, 20.0, 15, plantilla);

        // Se utiliza all() según la definición de CtrlMision
        when(ctrlMisionMock.all()).thenReturn(Arrays.asList(mPequena, mGrande));

        List<Mision> lista = misionesUC.listarMisiones(null);

        assertEquals(2, lista.size());
        assertEquals("Golpe Grande", lista.get(0).getNombreClave());
        assertEquals("Golpe Pequeño", lista.get(1).getNombreClave());

        verify(ctrlMisionMock, times(1)).all();
    }
}