package model;

import org.junit.Test;

import main.model.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MisionTest {

    @Test
    public void testCrearMisionValida(){
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);
        plantilla.put(TRol.ASALTO, 2);

        Mision m = new Mision("Robo_Licoreria", "Centro", 20000L, 10.0, 12, plantilla);

        assertEquals("Robo_Licoreria", m.getNombreClave());
        assertEquals("Centro", m.getUbicacion());
        assertEquals(20000L, m.getBotin());
        assertEquals(10.0, m.getProbFracaso(), 0.001);
        assertEquals(12, m.getDuracion());
        assertEquals(TEstado.DISPONIBLE, m.getEstado());
        assertEquals(3, m.getPersonalTotal());
        assertEquals(1, m.getCantidadRol(TRol.CONDUCTOR));
        assertEquals(2, m.getCantidadRol(TRol.ASALTO));
        assertEquals(0, m.getCantidadRol(TRol.HACKER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarPlantillaSinConductor(){
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.ASALTO, 2);

        new Mision("Robo_Licoreria", "Centro", 20000L, 10.0, 12, plantilla);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarBotinCero(){
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);

        new Mision("Robo_Licoreria", "Centro", 0L, 10.0, 12, plantilla);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRechazarProbFracasoFueraDeRango(){
        Map<TRol, Integer> plantilla = new HashMap<>();
        plantilla.put(TRol.CONDUCTOR, 1);

        new Mision("Robo_Licoreria", "Centro", 20000L, 105.0, 12, plantilla);
    }
}