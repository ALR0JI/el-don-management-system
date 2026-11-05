package main.model;
// OJO con el package: Ejecucion.iniciar llama a mision.registrarEjecucion(), que es de visibilidad
// de paquete. Para poder hacer verify() sobre ese metodo el test tiene que estar en main.model
// (es lo que pide la regla 3 del README: "mismo package que la clase que prueban").

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class) // Mockito crea los @Mock antes de cada test
public class EjecucionTest {

    private static final int ID = 1;
    private static final int HORA_INICIO = 10;
    private static final int DURACION = 12;          // fin = 22
    private static final int HORA_FIN = HORA_INICIO + DURACION;
    private static final long BOTIN = 10000L;
    private static final double DELTA = 0.001;

    // La mision es un colaborador de Ejecucion: la sustituimos por un mock para controlar lo que devuelve.
    @Mock
    private Mision misionMock;

    // El Random NO se puede mockear con Mockito 4.9 en Java 17+ (java.util.Random lleva anotaciones
    // internas del JDK y Mockito falla al generar el mock). Lo sustituimos a mano por un "Random
    // guionizado": una subclase que devuelve las tiradas que nosotros le programamos (ver abajo).
    private RandomGuionizado random;

    private static class RandomGuionizado extends Random {
        private double[] tiradas = new double[0];
        private int llamadas = 0;

        void programar(double[] tiradas) {
            this.tiradas = tiradas;
            this.llamadas = 0;
        }

        int getLlamadas() {
            return llamadas;
        }

        @Override
        public double nextDouble() {
            if (tiradas.length == 0) {
                throw new IllegalStateException("Se ha pedido una tirada pero el test no ha programado ninguna");
            }
            double valor = tiradas[Math.min(llamadas, tiradas.length - 1)]; // al acabarse, repite la ultima
            llamadas++;
            return valor;
        }
    }

    // Secuaces "falsos": uno con especialidad CONDUCTOR y otro con ASALTO. Ambos estan libres.
    private Secuaz conductor;
    private Secuaz asaltante;

    // Que NO se mockea y por que:
    //  - Participacion: la crea Ejecucion internamente con "new", asi que es real. Depende solo de
    //    Secuaz (que es mock) y de TRol (enum real).
    //  - Don y Reloj: son singletons. Se usan reales y se reinician con reiniciar() (ver setUp/tearDown).
    //  - Los enums (TRol, TResultado...) no se pueden mockear y no hace falta.

    @Before
    public void setUp() {
        Don.reiniciar();
        Reloj.reiniciar();

        random = new RandomGuionizado();
        conductor = secuaz(TRol.CONDUCTOR);
        asaltante = secuaz(TRol.ASALTO);

        // Mision estandar de los tests: 1 CONDUCTOR + 1 ASALTO (N = 2), k = 20 --> si todos
        // estan en su especialidad, probExito = 2/2 * (100 - 20) = 80.
        configurarMision(20.0, Map.of(TRol.CONDUCTOR, 1, TRol.ASALTO, 1));
    }

    @After
    public void tearDown() {
        Don.reiniciar();
        Reloj.reiniciar();
    }

    // ------------------------------------------------------------------------------------------
    // Metodos de apoyo (los stubs son lenient() porque son preparacion compartida: no todos los
    // tests llegan a usarlos y MockitoJUnitRunner se quejaria de "stubs innecesarios").
    // ------------------------------------------------------------------------------------------

    private Secuaz secuaz(TRol especialidad) {
        Secuaz s = mock(Secuaz.class);
        lenient().when(s.getEspecialidad()).thenReturn(especialidad);
        lenient().when(s.esLibre()).thenReturn(true);
        return s;
    }

    private void configurarMision(double probFracaso, Map<TRol, Integer> plantilla) {
        reset(misionMock);
        List<PlantillaRol> plantillas = new ArrayList<>();
        int personalTotal = 0;
        for (Map.Entry<TRol, Integer> entrada : plantilla.entrySet()) {
            TRol rol = entrada.getKey();
            int cantidad = entrada.getValue();
            PlantillaRol pr = mock(PlantillaRol.class);
            lenient().when(pr.getRol()).thenReturn(rol);
            lenient().when(pr.getCantidad()).thenReturn(cantidad);
            lenient().when(misionMock.getCantidadRol(rol)).thenReturn(cantidad);
            plantillas.add(pr);
            personalTotal += cantidad;
        }
        lenient().when(misionMock.getPlantillasRol()).thenReturn(plantillas);
        lenient().when(misionMock.getPersonalTotal()).thenReturn(personalTotal);
        lenient().when(misionMock.getProbFracaso()).thenReturn(probFracaso);
        lenient().when(misionMock.getDuracion()).thenReturn(DURACION);
        lenient().when(misionMock.getBotin()).thenReturn(BOTIN);
        lenient().when(misionMock.getNombreClave()).thenReturn("Golpe");
        lenient().when(misionMock.puedeIniciarse()).thenReturn(true);
        // getRequisitosMateriales() devuelve una lista vacia por defecto (Mockito)
    }

    private Map<TRol, List<Secuaz>> equipoBase() {
        Map<TRol, List<Secuaz>> asignacion = new LinkedHashMap<>(); // LinkedHashMap: orden de participaciones estable
        asignacion.put(TRol.CONDUCTOR, new ArrayList<>(List.of(conductor)));
        asignacion.put(TRol.ASALTO, new ArrayList<>(List.of(asaltante)));
        return asignacion;
    }

    private Ejecucion iniciarBase() {
        return Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, HORA_INICIO, equipoBase());
    }

    private RequisitoMaterial requisito(String nombreObjeto, int cantidad, Objeto objeto) {
        lenient().when(objeto.getNombre()).thenReturn(nombreObjeto);
        RequisitoMaterial r = mock(RequisitoMaterial.class);
        lenient().when(r.getObjeto()).thenReturn(objeto);
        lenient().when(r.getCantidad()).thenReturn(cantidad);
        lenient().when(r.esSuficienteEnAlmacen()).thenReturn(true);
        return r;
    }

    // Las tiradas que va a "sacar" el Random, en orden. Cuando se acaban, repite la ultima.
    // Recordad: la tirada es random.nextDouble() * 100 y el suceso ocurre si es MENOR que la probabilidad.
    private void tiradas(double... valores) {
        random.programar(valores);
    }

    // ==========================================================================================
    // calcularProbabilidadExito: funcion pura, m / N * (100 - k)
    // ==========================================================================================

    @Test
    public void testProbabilidadTodosEnSuEspecialidad() {
        // m = 2, N = 2, k = 20 --> 2/2 * 80 = 80
        assertEquals(80.0, Ejecucion.calcularProbabilidadExito(misionMock, equipoBase()), DELTA);
    }

    @Test
    public void testProbabilidadUnoFueraDeEspecialidad() {
        Map<TRol, List<Secuaz>> asignacion = equipoBase();
        asignacion.put(TRol.ASALTO, new ArrayList<>(List.of(secuaz(TRol.HACKER)))); // un hacker haciendo de asalto

        // m = 1, N = 2, k = 20 --> 1/2 * 80 = 40
        assertEquals(40.0, Ejecucion.calcularProbabilidadExito(misionMock, asignacion), DELTA);
    }

    @Test
    public void testProbabilidadNingunoEnSuEspecialidad() {
        Map<TRol, List<Secuaz>> asignacion = new LinkedHashMap<>();
        asignacion.put(TRol.CONDUCTOR, new ArrayList<>(List.of(secuaz(TRol.HACKER))));
        asignacion.put(TRol.ASALTO, new ArrayList<>(List.of(secuaz(TRol.CERRAJERO))));

        assertEquals(0.0, Ejecucion.calcularProbabilidadExito(misionMock, asignacion), DELTA);
    }

    @Test
    public void testProbabilidadSinProbabilidadDeFracaso() {
        configurarMision(0.0, Map.of(TRol.CONDUCTOR, 1, TRol.ASALTO, 1));

        assertEquals(100.0, Ejecucion.calcularProbabilidadExito(misionMock, equipoBase()), DELTA);
    }

    @Test
    public void testProbabilidadFracasoSeguro() {
        configurarMision(100.0, Map.of(TRol.CONDUCTOR, 1, TRol.ASALTO, 1));

        assertEquals(0.0, Ejecucion.calcularProbabilidadExito(misionMock, equipoBase()), DELTA);
    }

    @Test
    public void testProbabilidadNoUsaDivisionEntera() {
        // m = 3, N = 4, k = 10 --> 3/4 * 90 = 67.5. Con division entera (3/4 = 0) saldria 0.
        configurarMision(10.0, Map.of(TRol.CONDUCTOR, 1, TRol.ASALTO, 3));
        Map<TRol, List<Secuaz>> asignacion = new LinkedHashMap<>();
        asignacion.put(TRol.CONDUCTOR, new ArrayList<>(List.of(conductor)));
        asignacion.put(TRol.ASALTO, new ArrayList<>(List.of(secuaz(TRol.ASALTO), secuaz(TRol.ASALTO), secuaz(TRol.HACKER))));

        assertEquals(67.5, Ejecucion.calcularProbabilidadExito(misionMock, asignacion), DELTA);
    }

    // ==========================================================================================
    // iniciar: caso correcto
    // ==========================================================================================

    @Test
    public void testIniciarEstadoInicial() {
        Ejecucion e = iniciarBase();

        assertEquals(ID, e.getId());
        assertSame(misionMock, e.getMision());
        assertEquals(TEjecucion.MANUAL, e.getTipo());
        assertEquals(HORA_INICIO, e.getInicio());
        assertEquals(HORA_FIN, e.getFin());                    // fin = inicio + duracion (RT23)
        assertEquals(TResultado.EN_CURSO, e.getResultado());
        assertTrue(e.estaEnCurso());
        assertFalse(e.isLeida());
        assertEquals("", e.getMaterialPerdido());
        assertEquals(80.0, e.getProbExito(), DELTA);           // se fija en iniciar (RT25)
    }

    @Test
    public void testIniciarCreaUnaParticipacionPorSecuaz() {
        Ejecucion e = iniciarBase();

        assertEquals(2, e.getParticipaciones().size());
        Participacion pConductor = e.getParticipaciones().get(0);
        Participacion pAsalto = e.getParticipaciones().get(1);

        assertSame(conductor, pConductor.getSecuaz());
        assertEquals(TRol.CONDUCTOR, pConductor.getRolDesempenado());
        assertSame(e, pConductor.getEjecucion());
        assertEquals(0, pConductor.getCobrado());
        assertEquals(TConsecuencia.NINGUNA, pConductor.getConsecuencia());

        assertSame(asaltante, pAsalto.getSecuaz());
        assertEquals(TRol.ASALTO, pAsalto.getRolDesempenado());
    }

    @Test
    public void testIniciarOcupaALosSecuaces() {
        iniciarBase();

        verify(conductor).ocupar();
        verify(asaltante).ocupar();
    }

    @Test
    public void testIniciarRegistraLaEjecucionEnLaMision() {
        Ejecucion e = iniciarBase();

        verify(misionMock).registrarEjecucion(e);
    }

    @Test
    public void testIniciarDescuentaElMaterial() {
        Objeto ganzua = mock(Objeto.class);
        RequisitoMaterial req = requisito("Ganzua", 2, ganzua);
        when(misionMock.getRequisitosMateriales()).thenReturn(List.of(req));

        iniciarBase();

        verify(ganzua).descontar(2);
    }

    @Test
    public void testIniciarEjecucionAutomatica() {
        Ejecucion e = Ejecucion.iniciar(ID, misionMock, TEjecucion.AUTOMATICA, HORA_INICIO, equipoBase());

        assertEquals(TEjecucion.AUTOMATICA, e.getTipo());
    }

    // ==========================================================================================
    // iniciar: validaciones. Lo importante: si algo falla se lanza la excepcion y NO se ha
    // modificado nada ("valida y despues modifica").
    // ==========================================================================================

    @Test
    public void testIniciarMisionNoIniciable() {
        when(misionMock.puedeIniciarse()).thenReturn(false);

        assertThrows(IllegalStateException.class, this::iniciarBase);

        verify(conductor, never()).ocupar();
        verify(misionMock, never()).registrarEjecucion(any());
    }

    @Test
    public void testIniciarPuestoSinCubrir() {
        Map<TRol, List<Secuaz>> asignacion = equipoBase();
        asignacion.remove(TRol.ASALTO);

        assertThrows(IllegalArgumentException.class,
                () -> Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, HORA_INICIO, asignacion));

        verify(conductor, never()).ocupar();
    }

    @Test
    public void testIniciarPuestoConSecuacesDeMenos() {
        Map<TRol, List<Secuaz>> asignacion = equipoBase();
        asignacion.put(TRol.ASALTO, new ArrayList<>()); // la plantilla pide 1 y damos 0

        assertThrows(IllegalArgumentException.class,
                () -> Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, HORA_INICIO, asignacion));
    }

    @Test
    public void testIniciarPuestoConSecuacesDeMas() {
        Map<TRol, List<Secuaz>> asignacion = equipoBase();
        asignacion.put(TRol.ASALTO, new ArrayList<>(List.of(asaltante, secuaz(TRol.ASALTO)))); // pide 1 y damos 2

        assertThrows(IllegalArgumentException.class,
                () -> Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, HORA_INICIO, asignacion));
    }

    @Test
    public void testIniciarPuestoQueNoEstaEnLaPlantilla() {
        Map<TRol, List<Secuaz>> asignacion = equipoBase();
        asignacion.put(TRol.HACKER, new ArrayList<>(List.of(secuaz(TRol.HACKER)))); // la mision no pide HACKER

        assertThrows(IllegalArgumentException.class,
                () -> Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, HORA_INICIO, asignacion));
    }

    @Test
    public void testIniciarSecuazRepetido() {
        Map<TRol, List<Secuaz>> asignacion = equipoBase();
        asignacion.put(TRol.ASALTO, new ArrayList<>(List.of(conductor))); // el mismo secuaz en dos puestos

        assertThrows(IllegalArgumentException.class,
                () -> Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, HORA_INICIO, asignacion));
    }

    @Test
    public void testIniciarSecuazNulo() {
        Map<TRol, List<Secuaz>> asignacion = equipoBase();
        asignacion.put(TRol.ASALTO, Arrays.asList((Secuaz) null));

        assertThrows(IllegalArgumentException.class,
                () -> Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, HORA_INICIO, asignacion));
    }

    @Test
    public void testIniciarSecuazNoLibre() {
        when(asaltante.esLibre()).thenReturn(false);

        assertThrows(IllegalStateException.class, this::iniciarBase);

        // El conductor si estaba libre, pero como la validacion falla no se le puede haber ocupado
        verify(conductor, never()).ocupar();
    }

    @Test
    public void testIniciarMaterialInsuficiente() {
        Objeto ganzua = mock(Objeto.class);
        RequisitoMaterial req = requisito("Ganzua", 2, ganzua);
        when(req.esSuficienteEnAlmacen()).thenReturn(false);
        when(misionMock.getRequisitosMateriales()).thenReturn(List.of(req));

        assertThrows(IllegalStateException.class, this::iniciarBase);

        verify(conductor, never()).ocupar();
        verify(asaltante, never()).ocupar();
        verify(ganzua, never()).descontar(anyInt());
        verify(misionMock, never()).registrarEjecucion(any());
    }

    @Test
    public void testIniciarIdInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> Ejecucion.iniciar(0, misionMock, TEjecucion.MANUAL, HORA_INICIO, equipoBase()));

        verify(conductor, never()).ocupar();
    }

    @Test
    public void testIniciarTipoNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> Ejecucion.iniciar(ID, misionMock, null, HORA_INICIO, equipoBase()));

        verify(conductor, never()).ocupar();
    }

    @Test
    public void testIniciarHoraNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, -1, equipoBase()));

        verify(conductor, never()).ocupar();
    }

    // ==========================================================================================
    // Consultas simples
    // ==========================================================================================

    @Test
    public void testGetTiempoRestante() {
        Ejecucion e = iniciarBase(); // fin = 22

        assertEquals(12, e.getTiempoRestante(10));
        assertEquals(2, e.getTiempoRestante(20));
        assertEquals(0, e.getTiempoRestante(22));  // justo en el fin
        assertEquals(0, e.getTiempoRestante(30));  // nunca negativo
    }

    @Test
    public void testGetParticipacionesNoSePuedeModificar() {
        Ejecucion e = iniciarBase();

        assertThrows(UnsupportedOperationException.class, () -> e.getParticipaciones().clear());
    }

    @Test
    public void testTotalCobradoYComisionDonAntesDeFinalizar() {
        Ejecucion e = iniciarBase();

        assertEquals(0, e.getTotalCobrado());
        assertEquals(0, e.getComisionDon());
    }

    @Test
    public void testMarcarComoLeidaEnCursoNoHaceNada() {
        Ejecucion e = iniciarBase();

        e.marcarComoLeida();

        assertFalse(e.isLeida());
    }

    @Test
    public void testMarcarComoLeidaTerminada() {
        Ejecucion e = iniciarBase();
        tiradas(0.0);
        e.finalizar(HORA_FIN, random);

        e.marcarComoLeida();

        assertTrue(e.isLeida());
    }

    // ==========================================================================================
    // finalizar: validaciones
    // ==========================================================================================

    @Test
    public void testFinalizarAntesDeSuFin() {
        Ejecucion e = iniciarBase();

        assertThrows(IllegalStateException.class, () -> e.finalizar(HORA_FIN - 1, random));

        assertTrue(e.estaEnCurso());
        assertEquals(0, random.getLlamadas()); // ni siquiera se ha hecho la tirada
    }

    @Test
    public void testFinalizarEnLaHoraExactaDeFin() {
        Ejecucion e = iniciarBase();
        tiradas(0.0);

        e.finalizar(HORA_FIN, random); // horaActual >= fin es valido

        assertFalse(e.estaEnCurso());
    }

    @Test
    public void testFinalizarDosVeces() {
        Ejecucion e = iniciarBase();
        tiradas(0.0);
        e.finalizar(HORA_FIN, random);

        assertThrows(IllegalStateException.class, () -> e.finalizar(HORA_FIN, random));
    }

    // ==========================================================================================
    // finalizar: tirada de exito (exito si nextDouble()*100 < probExito; aqui probExito = 80)
    // ==========================================================================================

    @Test
    public void testFinalizarConExito() {
        Ejecucion e = iniciarBase();
        tiradas(0.0); // 0 < 80 --> exito

        e.finalizar(HORA_FIN, random);

        assertEquals(TResultado.EXITO, e.getResultado());
        assertFalse(e.estaEnCurso());
    }

    @Test
    public void testFinalizarConFracaso() {
        Ejecucion e = iniciarBase();
        tiradas(0.99); // 99 >= 80 --> fracaso (y 99 >= 40: nadie tiene incidente)

        e.finalizar(HORA_FIN, random);

        assertEquals(TResultado.FRACASO, e.getResultado());
        assertFalse(e.estaEnCurso());
    }

    @Test
    public void testFinalizarTiradaJustoEnLaProbabilidadEsFracaso() {
        // probExito = 50 (m = 1 de N = 2, k = 0). La tirada 0.5 * 100 = 50 NO es menor que 50.
        configurarMision(0.0, Map.of(TRol.CONDUCTOR, 1, TRol.ASALTO, 1));
        Map<TRol, List<Secuaz>> asignacion = equipoBase();
        asignacion.put(TRol.ASALTO, new ArrayList<>(List.of(secuaz(TRol.HACKER))));
        Ejecucion e = Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, HORA_INICIO, asignacion);
        assertEquals(50.0, e.getProbExito(), DELTA);
        tiradas(0.5, 0.99);

        e.finalizar(HORA_FIN, random);

        assertEquals(TResultado.FRACASO, e.getResultado());
    }

    @Test
    public void testFinalizarTiradaJustoPorDebajoDeLaProbabilidadEsExito() {
        configurarMision(0.0, Map.of(TRol.CONDUCTOR, 1, TRol.ASALTO, 1));
        Map<TRol, List<Secuaz>> asignacion = equipoBase();
        asignacion.put(TRol.ASALTO, new ArrayList<>(List.of(secuaz(TRol.HACKER))));
        Ejecucion e = Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, HORA_INICIO, asignacion);
        tiradas(0.49);

        e.finalizar(HORA_FIN, random);

        assertEquals(TResultado.EXITO, e.getResultado());
    }

    @Test
    public void testFinalizarProbabilidadCeroNuncaHaceExito() {
        configurarMision(100.0, Map.of(TRol.CONDUCTOR, 1, TRol.ASALTO, 1)); // probExito = 0
        Ejecucion e = iniciarBase();
        tiradas(0.0, 0.99); // ni con la mejor tirada posible

        e.finalizar(HORA_FIN, random);

        assertEquals(TResultado.FRACASO, e.getResultado());
    }

    @Test
    public void testFinalizarProbabilidadCienSiempreHaceExito() {
        configurarMision(0.0, Map.of(TRol.CONDUCTOR, 1, TRol.ASALTO, 1)); // probExito = 100
        Ejecucion e = iniciarBase();
        tiradas(0.9999);

        e.finalizar(HORA_FIN, random);

        assertEquals(TResultado.EXITO, e.getResultado());
    }

    // ==========================================================================================
    // finalizar: EXITO --> reparto del botin y comision del Don
    // ==========================================================================================

    @Test
    public void testFinalizarExitoRepartePorEspecialidad() {
        Ejecucion e = iniciarBase();
        tiradas(0.0);

        e.finalizar(HORA_FIN, random);

        // botin 10000: CONDUCTOR 15% = 1500, ASALTO 20% = 2000
        assertEquals(1500, e.getParticipaciones().get(0).getCobrado());
        assertEquals(2000, e.getParticipaciones().get(1).getCobrado());
        assertEquals(3500, e.getTotalCobrado());
        verify(conductor).acumularGanancia(1500);
        verify(asaltante).acumularGanancia(2000);
    }

    @Test
    public void testFinalizarExitoComisionDelDon() {
        Ejecucion e = iniciarBase();
        tiradas(0.0);

        e.finalizar(HORA_FIN, random);

        assertEquals(6500, e.getComisionDon()); // 10000 - 3500
        assertEquals(6500, Don.getInstance().getCaja());
        assertEquals(1, Don.getInstance().getMovimientos().size());
        MovimientoCaja mov = Don.getInstance().getMovimientos().get(0);
        assertEquals(TMovimiento.COMISION, mov.getTipo());
        assertEquals(6500, mov.getImporte());
        assertEquals("Comisión Golpe", mov.getConcepto());
    }

    @Test
    public void testFinalizarExitoNadieSufreConsecuencias() {
        Ejecucion e = iniciarBase();
        tiradas(0.0);

        e.finalizar(HORA_FIN, random);

        assertEquals(2, e.contarConsecuencias(TConsecuencia.NINGUNA));
        assertEquals(0, e.contarConsecuencias(TConsecuencia.DETENIDO));
        assertEquals(0, e.contarConsecuencias(TConsecuencia.MUERTO));
        verify(conductor).procesarConsecuencia(TConsecuencia.NINGUNA, HORA_FIN);
        verify(asaltante).procesarConsecuencia(TConsecuencia.NINGUNA, HORA_FIN);
    }

    @Test
    public void testFinalizarExitoSoloHaceUnaTirada() {
        Ejecucion e = iniciarBase();
        tiradas(0.0);

        e.finalizar(HORA_FIN, random);

        assertEquals(1, random.getLlamadas()); // en exito no hay tiradas de incidente
    }

    @Test
    public void testFinalizarExitoComisionDelDonPuedeSerNegativa() {
        // RT6: si las comisiones de los secuaces suman mas que el botin, el Don "pierde" dinero.
        // botin 10000; 1 conductor (15%) + 5 asaltos (20%) = 1500 + 5 * 2000 = 11500 --> Don: -1500
        configurarMision(0.0, Map.of(TRol.CONDUCTOR, 1, TRol.ASALTO, 5));
        Map<TRol, List<Secuaz>> asignacion = new LinkedHashMap<>();
        asignacion.put(TRol.CONDUCTOR, new ArrayList<>(List.of(conductor)));
        List<Secuaz> asaltos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            asaltos.add(secuaz(TRol.ASALTO));
        }
        asignacion.put(TRol.ASALTO, asaltos);
        Ejecucion e = Ejecucion.iniciar(ID, misionMock, TEjecucion.MANUAL, HORA_INICIO, asignacion);
        tiradas(0.0);

        e.finalizar(HORA_FIN, random);

        assertEquals(11500, e.getTotalCobrado());
        assertEquals(-1500, e.getComisionDon());
        assertEquals(-1500, Don.getInstance().getCaja());
    }

    // ==========================================================================================
    // finalizar: FRACASO --> incidentes (probIncidente = 40 / (1 + 0.1 * numMisiones))
    // Orden de tiradas: 1) exito, 2) por cada participante: incidente y, si lo hay, la consecuencia.
    // ==========================================================================================

    @Test
    public void testFinalizarFracasoSinIncidentesNoReparteNiCobra() {
        Ejecucion e = iniciarBase();
        tiradas(0.99); // fracaso; 99 >= 40: sin incidentes para nadie

        e.finalizar(HORA_FIN, random);

        assertEquals(2, e.contarConsecuencias(TConsecuencia.NINGUNA));
        assertEquals(0, e.getTotalCobrado());
        assertEquals(0, e.getComisionDon());
        assertEquals(0, Don.getInstance().getCaja());
        assertTrue(Don.getInstance().getMovimientos().isEmpty());
        verify(conductor, never()).acumularGanancia(anyLong());
        verify(asaltante, never()).acumularGanancia(anyLong());
    }

    @Test
    public void testFinalizarFracasoConDetencion() {
        Ejecucion e = iniciarBase();
        // exito 0.99 (fracaso) | conductor: incidente 0.0 (<40), consecuencia 0.99 (>= 10, probMuerte del CONDUCTOR: DETENIDO)
        // | asaltante: incidente 0.99 (sin incidente)
        tiradas(0.99, 0.0, 0.99, 0.99);

        e.finalizar(HORA_FIN, random);

        assertEquals(TConsecuencia.DETENIDO, e.getParticipaciones().get(0).getConsecuencia());
        assertEquals(TConsecuencia.NINGUNA, e.getParticipaciones().get(1).getConsecuencia());
        assertEquals(1, e.contarConsecuencias(TConsecuencia.DETENIDO));
        assertEquals(1, e.contarConsecuencias(TConsecuencia.NINGUNA));
        assertEquals(0, e.contarConsecuencias(TConsecuencia.MUERTO));
        verify(conductor).procesarConsecuencia(TConsecuencia.DETENIDO, HORA_FIN);
        verify(asaltante).procesarConsecuencia(TConsecuencia.NINGUNA, HORA_FIN);
    }

    @Test
    public void testFinalizarFracasoConMuerte() {
        Ejecucion e = iniciarBase();
        // conductor: incidente 0.0, consecuencia 0.05 (5 < 10 --> MUERTO) | asaltante: sin incidente
        tiradas(0.99, 0.0, 0.05, 0.99);

        e.finalizar(HORA_FIN, random);

        assertEquals(TConsecuencia.MUERTO, e.getParticipaciones().get(0).getConsecuencia());
        assertEquals(1, e.contarConsecuencias(TConsecuencia.MUERTO));
        verify(conductor).procesarConsecuencia(TConsecuencia.MUERTO, HORA_FIN);
    }

    @Test
    public void testFinalizarConsecuenciaDependeDeLaEspecialidadDelSecuaz() {
        // El asaltante tiene probMuerte = 30. La misma tirada (0.2 --> 20) que a un CONDUCTOR (probMuerte 10)
        // lo detendria, a un ASALTO lo mata, porque 20 < 30.
        Ejecucion e = iniciarBase();
        // conductor: sin incidente | asaltante: incidente 0.0, consecuencia 0.2
        tiradas(0.99, 0.99, 0.0, 0.2);

        e.finalizar(HORA_FIN, random);

        assertEquals(TConsecuencia.NINGUNA, e.getParticipaciones().get(0).getConsecuencia());
        assertEquals(TConsecuencia.MUERTO, e.getParticipaciones().get(1).getConsecuencia());
    }

    @Test
    public void testFinalizarSecuazVeteranoTieneMenosProbabilidadDeIncidente() {
        // Con numMisiones = 6, probIncidente = 40 / 1.6 = 25. La tirada 0.3 (30) ya NO es incidente.
        when(conductor.numMisiones()).thenReturn(6);
        Ejecucion e = iniciarBase();
        tiradas(0.99, 0.3, 0.99);

        e.finalizar(HORA_FIN, random);

        assertEquals(TConsecuencia.NINGUNA, e.getParticipaciones().get(0).getConsecuencia());
    }

    @Test
    public void testFinalizarSecuazNovatoSiSufreIncidenteConLaMismaTirada() {
        // Con numMisiones = 0, probIncidente = 40. La misma tirada 0.3 (30 < 40) SI es incidente.
        Ejecucion e = iniciarBase();
        tiradas(0.99, 0.3, 0.99, 0.99);

        e.finalizar(HORA_FIN, random);

        assertEquals(TConsecuencia.DETENIDO, e.getParticipaciones().get(0).getConsecuencia());
    }

    // ==========================================================================================
    // finalizar: material
    // ==========================================================================================

    @Test
    public void testFinalizarDevuelveAlAlmacenLoQueNoSePierde() {
        Objeto ganzua = mock(Objeto.class);
        RequisitoMaterial req = requisito("Ganzua", 3, ganzua);
        when(misionMock.getRequisitosMateriales()).thenReturn(List.of(req));
        when(ganzua.calcularUnidadesPerdidas(3, true, random)).thenReturn(1);
        Ejecucion e = iniciarBase();
        tiradas(0.0);

        e.finalizar(HORA_FIN, random);

        verify(ganzua).añadir(2);                      // 3 - 1 perdida
        assertEquals("Ganzua x 1", e.getMaterialPerdido());
    }

    @Test
    public void testFinalizarMaterialTodoPerdidoNoDevuelveNada() {
        Objeto explosivo = mock(Objeto.class);
        RequisitoMaterial req = requisito("Explosivo", 3, explosivo);
        when(misionMock.getRequisitosMateriales()).thenReturn(List.of(req));
        when(explosivo.calcularUnidadesPerdidas(3, true, random)).thenReturn(3);
        Ejecucion e = iniciarBase();
        tiradas(0.0);

        e.finalizar(HORA_FIN, random);

        verify(explosivo, never()).añadir(anyInt());   // añadir(0) lanzaria IllegalArgumentException
        assertEquals("Explosivo x 3", e.getMaterialPerdido());
    }

    @Test
    public void testFinalizarMaterialSinPerdidas() {
        Objeto camion = mock(Objeto.class);
        RequisitoMaterial req = requisito("Camion", 1, camion);
        when(misionMock.getRequisitosMateriales()).thenReturn(List.of(req));
        when(camion.calcularUnidadesPerdidas(1, true, random)).thenReturn(0);
        Ejecucion e = iniciarBase();
        tiradas(0.0);

        e.finalizar(HORA_FIN, random);

        verify(camion).añadir(1);
        assertEquals("", e.getMaterialPerdido());
    }

    @Test
    public void testFinalizarVariosMaterialesPerdidosSeparadosPorComa() {
        Objeto ganzua = mock(Objeto.class);
        Objeto taladro = mock(Objeto.class);
        RequisitoMaterial r1 = requisito("Ganzua", 2, ganzua);
        RequisitoMaterial r2 = requisito("Taladro", 3, taladro);
        when(misionMock.getRequisitosMateriales()).thenReturn(List.of(r1, r2));
        when(ganzua.calcularUnidadesPerdidas(2, true, random)).thenReturn(1);
        when(taladro.calcularUnidadesPerdidas(3, true, random)).thenReturn(2);
        Ejecucion e = iniciarBase();
        tiradas(0.0);

        e.finalizar(HORA_FIN, random);

        assertEquals("Ganzua x 1, Taladro x 2", e.getMaterialPerdido());
    }

    @Test
    public void testFinalizarPasaAlObjetoSiHuboExitoOFracaso() {
        Objeto ganzua = mock(Objeto.class);
        RequisitoMaterial req = requisito("Ganzua", 2, ganzua);
        when(misionMock.getRequisitosMateriales()).thenReturn(List.of(req));
        Ejecucion e = iniciarBase();
        tiradas(0.99); // fracaso

        e.finalizar(HORA_FIN, random);

        verify(ganzua).calcularUnidadesPerdidas(2, false, random);
    }

    // ==========================================================================================
    // finalizar: actualizar el estado de la mision (siempre el ultimo paso)
    // ==========================================================================================

    @Test
    public void testFinalizarActualizaElEstadoDeLaMisionAlFinal() {
        Ejecucion e = iniciarBase();
        tiradas(0.0);

        e.finalizar(HORA_FIN, random);

        // Primero se resuelve a los secuaces y despues se actualiza la mision
        InOrder orden = inOrder(conductor, misionMock);
        orden.verify(conductor).procesarConsecuencia(TConsecuencia.NINGUNA, HORA_FIN);
        orden.verify(misionMock).actualizarEstado();
    }
}
