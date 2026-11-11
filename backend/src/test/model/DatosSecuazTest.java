package test.model;

import main.model.*;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Alejandro
 */

public class DatosSecuazTest {

    // Comprueba que el constructor guarda el nombre y el rol que se le pasan en el formato necesario
    // probando con todos los roles para detectar que no se guarda siempre el mismo
    @Test
    public void testConstructorGuardaNombreYRol() {
        for (TRol rol : TRol.values()) {
            DatosSecuaz datos = new DatosSecuaz("Alex Herrero", rol);

            assertEquals("Alex Herrero", datos.getNombre());
            assertSame(rol, datos.getRol());
        }
    }

    // Comprueba que un nombre de un solo caracter ya es valido
    @Test
    public void testNombreDeUnCaracterEsValido() {
        DatosSecuaz datos = new DatosSecuaz("f", TRol.CONDUCTOR);

        assertEquals("f", datos.getNombre());
    }

    // Comprueba que un nombre null se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testNombreNullLanzaExcepcion() {
        new DatosSecuaz(null, TRol.CONDUCTOR);
    }

    // Comprueba que un nombre vacio se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testNombreVacioLanzaExcepcion() {
        new DatosSecuaz("", TRol.CONDUCTOR);
    }

    // Comprueba que un nombre formado solo por espacios se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testNombreSoloEspaciosLanzaExcepcion() {
        new DatosSecuaz("   ", TRol.CONDUCTOR);
    }

    // Comprueba que un rol null se rechaza aunque el nombre sea valido
    @Test(expected = IllegalArgumentException.class)
    public void testRolNullLanzaExcepcion() {
        new DatosSecuaz("Alex Herrero", null);
    }
}
