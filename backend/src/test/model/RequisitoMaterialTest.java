package model;

import main.model.*;


import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RequisitoMaterialTest {

    private Mision mision;
    private Objeto camion;
    private RequisitoMaterial requisito;

    @Before
    public void setUp() {
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);
        
        mision = new Mision("Golpe Central", "Banco Central", 100000, 20.0, 12, plantilla);
        camion = new Objeto("Camion blindado", "Vehiculo de escape", 50000, TUso.REUTILIZABLE, TCategoria.VEHICULO);
        
        requisito = new RequisitoMaterial(mision, camion, 2);
    }

    @Test
    public void testEstadoInicial() {
        assertEquals(mision, requisito.getMision());
        assertEquals(camion, requisito.getObjeto());
        assertEquals(2, requisito.getCantidad());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisionNula() {
        new RequisitoMaterial(null, camion, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testObjetoNulo() {
        new RequisitoMaterial(mision, null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCantidadInvalidaConstructor() {
        new RequisitoMaterial(mision, camion, 0);
    }

    @Test
    public void testSetCantidadValida() {
        requisito.setCantidad(5);
        assertEquals(5, requisito.getCantidad());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCantidadInvalida() {
        requisito.setCantidad(-2);
    }

    @Test
    public void testEsSuficienteEnAlmacenYUnidadesFaltantes() {
        // Stock inicial = 0, requerido = 2
        assertFalse(requisito.esSuficienteEnAlmacen());
        assertEquals(2, requisito.unidadesFaltantes());

        // Compramos 1 unidad
        camion.añadir(1);
        assertFalse(requisito.esSuficienteEnAlmacen());
        assertEquals(1, requisito.unidadesFaltantes());

        // Compramos otra unidad (alcanza las 2)
        camion.añadir(1);
        assertTrue(requisito.esSuficienteEnAlmacen());
        assertEquals(0, requisito.unidadesFaltantes());
    }
}