package uc;

import main.model.*;
import main.uc.*;
import main.ctrl.*;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MisionesUCTest {

    private CtrlMision ctrlMision;
    private CtrlObjeto ctrlObjeto;
    private MisionesUC misionesUC;

    @Before
    public void setUp() {
        FactoriaCtrl.reiniciar(); //[cite: 4]
        ctrlMision = FactoriaCtrl.getInstance().getCtrlMision();
        ctrlObjeto = FactoriaCtrl.getInstance().getCtrlObjeto();
        misionesUC = new MisionesUC(ctrlMision, ctrlObjeto);

        // Cargar un objeto de prueba
        Objeto camion = new Objeto("Camion blindado", "Escape", 50000, TUso.REUTILIZABLE, TCategoria.VEHICULO);
        ctrlObjeto.insertar(camion);
    }

    @Test
    //aun no esta implementado para añadir re4quisitos materiales porque es de la segunda entrega
    public void testCrearMisionExitosa() {
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);

        Mision m = misionesUC.crearMision("Golpe Central", "Banco", 500000, 15.0, 10, plantilla, null, null);

        assertNotNull(m);
        assertEquals("Golpe Central", m.getNombreClave());
        assertTrue(ctrlMision.exists("Golpe Central"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrearMisionDuplicada() {
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);

        misionesUC.crearMision("Golpe Central", "Banco", 500000, 15.0, 10, plantilla, null, null);
        misionesUC.crearMision("Golpe Central", "Otro", 100000, 10.0, 5, plantilla, null, null);
    }

    @Test
    public void testListarMisionesOrdenadasPorBotin() {
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);

        misionesUC.crearMision("Golpe Pequeño", "Jueria", 100000, 10.0, 5, plantilla, null, null);
        misionesUC.crearMision("Golpe Grande", "Banco", 1000000, 20.0, 15, plantilla, null, null);

        List<Mision> lista = misionesUC.listarMisiones(null);
        assertEquals(2, lista.size());
        assertEquals("Golpe Grande", lista.get(0).getNombreClave()); // Mayor botín primero
        assertEquals("Golpe Pequeño", lista.get(1).getNombreClave());
    }
}