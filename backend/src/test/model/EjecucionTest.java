package test.model;

import main.model.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Pol
 */

// Silent: los helpers configuran mas stubs de los que usa cada test y el runner estricto fallaria por stubs sin usar
@RunWith(MockitoJUnitRunner.Silent.class)
public class EjecucionTest {
    private static final int ID = 1;
    private static final int HORA_INICIO = 5;
    private static final int DURACION = 12;
    private static final int HORA_FIN = HORA_INICIO + DURACION;
    private static final long BOTIN = 10000L;

    // Mocks
    @Mock private Mision mision;
    @Mock private PlantillaRol plantillaConductor;
    @Mock private PlantillaRol plantillaHacker;
    @Mock private Secuaz conductor;
    @Mock private Secuaz hacker;
    @Mock private Secuaz asalto;
    @Mock private Random random;
    // Don y Reloj no tienen mock pq con mockito 4.9 sus getInstance() son estaticos: se usan los reales y se reinician
    // Participacion tampoco: la crea iniciar() con new, asi que es la real (con Secuaz simulado)

    // Por defecto: mision de 2 puestos (conductor y hacker), 10% de fracaso, sin material, y secuaces libres en su especialidad
    @Before
    public void setUp() {
        Don.reiniciar();
        Reloj.reiniciar();

        when(plantillaConductor.getRol()).thenReturn(TRol.CONDUCTOR);
        when(plantillaConductor.getCantidad()).thenReturn(1);
        when(plantillaHacker.getRol()).thenReturn(TRol.HACKER);
        when(plantillaHacker.getCantidad()).thenReturn(1);

        when(mision.getNombreClave()).thenReturn("Robo_Licoreria");
        when(mision.getBotin()).thenReturn(BOTIN);
        when(mision.getProbFracaso()).thenReturn(10.0);
        when(mision.getDuracion()).thenReturn(DURACION);
        when(mision.getPersonalTotal()).thenReturn(2);
        when(mision.puedeIniciarse()).thenReturn(true);
        when(mision.getPlantillasRol()).thenReturn(List.of(plantillaConductor, plantillaHacker));
        when(mision.getCantidadRol(TRol.CONDUCTOR)).thenReturn(1);
        when(mision.getCantidadRol(TRol.HACKER)).thenReturn(1);
        when(mision.getRequisitosMateriales()).thenReturn(new ArrayList<RequisitoMaterial>());

        when(conductor.getEspecialidad()).thenReturn(TRol.CONDUCTOR);
        when(conductor.esLibre()).thenReturn(true);
        when(hacker.getEspecialidad()).thenReturn(TRol.HACKER);
        when(hacker.esLibre()).thenReturn(true);
        when(asalto.getEspecialidad()).thenReturn(TRol.ASALTO);
        when(asalto.esLibre()).thenReturn(true);
    }

    // Como son singletons y son estaticos se limpian para evitar interferir en otros tests
    @After
    public void tearDown() {
        Don.reiniciar();
        Reloj.reiniciar();
    }

    // Equipo valido: cada secuaz en el puesto de su especialidad.
    // EnumMap para que el orden sea siempre CONDUCTOR y luego HACKER (las tiradas dependen de ese orden)
    private Map<TRol, List<Secuaz>> asignacionValida() {
        Map<TRol, List<Secuaz>> asignacion = new EnumMap<>(TRol.class);
        asignacion.put(TRol.CONDUCTOR, List.of(conductor));
        asignacion.put(TRol.HACKER, List.of(hacker));
        return asignacion;
    }

    // Equipo con los dos secuaces cambiados de puesto (ninguno en su especialidad)
    private Map<TRol, List<Secuaz>> asignacionIntercambiada() {
        Map<TRol, List<Secuaz>> asignacion = new EnumMap<>(TRol.class);
        asignacion.put(TRol.CONDUCTOR, List.of(hacker));
        asignacion.put(TRol.HACKER, List.of(conductor));
        return asignacion;
    }

    private Ejecucion iniciarManual() {
        return Ejecucion.iniciar(ID, mision, TEjecucion.MANUAL, HORA_INICIO, asignacionValida());
    }

    private Ejecucion iniciarYFinalizar() {
        Ejecucion e = iniciarManual();
        e.finalizar(HORA_FIN, random);
        return e;
    }

    // Las siguientes llamadas a random.nextDouble() devuelven estos valores, en orden:
    // primero la tirada de exito y luego, si hay fracaso, la de incidente de cada participante
    // (y la de consecuencia de los que tienen incidente)
    private void tiradas(Double primera, Double... siguientes) {
        when(random.nextDouble()).thenReturn(primera, siguientes);
    }

    // Añade a la mision un requisito de `cantidad` unidades de un objeto nuevo, con stock suficiente
    private RequisitoMaterial añadirMaterial(String nombre, int cantidad) {
        Objeto objeto = mock(Objeto.class);
        when(objeto.getNombre()).thenReturn(nombre);
        RequisitoMaterial requisito = mock(RequisitoMaterial.class);
        when(requisito.getObjeto()).thenReturn(objeto);
        when(requisito.getCantidad()).thenReturn(cantidad);
        when(requisito.esSuficienteEnAlmacen()).thenReturn(true);

        List<RequisitoMaterial> requisitos = new ArrayList<>(mision.getRequisitosMateriales());
        requisitos.add(requisito);
        when(mision.getRequisitosMateriales()).thenReturn(requisitos);
        return requisito;
    }

    //calcularProbabilidadExito

    // Comprueba que con todos los secuaces en su especialidad la probabilidad es 100 - k
    @Test
    public void testProbabilidadTodosEnSuEspecialidad() {
        assertEquals(90.0, Ejecucion.calcularProbabilidadExito(mision, asignacionValida()), 0.001);
    }

    // Comprueba que con ningun secuaz en su especialidad la probabilidad es 0
    @Test
    public void testProbabilidadSinNingunEncaje() {
        assertEquals(0.0, Ejecucion.calcularProbabilidadExito(mision, asignacionIntercambiada()), 0.001);
    }

    // Comprueba que con 1 encaje de 2 puestos la probabilidad es la mitad de 100 - k
    @Test
    public void testProbabilidadParcial() {
        Map<TRol, List<Secuaz>> asignacion = new EnumMap<>(TRol.class);
        asignacion.put(TRol.CONDUCTOR, List.of(conductor));
        asignacion.put(TRol.HACKER, List.of(asalto));   // el asalto sustituye al hacker

        assertEquals(45.0, Ejecucion.calcularProbabilidadExito(mision, asignacion), 0.001);
    }

    // Comprueba que con k = 0 y todos encajados la probabilidad es 100
    @Test
    public void testProbabilidadConFracasoCero() {
        when(mision.getProbFracaso()).thenReturn(0.0);

        assertEquals(100.0, Ejecucion.calcularProbabilidadExito(mision, asignacionValida()), 0.001);
    }

    // Comprueba que con k = 100 la probabilidad es 0 aunque todos encajen
    @Test
    public void testProbabilidadConFracasoCien() {
        when(mision.getProbFracaso()).thenReturn(100.0);

        assertEquals(0.0, Ejecucion.calcularProbabilidadExito(mision, asignacionValida()), 0.001);
    }

    // Comprueba que es una funcion pura: no ocupa secuaces ni registra nada en la mision
    @Test
    public void testProbabilidadNoModificaNada() {
        Ejecucion.calcularProbabilidadExito(mision, asignacionValida());

        verify(conductor, never()).ocupar();
        verify(hacker, never()).ocupar();
        verify(mision, never()).registrarEjecucion(any(Ejecucion.class));
    }

    //iniciar

    // Comprueba que iniciar deja la ejecucion con todos sus atributos iniciales
    @Test
    public void testIniciarInicializaLaEjecucion() {
        Ejecucion e = iniciarManual();

        assertEquals(ID, e.getId());
        assertSame(mision, e.getMision());
        assertEquals(TEjecucion.MANUAL, e.getTipo());
        assertEquals(HORA_INICIO, e.getInicio());
        assertEquals(HORA_FIN, e.getFin());
        assertEquals(90.0, e.getProbExito(), 0.001);
        assertEquals(TResultado.EN_CURSO, e.getResultado());
        assertTrue(e.estaEnCurso());
        assertFalse(e.isLeida());
        assertEquals("", e.getMaterialPerdido());
        assertEquals(2, e.getParticipaciones().size());
    }

    // Comprueba que se guarda el tipo AUTOMATICA cuando la ejecucion es de la ronda automatica
    @Test
    public void testIniciarTipoAutomatica() {
        Ejecucion e = Ejecucion.iniciar(ID, mision, TEjecucion.AUTOMATICA, HORA_INICIO, asignacionValida());

        assertEquals(TEjecucion.AUTOMATICA, e.getTipo());
    }

    // Comprueba que se crea una participacion por secuaz, con el rol del puesto que ocupa
    @Test
    public void testIniciarCreaUnaParticipacionPorSecuaz() {
        Ejecucion e = iniciarManual();

        assertSame(conductor, e.getParticipaciones().get(0).getSecuaz());
        assertEquals(TRol.CONDUCTOR, e.getParticipaciones().get(0).getRolDesempenado());
        assertSame(hacker, e.getParticipaciones().get(1).getSecuaz());
        assertEquals(TRol.HACKER, e.getParticipaciones().get(1).getRolDesempenado());
        assertSame(e, e.getParticipaciones().get(0).getEjecucion());
    }

    // Comprueba que un secuaz sustituto participa con el rol del puesto, no con su especialidad
    @Test
    public void testIniciarConSustitucionDeRoles() {
        Ejecucion e = Ejecucion.iniciar(ID, mision, TEjecucion.MANUAL, HORA_INICIO, asignacionIntercambiada());

        assertEquals(TRol.CONDUCTOR, e.getParticipaciones().get(0).getRolDesempenado());
        assertSame(hacker, e.getParticipaciones().get(0).getSecuaz());
        assertFalse(e.getParticipaciones().get(0).esEnSuEspecialidad());
        assertEquals(0.0, e.getProbExito(), 0.001);
    }

    // Comprueba que todos los secuaces del equipo pasan a ocupados
    @Test
    public void testIniciarOcupaALosSecuaces() {
        iniciarManual();

        verify(conductor).ocupar();
        verify(hacker).ocupar();
    }

    // Comprueba que la ejecucion se registra en la mision
    @Test
    public void testIniciarRegistraLaEjecucionEnLaMision() {
        Ejecucion e = iniciarManual();

        verify(mision).registrarEjecucion(e);
    }

    // Comprueba que al iniciar se descuenta del almacen el material que pide la mision
    @Test
    public void testIniciarDescuentaElMaterial() {
        RequisitoMaterial requisito = añadirMaterial("Ganzua", 2);
        Objeto objeto = requisito.getObjeto();

        iniciarManual();

        verify(objeto).descontar(2);
    }

    // Comprueba que una mision que no puede iniciarse lanza excepcion y no cambia nada
    @Test
    public void testIniciarMisionNoIniciableExcepcion() {
        when(mision.puedeIniciarse()).thenReturn(false);

        try {
            iniciarManual();
            fail("Se esperaba un IllegalStateException");
        } catch (IllegalStateException e) {
            verify(conductor, never()).ocupar();
            verify(mision, never()).registrarEjecucion(any(Ejecucion.class));
        }
    }

    // Comprueba que falta un puesto de la plantilla sin cubrir lanza excepcion
    @Test(expected = IllegalArgumentException.class)
    public void testIniciarPuestoSinCubrirExcepcion() {
        Map<TRol, List<Secuaz>> asignacion = new EnumMap<>(TRol.class);
        asignacion.put(TRol.CONDUCTOR, List.of(conductor));

        Ejecucion.iniciar(ID, mision, TEjecucion.MANUAL, HORA_INICIO, asignacion);
    }

    // Comprueba que un puesto con mas secuaces de los que pide la plantilla lanza excepcion
    @Test(expected = IllegalArgumentException.class)
    public void testIniciarPuestoConCantidadIncorrectaExcepcion() {
        Map<TRol, List<Secuaz>> asignacion = asignacionValida();
        asignacion.put(TRol.CONDUCTOR, List.of(conductor, asalto));

        Ejecucion.iniciar(ID, mision, TEjecucion.MANUAL, HORA_INICIO, asignacion);
    }

    // Comprueba que un puesto que no esta en la plantilla lanza excepcion
    @Test(expected = IllegalArgumentException.class)
    public void testIniciarPuestoFueraDePlantillaExcepcion() {
        Map<TRol, List<Secuaz>> asignacion = asignacionValida();
        asignacion.put(TRol.ASALTO, List.of(asalto));

        Ejecucion.iniciar(ID, mision, TEjecucion.MANUAL, HORA_INICIO, asignacion);
    }

    // Comprueba que un mismo secuaz en dos puestos lanza excepcion
    @Test(expected = IllegalArgumentException.class)
    public void testIniciarSecuazRepetidoExcepcion() {
        Map<TRol, List<Secuaz>> asignacion = new EnumMap<>(TRol.class);
        asignacion.put(TRol.CONDUCTOR, List.of(conductor));
        asignacion.put(TRol.HACKER, List.of(conductor));

        Ejecucion.iniciar(ID, mision, TEjecucion.MANUAL, HORA_INICIO, asignacion);
    }

    // Comprueba que un secuaz nulo en el equipo lanza excepcion
    @Test(expected = IllegalArgumentException.class)
    public void testIniciarSecuazNuloExcepcion() {
        Map<TRol, List<Secuaz>> asignacion = new EnumMap<>(TRol.class);
        asignacion.put(TRol.CONDUCTOR, List.of(conductor));
        asignacion.put(TRol.HACKER, Arrays.asList((Secuaz) null));

        Ejecucion.iniciar(ID, mision, TEjecucion.MANUAL, HORA_INICIO, asignacion);
    }

    // Comprueba que un secuaz que no esta libre lanza excepcion y que no se ocupa a ningun otro secuaz del equipo
    @Test
    public void testIniciarSecuazNoLibreExcepcionYNoCambiaNada() {
        when(hacker.esLibre()).thenReturn(false);

        try {
            iniciarManual();
            fail("Se esperaba un IllegalStateException");
        } catch (IllegalStateException e) {
            verify(conductor, never()).ocupar();
            verify(hacker, never()).ocupar();
            verify(mision, never()).registrarEjecucion(any(Ejecucion.class));
        }
    }

    // Comprueba que si no hay material suficiente lanza excepcion y no se descuenta nada
    @Test
    public void testIniciarMaterialInsuficienteExcepcionYNoCambiaNada() {
        RequisitoMaterial requisito = añadirMaterial("Ganzua", 2);
        when(requisito.esSuficienteEnAlmacen()).thenReturn(false);
        Objeto objeto = requisito.getObjeto();

        try {
            iniciarManual();
            fail("Se esperaba un IllegalStateException");
        } catch (IllegalStateException e) {
            verify(objeto, never()).descontar(anyInt());
            verify(conductor, never()).ocupar();
        }
    }

    // Comprueba que un id que no es mayor que 0 lanza excepcion antes de modificar nada
    @Test
    public void testIniciarIdInvalidoExcepcion() {
        try {
            Ejecucion.iniciar(0, mision, TEjecucion.MANUAL, HORA_INICIO, asignacionValida());
            fail("Se esperaba un IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            verify(conductor, never()).ocupar();
        }
    }

    // Comprueba que un tipo de ejecucion nulo lanza excepcion
    @Test(expected = IllegalArgumentException.class)
    public void testIniciarTipoNuloExcepcion() {
        Ejecucion.iniciar(ID, mision, null, HORA_INICIO, asignacionValida());
    }

    // Comprueba que una hora de inicio negativa lanza excepcion
    @Test(expected = IllegalArgumentException.class)
    public void testIniciarHoraNegativaExcepcion() {
        Ejecucion.iniciar(ID, mision, TEjecucion.MANUAL, -1, asignacionValida());
    }

    //consultas en curso

    // Comprueba que el tiempo restante baja hora a hora y nunca es negativo
    @Test
    public void testGetTiempoRestante() {
        Ejecucion e = iniciarManual();

        assertEquals(DURACION, e.getTiempoRestante(HORA_INICIO));
        assertEquals(1, e.getTiempoRestante(HORA_FIN - 1));
        assertEquals(0, e.getTiempoRestante(HORA_FIN));
        assertEquals(0, e.getTiempoRestante(HORA_FIN + 10));
    }

    // Comprueba que getParticipaciones devuelve una lista inmodificable
    @Test(expected = UnsupportedOperationException.class)
    public void testGetParticipacionesEsInmodificable() {
        iniciarManual().getParticipaciones().clear();
    }

    // Comprueba que mientras esta en curso no hay cobros, ni comision del Don, ni consecuencias
    @Test
    public void testEnCursoNoTieneCobrosNiConsecuencias() {
        Ejecucion e = iniciarManual();

        assertEquals(0L, e.getTotalCobrado());
        assertEquals(0L, e.getComisionDon());
        assertEquals(0, e.contarConsecuencias(TConsecuencia.MUERTO));
        assertEquals(0, e.contarConsecuencias(TConsecuencia.DETENIDO));
    }

    // Comprueba que marcarComoLeida no hace nada mientras la ejecucion esta en curso
    @Test
    public void testMarcarComoLeidaEnCursoNoHaceNada() {
        Ejecucion e = iniciarManual();

        e.marcarComoLeida();

        assertFalse(e.isLeida());
    }

    // finalizar con exito

    // Comprueba que con exito cada participante cobra su comision, el Don se queda lo que sobra
    // y la ejecucion queda terminada y sin leer
    @Test
    public void testFinalizarExito() {
        tiradas(0.5);   // 50 < 90: exito

        Ejecucion e = iniciarYFinalizar();

        assertEquals(TResultado.EXITO, e.getResultado());
        assertFalse(e.estaEnCurso());
        assertFalse(e.isLeida());
        assertEquals(1800L, e.getTotalCobrado());   // 15% (conductor) + 3% (hacker) de 10000
        assertEquals(8200L, e.getComisionDon());
        assertEquals(1500L, e.getParticipaciones().get(0).getCobrado());
        assertEquals(300L, e.getParticipaciones().get(1).getCobrado());
    }

    // Comprueba que con exito los secuaces acumulan su ganancia y quedan libres de nuevo, sin consecuencias
    @Test
    public void testFinalizarExitoActualizaALosSecuaces() {
        tiradas(0.5);

        Ejecucion e = iniciarYFinalizar();

        verify(conductor).acumularGanancia(1500L);
        verify(hacker).acumularGanancia(300L);
        verify(conductor).procesarConsecuencia(TConsecuencia.NINGUNA, HORA_FIN);
        verify(hacker).procesarConsecuencia(TConsecuencia.NINGUNA, HORA_FIN);
        assertEquals(2, e.contarConsecuencias(TConsecuencia.NINGUNA));
    }

    // Comprueba que con exito el Don registra la comision en la caja y que no se hacen tiradas de incidente
    @Test
    public void testFinalizarExitoRegistraLaComisionEnElDon() {
        tiradas(0.5);

        iniciarYFinalizar();

        Don don = Don.getInstance();
        assertEquals(8200L, don.getCaja());
        assertEquals(1, don.getMovimientos().size());
        assertEquals(TMovimiento.COMISION, don.getMovimientos().get(0).getTipo());
        assertEquals(8200L, don.getMovimientos().get(0).getImporte());
        assertTrue(don.getMovimientos().get(0).getConcepto().contains("Robo_Licoreria"));
        verify(random, times(1)).nextDouble();
    }

    // Comprueba que con exito la mision actualiza su estado
    @Test
    public void testFinalizarActualizaElEstadoDeLaMision() {
        tiradas(0.5);

        iniciarYFinalizar();

        verify(mision).actualizarEstado();
    }

    // Comprueba que si los secuaces cobran mas del botin la comision del Don es negativa y deja la caja por debajo de 0
    @Test
    public void testFinalizarComisionDonNegativa() {
        // 6 asaltos cobran un 20% cada uno (120% del botin)
        PlantillaRol plantillaAsalto = mock(PlantillaRol.class);
        when(plantillaAsalto.getRol()).thenReturn(TRol.ASALTO);
        when(plantillaAsalto.getCantidad()).thenReturn(6);
        when(mision.getPlantillasRol()).thenReturn(List.of(plantillaAsalto));
        when(mision.getCantidadRol(TRol.ASALTO)).thenReturn(6);
        when(mision.getPersonalTotal()).thenReturn(6);
        when(mision.getProbFracaso()).thenReturn(0.0);
        List<Secuaz> asaltos = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Secuaz s = mock(Secuaz.class);
            when(s.getEspecialidad()).thenReturn(TRol.ASALTO);
            when(s.esLibre()).thenReturn(true);
            asaltos.add(s);
        }
        Map<TRol, List<Secuaz>> asignacion = new EnumMap<>(TRol.class);
        asignacion.put(TRol.ASALTO, asaltos);
        tiradas(0.5);

        Ejecucion e = Ejecucion.iniciar(ID, mision, TEjecucion.MANUAL, HORA_INICIO, asignacion);
        e.finalizar(HORA_FIN, random);

        assertEquals(TResultado.EXITO, e.getResultado());
        assertEquals(12000L, e.getTotalCobrado());
        assertEquals(-2000L, e.getComisionDon());
        assertEquals(-2000L, Don.getInstance().getCaja());
    }

    // finalizar con fracaso

    // Comprueba que con fracaso nadie cobra y el Don no registra ninguna comision
    @Test
    public void testFinalizarFracasoNadieCobra() {
        tiradas(0.95, 0.9, 0.9);   // 95 >= 90: fracaso; ningun incidente

        Ejecucion e = iniciarYFinalizar();

        assertEquals(TResultado.FRACASO, e.getResultado());
        assertEquals(0L, e.getTotalCobrado());
        assertEquals(0L, e.getComisionDon());
        assertEquals(0L, Don.getInstance().getCaja());
        assertTrue(Don.getInstance().getMovimientos().isEmpty());
        verify(conductor, never()).acumularGanancia(anyLong());
    }

    // Comprueba que en un fracaso sin incidentes todos quedan sin consecuencias
    @Test
    public void testFinalizarFracasoSinIncidentes() {
        tiradas(0.95, 0.9, 0.9);

        Ejecucion e = iniciarYFinalizar();

        assertEquals(2, e.contarConsecuencias(TConsecuencia.NINGUNA));
        verify(conductor).procesarConsecuencia(TConsecuencia.NINGUNA, HORA_FIN);
        verify(hacker).procesarConsecuencia(TConsecuencia.NINGUNA, HORA_FIN);
        verify(mision).actualizarEstado();
    }

    // Comprueba que un incidente cuya tirada de consecuencia es menor que la probabilidad de muerte del rol mata al secuaz
    // (conductor: incidente 10 < 40, consecuencia 5 < 10 = muerte; hacker: 90 >= 40 sin incidente)
    @Test
    public void testFinalizarFracasoConMuerte() {
        tiradas(0.95, 0.1, 0.05, 0.9);

        Ejecucion e = iniciarYFinalizar();

        verify(conductor).procesarConsecuencia(TConsecuencia.MUERTO, HORA_FIN);
        verify(hacker).procesarConsecuencia(TConsecuencia.NINGUNA, HORA_FIN);
        assertEquals(1, e.contarConsecuencias(TConsecuencia.MUERTO));
        assertEquals(1, e.contarConsecuencias(TConsecuencia.NINGUNA));
        assertEquals(0, e.contarConsecuencias(TConsecuencia.DETENIDO));
    }

    // Comprueba que un incidente cuya tirada de consecuencia supera la probabilidad de muerte detiene al secuaz
    @Test
    public void testFinalizarFracasoConDetencion() {
        tiradas(0.95, 0.1, 0.5, 0.9);   // consecuencia 50 >= 10: detenido

        Ejecucion e = iniciarYFinalizar();

        verify(conductor).procesarConsecuencia(TConsecuencia.DETENIDO, HORA_FIN);
        assertEquals(1, e.contarConsecuencias(TConsecuencia.DETENIDO));
        assertEquals(0, e.contarConsecuencias(TConsecuencia.MUERTO));
    }

    // Comprueba que la experiencia previa reduce la probabilidad de incidente: 40 / (1 + 0,1 * misiones)
    // el conductor con 10 misiones tiene 20% y con la tirada 30 no tiene incidente, el hacker novato (40%) si
    @Test
    public void testFinalizarLaExperienciaReduceLaProbabilidadDeIncidente() {
        when(conductor.numMisiones()).thenReturn(10);
        tiradas(0.95, 0.3, 0.3, 0.0);   // la ultima es la consecuencia del hacker: 0 < 5 = muerte

        iniciarYFinalizar();

        verify(conductor).procesarConsecuencia(TConsecuencia.NINGUNA, HORA_FIN);
        verify(hacker).procesarConsecuencia(TConsecuencia.MUERTO, HORA_FIN);
    }

    // tirada de exito

    // Comprueba que si la tirada es igual a la probabilidad de exito es fracaso (la comparacion es estricta)
    @Test
    public void testFinalizarTiradaIgualALaProbabilidadEsFracaso() {
        Map<TRol, List<Secuaz>> asignacion = asignacionIntercambiada();   // probabilidad 0
        tiradas(0.0, 0.9, 0.9);

        Ejecucion e = Ejecucion.iniciar(ID, mision, TEjecucion.MANUAL, HORA_INICIO, asignacion);
        e.finalizar(HORA_FIN, random);

        assertEquals(TResultado.FRACASO, e.getResultado());
    }

    // Comprueba que con probabilidad 100 cualquier tirada posible es exito
    @Test
    public void testFinalizarProbabilidadCienSiempreExito() {
        when(mision.getProbFracaso()).thenReturn(0.0);
        tiradas(0.999);

        Ejecucion e = iniciarYFinalizar();

        assertEquals(TResultado.EXITO, e.getResultado());
    }

    // material perdido

    // Comprueba que con exito y sin perdidas se devuelve todo el material al almacen y no se anota nada
    @Test
    public void testFinalizarExitoDevuelveTodoElMaterial() {
        RequisitoMaterial requisito = añadirMaterial("Ganzua", 3);
        Objeto objeto = requisito.getObjeto();
        when(objeto.calcularUnidadesPerdidas(3, true, random)).thenReturn(0);
        tiradas(0.5);

        Ejecucion e = iniciarYFinalizar();

        verify(objeto).añadir(3);
        assertEquals("", e.getMaterialPerdido());
    }

    // Comprueba que las unidades no perdidas vuelven al almacen y las perdidas se anotan
    @Test
    public void testFinalizarFracasoPerdidaParcialDeMaterial() {
        RequisitoMaterial requisito = añadirMaterial("Ganzua", 3);
        Objeto objeto = requisito.getObjeto();
        when(objeto.calcularUnidadesPerdidas(3, false, random)).thenReturn(1);
        tiradas(0.95, 0.9, 0.9);

        Ejecucion e = iniciarYFinalizar();

        verify(objeto).añadir(2);
        assertEquals("Ganzua x 1", e.getMaterialPerdido());
    }

    // Comprueba que si se pierden todas las unidades no se devuelve nada al almacen
    @Test
    public void testFinalizarPerdidaTotalDeMaterial() {
        RequisitoMaterial requisito = añadirMaterial("C4", 3);
        Objeto objeto = requisito.getObjeto();
        when(objeto.calcularUnidadesPerdidas(3, true, random)).thenReturn(3);   // objeto que se gasta
        tiradas(0.5);

        Ejecucion e = iniciarYFinalizar();

        verify(objeto, never()).añadir(anyInt());
        assertEquals("C4 x 3", e.getMaterialPerdido());
    }

    // Comprueba que con varios objetos solo se anotan los que han perdido unidades, separados por coma
    @Test
    public void testFinalizarVariosObjetosSoloAnotaLosPerdidos() {
        Objeto ganzua = añadirMaterial("Ganzua", 3).getObjeto();
        Objeto c4 = añadirMaterial("C4", 2).getObjeto();
        Objeto furgon = añadirMaterial("Furgon", 1).getObjeto();
        when(ganzua.calcularUnidadesPerdidas(3, false, random)).thenReturn(0);
        when(c4.calcularUnidadesPerdidas(2, false, random)).thenReturn(2);
        when(furgon.calcularUnidadesPerdidas(1, false, random)).thenReturn(1);
        tiradas(0.95, 0.9, 0.9);

        Ejecucion e = iniciarYFinalizar();

        assertEquals("C4 x 2, Furgon x 1", e.getMaterialPerdido());
        verify(ganzua).añadir(3);
    }

    //restricciones de finalizar

    // Comprueba que no se puede finalizar antes de la hora de fin, sin hacer ninguna tirada
    @Test
    public void testFinalizarAntesDeSuFinExcepcion() {
        Ejecucion e = iniciarManual();

        try {
            e.finalizar(HORA_FIN - 1, random);
            fail("Se esperaba un IllegalStateException");
        } catch (IllegalStateException ex) {
            assertEquals(TResultado.EN_CURSO, e.getResultado());
            verify(random, never()).nextDouble();
        }
    }

    // Comprueba que una ejecucion ya terminada no se puede finalizar otra vez
    @Test
    public void testFinalizarDosVecesExcepcion() {
        tiradas(0.5);
        Ejecucion e = iniciarYFinalizar();

        try {
            e.finalizar(HORA_FIN, random);
            fail("Se esperaba un IllegalStateException");
        } catch (IllegalStateException ex) {
            assertEquals(TResultado.EXITO, e.getResultado());
            assertEquals(8200L, Don.getInstance().getCaja());   // la comision no se registra dos veces
        }
    }

    // notificacion

    // Comprueba que al terminar queda sin leer y que marcarComoLeida la marca como leida
    @Test
    public void testMarcarComoLeidaTrasFinalizar() {
        tiradas(0.5);
        Ejecucion e = iniciarYFinalizar();
        assertFalse(e.isLeida());

        e.marcarComoLeida();

        assertTrue(e.isLeida());
    }
}