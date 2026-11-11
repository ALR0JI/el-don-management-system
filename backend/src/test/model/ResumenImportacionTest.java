package test.model;

import main.model.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Alejandro
 */

@RunWith(MockitoJUnitRunner.class) // Asi se inicializan todos los objetos antes de hacer los tests
public class ResumenImportacionTest {
    private static final String AVISO_1 = "Linea 2: rol invalido";
    private static final String AVISO_2 = "Linea 5: esta vacia";

    // Mocks
    // No hace falta probar con DatosMision porque nunca usa el comportamiento de los elementos
    @Mock private DatosSecuaz secuaz1;
    @Mock private DatosSecuaz secuaz2;

    // Comprueba que el constructor guarda los datos y los avisos y en el mismo orden en que se pasan
    @Test
    public void testConstructorGuardaDatosYAvisos() {
        ResumenImportacion<DatosSecuaz> resumen = new ResumenImportacion<>(
                List.of(secuaz1, secuaz2), List.of(AVISO_1, AVISO_2));

        assertEquals(List.of(secuaz1, secuaz2), resumen.getDatos());
        assertEquals(List.of(AVISO_1, AVISO_2), resumen.getAvisos());
    }

    // Comprueba que una lista vacia es valida (solo se rechaza null) y que cada lista puede estar vacia sin la otra
    @Test
    public void testListasVaciasSonValidas() {
        ResumenImportacion<DatosSecuaz> sinNada = new ResumenImportacion<>(new ArrayList<>(), new ArrayList<>());
        assertTrue(sinNada.getDatos().isEmpty());
        assertTrue(sinNada.getAvisos().isEmpty());

        // avisos pero sin datos
        ResumenImportacion<DatosSecuaz> soloAvisos = new ResumenImportacion<>(new ArrayList<>(), List.of(AVISO_1));
        assertTrue(soloAvisos.getDatos().isEmpty());
        assertEquals(List.of(AVISO_1), soloAvisos.getAvisos());

        // datos pero sin avisos
        ResumenImportacion<DatosSecuaz> soloDatos = new ResumenImportacion<>(List.of(secuaz1), new ArrayList<>());
        assertEquals(List.of(secuaz1), soloDatos.getDatos());
        assertTrue(soloDatos.getAvisos().isEmpty());
    }

    // Comprueba que una lista de datos null se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testDatosNullLanzaExcepcion() {
        new ResumenImportacion<DatosSecuaz>(null, new ArrayList<>());
    }

    // Comprueba que una lista de avisos null se rechaza
    @Test(expected = IllegalArgumentException.class)
    public void testAvisosNullLanzaExcepcion() {
        new ResumenImportacion<DatosSecuaz>(new ArrayList<>(), null);
    }

    // Comprueba que no afecta al resumen modificar las listas originales despues de construirlo
    @Test
    public void testCopiaDefensivaNoSeVeAfectadaPorCambiosEnLosOriginales() {
        List<DatosSecuaz> datos = new ArrayList<>(List.of(secuaz1));
        List<String> avisos = new ArrayList<>(List.of(AVISO_1));
        ResumenImportacion<DatosSecuaz> resumen = new ResumenImportacion<>(datos, avisos);

        datos.add(secuaz2);
        avisos.clear();

        assertEquals(List.of(secuaz1), resumen.getDatos());
        assertEquals(List.of(AVISO_1), resumen.getAvisos());
    }

    // Comprueba que la lista que devuelve getDatos no se puede modificar
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDatosEsInmodificable() {
        List<DatosSecuaz> datos = new ArrayList<>(List.of(secuaz1));
        ResumenImportacion<DatosSecuaz> resumen = new ResumenImportacion<>(datos, new ArrayList<>());

        resumen.getDatos().add(secuaz2);
    }

    // Comprueba que la lista que devuelve getAvisos no se puede modificar
    @Test(expected = UnsupportedOperationException.class)
    public void testGetAvisosEsInmodificable() {
        List<String> avisos = new ArrayList<>(List.of(AVISO_1));
        ResumenImportacion<DatosSecuaz> resumen = new ResumenImportacion<>(new ArrayList<>(), avisos);

        resumen.getAvisos().add(AVISO_2);
    }
}
