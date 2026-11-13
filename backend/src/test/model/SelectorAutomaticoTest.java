package test.uc;

import org.junit.*;
import static org.junit.Assert.*;

import main.model.*;
import main.uc.SelectorAutomatico;

import java.util.*;

/**
 * @author Pol
 */
public class SelectorAutomaticoTest {

    private static final double DELTA = 1e-6;

    // Los 9 secuaces del enunciado, indexados por id (la posicion 0 no se usa)
    private Secuaz[] sec;
    private List<Secuaz> secuacesEnunciado;

    // Las 3 misiones del enunciado sin dependencias, y una bloqueada que no debe aparecer
    private Mision licoreria;
    private Mision furgon;
    private Mision rectorado;
    private Mision bloqueada;
    private List<Mision> misionesEnunciado;

    @Before
    public void setUp() {
        Azar.setSemilla(1L);

        sec = new Secuaz[10];
        sec[1] = secuaz(1, TRol.CONDUCTOR);
        sec[2] = secuaz(2, TRol.CONDUCTOR);
        sec[3] = secuaz(3, TRol.ASALTO);
        sec[4] = secuaz(4, TRol.ASALTO);
        sec[5] = secuaz(5, TRol.ASALTO);
        sec[6] = secuaz(6, TRol.HACKER);
        sec[7] = secuaz(7, TRol.CERRAJERO);
        sec[8] = secuaz(8, TRol.NEGOCIADOR);
        sec[9] = secuaz(9, TRol.NEGOCIADOR);
        secuacesEnunciado = new ArrayList<>(Arrays.asList(sec).subList(1, 10));

        licoreria = mision("Robo_Licoreria", 20000, 10, TRol.CONDUCTOR, 1, TRol.ASALTO, 2);
        furgon = mision("Asalto_Furgon", 10000, 5, TRol.CONDUCTOR, 1, TRol.HACKER, 1);
        rectorado = mision("Asalto_A_Rectorado", 5000, 3, TRol.CONDUCTOR, 1, TRol.NEGOCIADOR, 1);
        bloqueada = mision("Infiltracion_Lifeinvader", 5000, 15, TRol.CONDUCTOR, 1, TRol.NEGOCIADOR, 1, TRol.ASALTO, 1);
        bloquear(bloqueada);
        misionesEnunciado = new ArrayList<>(Arrays.asList(licoreria, furgon, rectorado, bloqueada));
    }

    @After
    public void tearDown() {
        Azar.reiniciar();
    }


    private static Mision mision(String nombre, long botin, double probFracaso, Object... plantilla) {
        Map<TRol,Integer> map = new EnumMap<>(TRol.class);
        for (int i = 0; i < plantilla.length; i += 2) map.put((TRol) plantilla[i], (Integer) plantilla[i+1]);
        return new Mision(nombre, "u", botin, probFracaso, null, map);
    }
    // Hace que la mision no pueda iniciarse: le ponemos un prerrequisito que aun no esta completado, y asi queda BLOQUEADA
    private static void bloquear(Mision m) {
        m.añadirPrerrequisito(mision("Pre_" + m.getNombreClave(), 1, 0, TRol.CONDUCTOR, 1));
    }
    private static Secuaz secuaz(int id, TRol rol) { return new Secuaz(id, "S" + id, "a", rol); }
    private static Secuaz secuazOcupado(int id, TRol rol) { Secuaz s = secuaz(id, rol); s.ocupar(); return s; }
    private static Objeto objeto(String nombre, long precio, int stock) {
        Objeto o = new Objeto(nombre, "d", precio, TUso.SE_GASTA, TCategoria.HERRAMIENTA);
        if (stock > 0) o.añadir(stock);
        return o;
    }
    // Anade un requisito de material a la mision. Mision.añadirRequisitoMaterial aun es de la 2.ª entrega (lanza excepcion),
    // asi que metemos el requisito directamente en la lista privada con reflexion. Cuando se implemente, usar esa funcion
    @SuppressWarnings("unchecked")
    private static void requiere(Mision m, Objeto objeto, int cantidad) {
        try {
            java.lang.reflect.Field f = Mision.class.getDeclaredField("requisitosMateriales");
            f.setAccessible(true);
            ((List<RequisitoMaterial>) f.get(m)).add(new RequisitoMaterial(m, objeto, cantidad));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    // ===== FIN AYUDAS =====

    // Devuelve la asignacion de la propuesta que corresponde a una mision, o null si no esta
    private static AsignacionPropuesta asignacionDe(PropuestaAutomatica p, Mision m) {
        for (AsignacionPropuesta a : p.getAsignaciones()) {
            if (a.getMision() == m) return a;
        }
        return null;
    }

    private static double sumaBeneficios(PropuestaAutomatica p) {
        double suma = 0;
        for (AsignacionPropuesta a : p.getAsignaciones()) suma += a.getBeneficioEsperado();
        return suma;
    }

    // Describe una propuesta entera en un texto, para poder comparar dos propuestas
    private static String describir(PropuestaAutomatica p) {
        StringBuilder sb = new StringBuilder();
        for (AsignacionPropuesta a : p.getAsignaciones()) {
            sb.append(a.getMision().getNombreClave()).append(" P=").append(a.getProbExito())
                    .append(" B=").append(a.getBeneficioEsperado()).append(" ");
            for (Map.Entry<TRol, List<Secuaz>> e : a.getAsignacion().entrySet()) {
                sb.append(e.getKey()).append(":");
                for (Secuaz s : e.getValue()) sb.append(s.getId()).append(",");
                sb.append(" ");
            }
            sb.append("| ");
        }
        sb.append(p.getExcluidas());
        return sb.toString();
    }

    // Peso de una mision (lo que aporta un encaje), calculado por separado para el optimo de referencia
    private static double peso(Mision m) {
        return m.getBotin() * (100 - m.getProbFracaso()) / (100.0 * m.getPersonalTotal());
    }

    // Optimo de referencia: prueba los 2^m subconjuntos de misiones (sin material) y se queda con el mejor
    private static double optimoExhaustivo(List<Mision> misiones, List<Secuaz> secuaces) {
        int m = misiones.size();
        double mejor = 0;
        for (int mascara = 0; mascara < (1 << m); mascara++) {
            List<Mision> elegidas = new ArrayList<>();
            int personal = 0;
            for (int i = 0; i < m; i++) {
                if ((mascara & (1 << i)) != 0) {
                    elegidas.add(misiones.get(i));
                    personal += misiones.get(i).getPersonalTotal();
                }
            }
            if (personal > secuaces.size()) continue;
            elegidas.sort((a, b) -> Double.compare(peso(b), peso(a)));
            Map<TRol, Integer> libres = new EnumMap<>(TRol.class);
            for (Secuaz s : secuaces) libres.merge(s.getEspecialidad(), 1, Integer::sum);
            double total = 0;
            for (Mision mi : elegidas) {
                for (PlantillaRol pr : mi.getPlantillasRol()) {
                    int usados = Math.min(libres.getOrDefault(pr.getRol(), 0), pr.getCantidad());
                    libres.merge(pr.getRol(), -usados, Integer::sum);
                    total += peso(mi) * usados;
                }
            }
            mejor = Math.max(mejor, total);
        }
        return mejor;
    }

    // Instancia aleatoria reproducible: numMisiones misiones y secuaces libres para cubrir la mitad del personal pedido
    private static List<List<?>> instanciaAleatoria(long semilla, int numMisiones) {
        Random r = new Random(semilla);
        TRol[] roles = TRol.values();
        List<Mision> misiones = new ArrayList<>();
        int demanda = 0;
        for (int i = 0; i < numMisiones; i++) {
            Map<TRol, Integer> plantilla = new EnumMap<>(TRol.class);
            plantilla.put(TRol.CONDUCTOR, 1);
            int extra = 1 + r.nextInt(3);
            for (int j = 0; j < extra; j++) {
                plantilla.merge(roles[1 + r.nextInt(4)], 1, Integer::sum);
            }
            Object[] pares = new Object[plantilla.size() * 2];
            int k = 0;
            for (Map.Entry<TRol, Integer> e : plantilla.entrySet()) {
                pares[k++] = e.getKey();
                pares[k++] = e.getValue();
            }
            Mision mision = mision("M" + i, 2000 + r.nextInt(18000), 10 + r.nextInt(50), pares);
            demanda += mision.getPersonalTotal();
            misiones.add(mision);
        }
        int numSecuaces = Math.max(3, Math.round(demanda * 0.5f));
        List<Secuaz> secuaces = new ArrayList<>();
        for (int i = 0; i < numSecuaces; i++) {
            secuaces.add(secuaz(i + 1, roles[r.nextInt(roles.length)]));
        }
        List<List<?>> instancia = new ArrayList<>();
        instancia.add(misiones);
        instancia.add(secuaces);
        return instancia;
    }

    // ---------- Casos sin solucion o con exclusiones ----------

    //Comprueba que sin misiones la propuesta es vacia y no hay excluidas.
    @Test
    public void testSinMisionesDevuelvePropuestaVacia() {
        PropuestaAutomatica p = SelectorAutomatico.proponer(new ArrayList<>(), secuacesEnunciado, 0);

        assertTrue(p.getAsignaciones().isEmpty());
        assertTrue(p.getExcluidas().isEmpty());
    }

    //Comprueba que sin secuaces libres la mision no se propone y sale en las excluidas con su nombre.
    @Test
    public void testSinSecuacesLibresExcluyeLaMision() {
        Mision m = mision("M1", 1000, 10, TRol.CONDUCTOR, 1, TRol.ASALTO, 1);

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(m), new ArrayList<>(), 0);

        assertTrue(p.getAsignaciones().isEmpty());
        assertEquals(1, p.getExcluidas().size());
        assertTrue(p.getExcluidas().get(0).contains("M1"));
    }

    //Comprueba que los secuaces que no estan libres no se usan: solo va el libre.
    @Test
    public void testSecuacesOcupadosNoSeUsan() {
        Secuaz libre = secuaz(1, TRol.CONDUCTOR);
        Secuaz ocupado = secuazOcupado(2, TRol.CONDUCTOR);
        Mision m = mision("M1", 1000, 0, TRol.CONDUCTOR, 1);

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(m), List.of(ocupado, libre), 0);

        AsignacionPropuesta a = asignacionDe(p, m);
        assertNotNull(a);
        assertEquals(List.of(libre), a.getAsignacion().get(TRol.CONDUCTOR));
    }

    //Comprueba que una mision que necesita mas gente de la que esta libre queda excluida
    //aunque haya otros secuaces ocupados que sumarian suficiente.
    @Test
    public void testMisionConPersonalOcupadoQuedaExcluida() {
        Secuaz libre = secuaz(1, TRol.CONDUCTOR);
        Secuaz ocupado = secuazOcupado(2, TRol.ASALTO);
        Mision m = mision("M1", 1000, 10, TRol.CONDUCTOR, 1, TRol.ASALTO, 1);

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(m), List.of(libre, ocupado), 0);

        assertTrue(p.getAsignaciones().isEmpty());
        assertEquals(1, p.getExcluidas().size());
    }

    //Comprueba que una mision que no puede iniciarse no se propone ni sale como excluida.
    @Test
    public void testMisionNoIniciableSeIgnora() {
        PropuestaAutomatica p = SelectorAutomatico.proponer(misionesEnunciado, secuacesEnunciado, 0);

        assertNull(asignacionDe(p, bloqueada));
        for (String excluida : p.getExcluidas()) {
            assertFalse(excluida.contains("Infiltracion_Lifeinvader"));
        }
    }

    //Comprueba que una mision que pide mas personal del libre queda excluida
    //y que las demas se siguen proponiendo.
    @Test
    public void testMisionConMasPersonalDelDisponibleQuedaExcluidaYLasDemasSePropone() {
        Mision grande = mision("Grande", 90000, 10, TRol.CONDUCTOR, 1, TRol.ASALTO, 4);
        Mision pequena = mision("Pequena", 5000, 10, TRol.CONDUCTOR, 1, TRol.HACKER, 1);
        List<Secuaz> libres = List.of(secuaz(1, TRol.CONDUCTOR), secuaz(2, TRol.HACKER), secuaz(3, TRol.ASALTO));

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(grande, pequena), libres, 0);

        assertNull(asignacionDe(p, grande));
        assertNotNull(asignacionDe(p, pequena));
        assertEquals(1, p.getExcluidas().size());
        assertTrue(p.getExcluidas().get(0).contains("Grande"));
    }

    // ---------- Ejemplo del enunciado (calculado a mano) ----------

    //Comprueba que con los datos del enunciado se eligen las tres misiones disponibles y ninguna se excluye.
    @Test
    public void testEnunciadoEligeLasTresMisionesDisponibles() {
        PropuestaAutomatica p = SelectorAutomatico.proponer(misionesEnunciado, secuacesEnunciado, 0);

        assertEquals(3, p.getAsignaciones().size());
        assertNotNull(asignacionDe(p, licoreria));
        assertNotNull(asignacionDe(p, furgon));
        assertNotNull(asignacionDe(p, rectorado));
        assertTrue(p.getExcluidas().isEmpty());
    }

    //Comprueba las probabilidades de exito: 3 encajes de 3 (k=10), 2 de 2 (k=5) y 1 de 2 (k=3).
    @Test
    public void testEnunciadoProbabilidadesDeExito() {
        PropuestaAutomatica p = SelectorAutomatico.proponer(misionesEnunciado, secuacesEnunciado, 0);

        assertEquals(90.0, asignacionDe(p, licoreria).getProbExito(), DELTA);
        assertEquals(95.0, asignacionDe(p, furgon).getProbExito(), DELTA);
        assertEquals(48.5, asignacionDe(p, rectorado).getProbExito(), DELTA);
    }

    //Comprueba los beneficios esperados (botin * P / 100) y su suma, 29925.
    @Test
    public void testEnunciadoBeneficiosEsperados() {
        PropuestaAutomatica p = SelectorAutomatico.proponer(misionesEnunciado, secuacesEnunciado, 0);

        assertEquals(18000.0, asignacionDe(p, licoreria).getBeneficioEsperado(), DELTA);
        assertEquals(9500.0, asignacionDe(p, furgon).getBeneficioEsperado(), DELTA);
        assertEquals(2425.0, asignacionDe(p, rectorado).getBeneficioEsperado(), DELTA);
        assertEquals(29925.0, sumaBeneficios(p), DELTA);
    }

    //Comprueba los equipos de las dos misiones de mayor peso: reciben primero los conductores escasos.
    @Test
    public void testEnunciadoEquipoDeLaLicoreriaYElFurgon() {
        PropuestaAutomatica p = SelectorAutomatico.proponer(misionesEnunciado, secuacesEnunciado, 0);

        Map<TRol, List<Secuaz>> eqLicoreria = asignacionDe(p, licoreria).getAsignacion();
        assertEquals(List.of(sec[1]), eqLicoreria.get(TRol.CONDUCTOR));
        assertEquals(List.of(sec[3], sec[4]), eqLicoreria.get(TRol.ASALTO));

        Map<TRol, List<Secuaz>> eqFurgon = asignacionDe(p, furgon).getAsignacion();
        assertEquals(List.of(sec[2]), eqFurgon.get(TRol.CONDUCTOR));
        assertEquals(List.of(sec[6]), eqFurgon.get(TRol.HACKER));
    }

    //Comprueba que el Rectorado se queda sin conductor y ese puesto lo cubre el cerrajero,
    //el sobrante que menos cobra (5 %), y no un asalto (20 %) ni un negociador (10 %).
    @Test
    public void testEnunciadoRellenaElConductorDelRectoradoConElCerrajero() {
        PropuestaAutomatica p = SelectorAutomatico.proponer(misionesEnunciado, secuacesEnunciado, 0);

        Map<TRol, List<Secuaz>> eq = asignacionDe(p, rectorado).getAsignacion();
        assertEquals(List.of(sec[8]), eq.get(TRol.NEGOCIADOR));
        assertEquals(List.of(sec[7]), eq.get(TRol.CONDUCTOR));
    }

    //Comprueba que ningun secuaz esta en dos misiones y que cada equipo tiene exactamente el personal de su plantilla.
    @Test
    public void testEnunciadoNingunSecuazSeRepiteYLosEquiposEstanCompletos() {
        PropuestaAutomatica p = SelectorAutomatico.proponer(misionesEnunciado, secuacesEnunciado, 0);

        Set<Secuaz> usados = new HashSet<>();
        for (AsignacionPropuesta a : p.getAsignaciones()) {
            int enEquipo = 0;
            for (List<Secuaz> puesto : a.getAsignacion().values()) {
                for (Secuaz s : puesto) {
                    assertTrue("Secuaz repetido", usados.add(s));
                    enEquipo++;
                }
            }
            assertEquals(a.getMision().getPersonalTotal(), enEquipo);
        }
    }

    //Comprueba que el resultado del ejemplo es el mismo con cualquier semilla (el optimo es unico y facil de encontrar).
    @Test
    public void testEnunciadoMismoResultadoConCualquierSemilla() {
        for (long semilla = 1; semilla <= 10; semilla++) {
            Azar.setSemilla(semilla);

            PropuestaAutomatica p = SelectorAutomatico.proponer(misionesEnunciado, secuacesEnunciado, 0);

            assertEquals("Semilla " + semilla, 29925.0, sumaBeneficios(p), DELTA);
            assertEquals("Semilla " + semilla, 3, p.getAsignaciones().size());
        }
    }

    // ---------- Greedy de encajes y relleno ----------

    //Comprueba que cuando solo hay un conductor se lo lleva la mision de mayor peso
    //y la otra cubre el puesto con un sobrante (aqui, el unico que queda: el de asalto).
    @Test
    public void testConductorEscasoVaALaMisionDeMayorPeso() {
        Mision alta = mision("Alta", 20000, 10, TRol.CONDUCTOR, 1, TRol.HACKER, 1);
        Mision baja = mision("Baja", 4000, 10, TRol.CONDUCTOR, 1, TRol.HACKER, 1);
        Secuaz conductor = secuaz(1, TRol.CONDUCTOR);
        Secuaz hacker1 = secuaz(2, TRol.HACKER);
        Secuaz hacker2 = secuaz(3, TRol.HACKER);
        Secuaz asalto = secuaz(4, TRol.ASALTO);

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(baja, alta),
                List.of(conductor, hacker1, hacker2, asalto), 0);

        AsignacionPropuesta a = asignacionDe(p, alta);
        AsignacionPropuesta b = asignacionDe(p, baja);
        assertEquals(List.of(conductor), a.getAsignacion().get(TRol.CONDUCTOR));
        assertEquals(List.of(asalto), b.getAsignacion().get(TRol.CONDUCTOR));
        assertEquals(90.0, a.getProbExito(), DELTA);
        assertEquals(45.0, b.getProbExito(), DELTA);
        assertEquals(18000.0, a.getBeneficioEsperado(), DELTA);
        assertEquals(1800.0, b.getBeneficioEsperado(), DELTA);
    }

    //Comprueba que si hay personal para todas y no compiten por nada, se eligen todas.
    @Test
    public void testMisionesCompatiblesSeEligenTodas() {
        Mision m1 = mision("M1", 10000, 0, TRol.CONDUCTOR, 1, TRol.HACKER, 1);
        Mision m2 = mision("M2", 8000, 0, TRol.CONDUCTOR, 1, TRol.ASALTO, 1);
        List<Secuaz> libres = List.of(secuaz(1, TRol.CONDUCTOR), secuaz(2, TRol.CONDUCTOR),
                secuaz(3, TRol.HACKER), secuaz(4, TRol.ASALTO));

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(m1, m2), libres, 0);

        assertEquals(2, p.getAsignaciones().size());
        assertEquals(10000.0, asignacionDe(p, m1).getBeneficioEsperado(), DELTA);
        assertEquals(8000.0, asignacionDe(p, m2).getBeneficioEsperado(), DELTA);
    }

    //Comprueba que si dos misiones no caben a la vez (5 personas para 3 secuaces) se elige la de mayor beneficio
    //y la otra no sale como excluida, porque por si sola si cabria.
    @Test
    public void testSiNoCabenJuntasSeEligeLaDeMayorBeneficio() {
        Mision m1 = mision("M1", 30000, 10, TRol.CONDUCTOR, 1, TRol.ASALTO, 1, TRol.HACKER, 1);
        Mision m2 = mision("M2", 20000, 10, TRol.CONDUCTOR, 1, TRol.ASALTO, 1);
        List<Secuaz> libres = List.of(secuaz(1, TRol.CONDUCTOR), secuaz(2, TRol.ASALTO), secuaz(3, TRol.HACKER));

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(m2, m1), libres, 0);

        assertEquals(1, p.getAsignaciones().size());
        assertNotNull(asignacionDe(p, m1));
        assertEquals(27000.0, asignacionDe(p, m1).getBeneficioEsperado(), DELTA);
        assertTrue(p.getExcluidas().isEmpty());
    }

    //Comprueba que los huecos sin especialista se rellenan con los sobrantes que menos cobran:
    //el cerrajero (5 %) y el primer asalto (20 %); el segundo asalto se queda sin misión.
    @Test
    public void testRellenoUsaLosSobrantesDeMenorComision() {
        Mision m = mision("M1", 9000, 0, TRol.HACKER, 1, TRol.CONDUCTOR, 1, TRol.NEGOCIADOR, 1);
        Secuaz hacker = secuaz(1, TRol.HACKER);
        Secuaz asalto1 = secuaz(2, TRol.ASALTO);
        Secuaz cerrajero = secuaz(3, TRol.CERRAJERO);
        Secuaz asalto2 = secuaz(4, TRol.ASALTO);

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(m),
                List.of(hacker, asalto1, cerrajero, asalto2), 0);

        Map<TRol, List<Secuaz>> eq = asignacionDe(p, m).getAsignacion();
        assertEquals(List.of(hacker), eq.get(TRol.HACKER));
        assertEquals(List.of(cerrajero), eq.get(TRol.CONDUCTOR));
        assertEquals(List.of(asalto1), eq.get(TRol.NEGOCIADOR));
        for (List<Secuaz> puesto : eq.values()) {
            assertFalse(puesto.contains(asalto2));
        }
    }

    //Comprueba que el relleno no cambia la probabilidad de exito: solo cuentan los encajes (1 de 3).
    @Test
    public void testRellenoNoCambiaLaProbabilidadDeExito() {
        Mision m = mision("M1", 9000, 20, TRol.HACKER, 1, TRol.CONDUCTOR, 1, TRol.NEGOCIADOR, 1);
        List<Secuaz> libres = List.of(secuaz(1, TRol.HACKER), secuaz(2, TRol.ASALTO),
                secuaz(3, TRol.CERRAJERO), secuaz(4, TRol.ASALTO));

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(m), libres, 0);

        assertEquals(1.0 / 3 * 80, asignacionDe(p, m).getProbExito(), DELTA);
    }

    // ---------- Material ----------

    //Comprueba que el material que ya esta en stock no cuesta nada.
    @Test
    public void testMaterialEnStockNoCuesta() {
        Mision m = mision("M1", 1000, 0, TRol.CONDUCTOR, 1);
        requiere(m, objeto("Furgoneta", 500, 5), 5);

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(m), List.of(secuaz(1, TRol.CONDUCTOR)), 0);

        assertEquals(1000.0, asignacionDe(p, m).getBeneficioEsperado(), DELTA);
    }

    //Comprueba que el material que falta se paga a precio de catalogo y se resta del beneficio (1000 - 2 * 100).
    @Test
    public void testMaterialQueFaltaSeRestaDelBeneficio() {
        Mision m = mision("M1", 1000, 0, TRol.CONDUCTOR, 1);
        requiere(m, objeto("Ganzua", 100, 0), 2);

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(m), List.of(secuaz(1, TRol.CONDUCTOR)), 0);

        assertEquals(800.0, asignacionDe(p, m).getBeneficioEsperado(), DELTA);
    }

    //Comprueba que si el material cuesta mas de lo que da la mision (2500 contra 1000) no se propone,
    //y que no es una exclusion porque hay personal suficiente.
    @Test
    public void testMisionQueNoCompensaElMaterialNoSePropone() {
        Mision m = mision("M1", 1000, 0, TRol.CONDUCTOR, 1);
        requiere(m, objeto("Explosivo", 500, 0), 5);

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(m), List.of(secuaz(1, TRol.CONDUCTOR)), 0);

        assertTrue(p.getAsignaciones().isEmpty());
        assertTrue(p.getExcluidas().isEmpty());
    }

    //Comprueba que una unidad de stock no se cuenta para dos misiones: la de mayor peso se la lleva
    //y la otra paga su unidad (8000 - 1000).
    @Test
    public void testMaterialCompartidoSoloSeConsumeUnaVez() {
        Objeto llave = objeto("Llave maestra", 1000, 1);
        Mision a = mision("A", 10000, 0, TRol.CONDUCTOR, 1);
        Mision b = mision("B", 8000, 0, TRol.CONDUCTOR, 1);
        requiere(a, llave, 1);
        requiere(b, llave, 1);
        List<Secuaz> libres = List.of(secuaz(1, TRol.CONDUCTOR), secuaz(2, TRol.CONDUCTOR));

        PropuestaAutomatica p = SelectorAutomatico.proponer(List.of(b, a), libres, 0);

        assertEquals(10000.0, asignacionDe(p, a).getBeneficioEsperado(), DELTA);
        assertEquals(7000.0, asignacionDe(p, b).getBeneficioEsperado(), DELTA);
    }

    // ---------- Reproducibilidad y efectos laterales ----------

    //Comprueba que con la misma semilla se obtiene exactamente la misma propuesta.
    @Test
    public void testMismaSemillaDaLaMismaPropuesta() {
        List<List<?>> inst = instanciaAleatoria(5, 9);
        @SuppressWarnings("unchecked") List<Mision> misiones = (List<Mision>) inst.get(0);
        @SuppressWarnings("unchecked") List<Secuaz> secuaces = (List<Secuaz>) inst.get(1);

        Azar.setSemilla(42L);
        String primera = describir(SelectorAutomatico.proponer(misiones, secuaces, 0));
        Azar.setSemilla(42L);
        String segunda = describir(SelectorAutomatico.proponer(misiones, secuaces, 0));

        assertEquals(primera, segunda);
    }

    //Comprueba que el selector no modifica las listas que recibe (ni su orden ni su contenido).
    @Test
    public void testNoModificaLasListasDeEntrada() {
        List<Mision> misiones = new ArrayList<>(Arrays.asList(rectorado, licoreria, bloqueada, furgon));
        List<Secuaz> secuaces = new ArrayList<>(secuacesEnunciado);
        List<Mision> misionesAntes = new ArrayList<>(misiones);
        List<Secuaz> secuacesAntes = new ArrayList<>(secuaces);

        SelectorAutomatico.proponer(misiones, secuaces, 0);

        assertEquals(misionesAntes, misiones);
        assertEquals(secuacesAntes, secuaces);
    }

    // ---------- Propiedades sobre instancias aleatorias ----------

    //Comprueba, en 30 instancias aleatorias de entre 3 y 10 misiones, que toda propuesta es valida:
    //solo usa secuaces libres, ninguno se repite, cada equipo esta completo y todo beneficio es positivo.
    @Test
    public void testPropiedadesDeFactibilidadEnInstanciasAleatorias() {
        for (int semilla = 1; semilla <= 30; semilla++) {
            List<List<?>> inst = instanciaAleatoria(semilla, 3 + semilla % 8);
            @SuppressWarnings("unchecked") List<Mision> misiones = (List<Mision>) inst.get(0);
            @SuppressWarnings("unchecked") List<Secuaz> secuaces = (List<Secuaz>) inst.get(1);
            Azar.setSemilla(semilla);

            PropuestaAutomatica p = SelectorAutomatico.proponer(misiones, secuaces, 0);

            Set<Secuaz> usados = new HashSet<>();
            Set<Mision> propuestas = new HashSet<>();
            for (AsignacionPropuesta a : p.getAsignaciones()) {
                assertTrue("Mision repetida (instancia " + semilla + ")", propuestas.add(a.getMision()));
                assertTrue(a.getBeneficioEsperado() > 0);
                assertTrue(a.getProbExito() >= 0 && a.getProbExito() <= 100);
                int enEquipo = 0;
                for (List<Secuaz> puesto : a.getAsignacion().values()) {
                    for (Secuaz s : puesto) {
                        assertTrue("Secuaz no libre (instancia " + semilla + ")", secuaces.contains(s));
                        assertTrue("Secuaz repetido (instancia " + semilla + ")", usados.add(s));
                        enEquipo++;
                    }
                }
                assertEquals("Equipo incompleto (instancia " + semilla + ")",
                        a.getMision().getPersonalTotal(), enEquipo);
            }
        }
    }

    //Comprueba, en 15 instancias de 8 misiones, que el genetico nunca supera el optimo exhaustivo (seria un error)
    //y que siempre encuentra alguna propuesta con beneficio. Como es estocastico no se le exige el optimo exacto:
    //se imprime la distancia al optimo, que es una de las metricas del estudio de hiperparametros.
    @Test
    public void testNuncaSuperaElOptimoExhaustivoEnInstanciasPequenas() {
        int exactas = 0;
        double peorRatio = 1.0;
        for (int semilla = 1; semilla <= 15; semilla++) {
            List<List<?>> inst = instanciaAleatoria(100 + semilla, 8);
            @SuppressWarnings("unchecked") List<Mision> misiones = (List<Mision>) inst.get(0);
            @SuppressWarnings("unchecked") List<Secuaz> secuaces = (List<Secuaz>) inst.get(1);
            double optimo = optimoExhaustivo(misiones, secuaces);
            Azar.setSemilla(semilla);

            double obtenido = sumaBeneficios(SelectorAutomatico.proponer(misiones, secuaces, 0));

            assertTrue("Instancia " + semilla + ": supera el optimo (" + obtenido + " > " + optimo + ")",
                    obtenido <= optimo + DELTA);
            if (optimo > 0) {
                assertTrue("Instancia " + semilla + ": no propone nada habiendo solucion", obtenido > 0);
                peorRatio = Math.min(peorRatio, obtenido / optimo);
            }
            if (Math.abs(obtenido - optimo) < DELTA) exactas++;
            System.out.printf(Locale.US, "Instancia %d: optimo %.2f, obtenido %.2f (%.1f %%)%n",
                    semilla, optimo, obtenido, optimo > 0 ? 100 * obtenido / optimo : 100.0);
        }
        System.out.println("Optimo exacto en " + exactas + " de 15; peor caso " + Math.round(peorRatio * 1000) / 10.0 + " %");
    }
}