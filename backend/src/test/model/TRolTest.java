package test.model;

import org.junit.*;
import static org.junit.Assert.*;

import main.model.TConsecuencia;
import main.model.TRol;

public class TRolTest {

    //Comprueba que existen los 5 roles.
    @Test
    public void testNumeroDeRoles() {
        assertEquals(5, TRol.values().length);
    }

    //Comprueba que los valores del conductor son correctos
    @Test
    public void testValoresConductor() {
        assertEquals(15, TRol.CONDUCTOR.getComision(), 0);
        assertEquals(1500L, TRol.CONDUCTOR.getFianzaBase());
        assertEquals(10, TRol.CONDUCTOR.getProbMuerte(), 0);
    }

    //Comprueba que los valores del asalto son correctos
    @Test
    public void testValoresAsalto() {
        assertEquals(20, TRol.ASALTO.getComision(), 0);
        assertEquals(2000L, TRol.ASALTO.getFianzaBase());
        assertEquals(30, TRol.ASALTO.getProbMuerte(), 0);
    }

    //Comprueba que los valores del hacker son correctos
    @Test
    public void testValoresHacker() {
        assertEquals(3, TRol.HACKER.getComision(), 0);
        assertEquals(300L, TRol.HACKER.getFianzaBase());
        assertEquals(5, TRol.HACKER.getProbMuerte(), 0);
    }

    //Comprueba que los valores del cerrajero son correctos
    @Test
    public void testValoresCerrajero() {
        assertEquals(5, TRol.CERRAJERO.getComision(), 0);
        assertEquals(500L, TRol.CERRAJERO.getFianzaBase());
        assertEquals(15, TRol.CERRAJERO.getProbMuerte(), 0);
    }

    //Comprueba que los valores del negociador son correctos
    @Test
    public void testValoresNegociador() {
        assertEquals(10, TRol.NEGOCIADOR.getComision(), 0);
        assertEquals(1000L, TRol.NEGOCIADOR.getFianzaBase());
        assertEquals(10, TRol.NEGOCIADOR.getProbMuerte(), 0);
    }

    //Comprueba que en todos los roles muerte y detención suman 100.
    @Test
    public void testProbMuerteMasDetencionSuman100() {
        for (TRol rol : TRol.values()) {
            assertEquals("Rol " + rol, 100, rol.getProbMuerte() + rol.getProbDetencion(), 0);
        }
    }

    //Comprueba que el conductor es conductor.
    @Test
    public void testEsConductor() {
        assertTrue(TRol.CONDUCTOR.esConductor());
    }

    //Comprueba que ningún otro rol es conductor.
    @Test
    public void testNoEsConductor() {
        assertFalse(TRol.ASALTO.esConductor());
        assertFalse(TRol.HACKER.esConductor());
        assertFalse(TRol.CERRAJERO.esConductor());
        assertFalse(TRol.NEGOCIADOR.esConductor());
    }

    //Comprueba que una tirada por debajo de la probabilidad de muerte mata al secuaz.
    //Asalto: probabilidad de muerte 30
    @Test
    public void testDecidirConsecuenciaMuerto() {
        assertEquals(TConsecuencia.MUERTO, TRol.ASALTO.decidirConsecuencia(0));
        assertEquals(TConsecuencia.MUERTO, TRol.ASALTO.decidirConsecuencia(29.9));
    }

    //Comprueba que una tirada por encima de la probabilidad de muerte lo detiene.
    @Test
    public void testDecidirConsecuenciaDetenido() {
        assertEquals(TConsecuencia.DETENIDO, TRol.ASALTO.decidirConsecuencia(30.1));
        assertEquals(TConsecuencia.DETENIDO, TRol.ASALTO.decidirConsecuencia(99.9));
    }

    //Comprueba que el límite hace que sea detenido
    @Test
    public void testDecidirConsecuenciaLimite() {
        assertEquals(TConsecuencia.DETENIDO, TRol.ASALTO.decidirConsecuencia(30));
    }

    //Comprueba que decidirConsecuencia usa la probabilidad de muerte de cada rol,
    //con tirada 10 se mata al cerrajero (15%) pero no al hacker (5%)
    @Test
    public void testDecidirConsecuenciaDependeDelRol() {
        assertEquals(TConsecuencia.DETENIDO, TRol.HACKER.decidirConsecuencia(10));
        assertEquals(TConsecuencia.MUERTO, TRol.CERRAJERO.decidirConsecuencia(10));
    }
}
