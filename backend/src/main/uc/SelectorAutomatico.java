package main.uc;


import java.util.*;

import main.model.*;


public class SelectorAutomatico {

    // Hiperparàmetres (De Jong)
    private static final double PROB_CROSS = 60;
    private static final double PROB_MUTACIO = 1;
    private static final int GENERACIONS = 100;
    private static final int TAMANY_POBLACIO = 50;


    public static PropuestaAutomatica proponer(List<Mision> misiones, List<Secuaz> secuaces, long caja) {

        //fem el random referenciant a la classe Azar per poder saber de quina semilla agafem (ens vindrà bé per l'experimentació)
        Random random = Azar.getInstance();

        //misiones i secuaces que arriben com a parametres son TOTS els que hi ha, no filtra per estat, aixi que s'han de filtrar les dues:
        List<Mision> misionesDisponibles = new ArrayList<>();
        for (Mision m : misiones) {
            if (m.puedeIniciarse()) misionesDisponibles.add(m);
        }
        List<Secuaz> secuacesDisponibles = new ArrayList<>();
        for (Secuaz s : secuaces) {
            if (s.esLibre()) secuacesDisponibles.add(s);
        }

        //fem aixo per crear tots els cromosomes
        List<boolean[]> poblacio = crearPoblacio(TAMANY_POBLACIO, misionesDisponibles.size(), random);

        //ordenem missió per pes decreixent (funció pes ho calcula) (pes es la w = botí * (1/n)(100-k))
        misionesDisponibles.sort((a, b) -> Double.compare(pes(b), pes(a)));

        double[] valors = new double[poblacio.size()]; //on emmagatzemarem el fitness de cada cromosoma

        //paso 3: genetic
        int numMissions = misionesDisponibles.size();
        boolean[] millor = new boolean[numMissions];   // el cromosoma buit (no fer res) val 0
        double valorMillor = 0.0;

        for (int g = 0; g <= GENERACIONS; g++) {
            valors = avaluar(poblacio, misionesDisponibles, secuacesDisponibles, caja);
            //posicio i de valors = fitness del cromosoma que hi ha en la posicio i de poblacio

            for (int i = 0; i < poblacio.size(); i++) {
                if (valors[i] > valorMillor) {
                    valorMillor = valors[i];
                    //faig que "millor" apunti al millor cromosoma que tenim en aquell moment
                    millor = poblacio.get(i).clone();
                    //es posa el .clone() perque si no el poses, si aquell element mutés o s'alterés en una
                    //generació posterior, millor també canviaria perque estaria apuntant encara a aquell element
                    //(el qual ja no te perque ser el millor)
                }
            }

            if (g < GENERACIONS) {
                //mentre no hagi arribat al final segueixo barrejant pares i creant fills
                poblacio = novaGeneracio(poblacio, valors, random);
            }
        }

        //missions excloses (necessiten mes sequaços dels que hi ha lliures)
        List<String> excluidas = new ArrayList<>();
        for (Mision mis : misionesDisponibles) {
            if (mis.getPersonalTotal() > secuacesDisponibles.size()) {
                excluidas.add(mis.getNombreClave() + ": necesita " + mis.getPersonalTotal()
                        + " secuaces y solo hay " + secuacesDisponibles.size() + " libres");
            }
        }

        //en el cas que tots els fitness siguin 0, vol dir que totes les missions estan excloses
        if (valorMillor <= 0) {
            return new PropuestaAutomatica(new ArrayList<>(), excluidas);
        }

        //ja tenim el millor cromosoma, ara construim la proposta per fer el return:
        List<Mision> escollides = new ArrayList<>();
        for (int i = 0; i < numMissions; i++) {
            if (millor[i]) escollides.add(misionesDisponibles.get(i));
        }

        Map<Mision, Map<TRol, List<Secuaz>>> equips = assignarEquips(escollides, secuacesDisponibles);
        Map<Mision, Long> costes = costesMaterial(escollides);

        List<AsignacionPropuesta> asignaciones = new ArrayList<>();
        for (Mision mision : escollides) {
            Map<TRol, List<Secuaz>> equip = equips.get(mision);
            double probExit = Ejecucion.calcularProbabilidadExito(mision, equip);
            double benefici = mision.getBotin() * probExit / 100.0 - costes.get(mision);
            if (benefici > 0) { //filtrem, es ridicul afegir a proposta si no ens dona benefici
                asignaciones.add(new AsignacionPropuesta(mision, equip, probExit, benefici));
            }
        }

        return new PropuestaAutomatica(asignaciones, excluidas);

    }


    private static boolean[] crearCromosoma(int m, Random random) {
        boolean[] cromo = new boolean[m];
        for (int i = 0; i < m; ++i) {
            cromo[i] = random.nextBoolean();
        }
        return cromo;
    }

    private static List<boolean[]> crearPoblacio(int tamanyPoblacio, int m, Random random) {
        List<boolean[]> poblacio = new ArrayList<>();
        for (int i = 0; i < tamanyPoblacio; ++i) {
            boolean[] cromosoma = crearCromosoma(m, random);
            poblacio.add(cromosoma);
        }
        return poblacio;
    }

    private static double fitness(boolean[] cromosoma, List<Mision> misiones, List<Secuaz> secuaces) {
        int numSecuacesNecesarios = 0;
        int numSecuacesDisponibles = secuaces.size();
        double fitnessTotal = 0.0;

        //faig un map amb el contador de sequaços separats per rol per anar-lo actualitzant a mesura que els assigno a missions
        Map<TRol, Integer> secuacesPorRol = contarDisponibles(secuaces);

        List<Mision> escollides = new ArrayList<>();


        for (int i = 0; i < misiones.size(); i++) {
            if (cromosoma[i]) {
                escollides.add(misiones.get(i));
                numSecuacesNecesarios += misiones.get(i).getPersonalTotal();
            }
            //cas base, si es necessiten mes sequaços dels que es tenen fitness = 0
            if (numSecuacesNecesarios > numSecuacesDisponibles) {
                return 0.0;
            }
        }

        Map<Mision, Integer> encaixosMissio = calcularEncajes(escollides, secuacesPorRol);

        for (Mision esc : escollides) {

            int m = encaixosMissio.get(esc);
            fitnessTotal += pes(esc) * m;

        }
        return fitnessTotal;
    }

    //el 100 del numerador es perque probabilitat fracas en % i no entre 0 i 1
    private static double pes(Mision m) {
        return m.getBotin() * (100 - m.getProbFracaso()) / (100.0 * m.getPersonalTotal());
    }


    private static Map<Mision, Integer> calcularEncajes(List<Mision> escollides, Map<TRol, Integer> secuacesPorRol) {
        Map<Mision, Integer> resultado = new HashMap<>();

        for (Mision mision : escollides) {
            int totalEncajes = 0;

            for (PlantillaRol pr : mision.getPlantillasRol()) { //recorro tota la plantilla necessaria per la missio
                int secDisponiblesRol = secuacesPorRol.getOrDefault(pr.getRol(), 0); //miro el contador de sequaços disponibles per aquell rol
                int encajesLocal = Math.min(secDisponiblesRol, pr.getCantidad()); //amb el min m'asseguro: si tinc 1 i necessito 2, agafo 1. Si tinc 2 i necessito 1, agafo 1.
                secuacesPorRol.put(pr.getRol(), secDisponiblesRol - encajesLocal); //actualitzo el contador del rol (resto tants com hagi assignat)
                totalEncajes += encajesLocal; //aixo es per cada rol, pero necessito el total d'encaixos de TOTS els rols

            }

            resultado.put(mision, totalEncajes);
        }
        return resultado;
    }

    //per omplir el mapa creat de comptador de sequaços separats per rol
    private static Map<TRol, Integer> contarDisponibles(List<Secuaz> libres) {
        Map<TRol, Integer> secuacesPorRol = new HashMap<>();
        for (Secuaz s : libres) {
            TRol rol = s.getEspecialidad();
            secuacesPorRol.put(rol, secuacesPorRol.getOrDefault(rol, 0) + 1);
        }
        return secuacesPorRol;
    }


    private static boolean[] torneig(List<boolean[]> poblacio, double[] valors, Random random) {
        int k = 3; //PARAMETRE QUE PODEM CANVIAR DESPRES DE REALITZAR ESTUDI

        int millor = random.nextInt(poblacio.size());
        for (int i = 1; i < k; i++) {
            int candidat = random.nextInt(poblacio.size());
            if (valors[candidat] > valors[millor]) {
                millor = candidat;
            }
        }
        return poblacio.get(millor);
    }

    //pel genètic, ho fem servir per omplir matriu "valors" amb tots els fitness, tenint en compte el material i tot
    private static double[] avaluar(List<boolean[]> poblacio, List<Mision> misiones, List<Secuaz> secuaces, long caja) {
        double[] valors = new double[poblacio.size()];
        for (int i = 0; i < poblacio.size(); i++) {
            valors[i] = fitnessAmbMaterial(poblacio.get(i), misiones, secuaces, caja);
        }
        return valors;
    }

    private static boolean[] crossover(boolean[] pare1, boolean[] pare2, Random random) {
        int m = pare1.length;
        //creo un punt de tall aleatori per separar pare1 i pare2:
        int pivot = (m == 0) ? 0 : random.nextInt(m);
        boolean[] fill = new boolean[m];
        for (int i = 0; i < m; i++) {
            //si la posicio que estic consultant està abans del punt de tall, agafo del pare1, sino agafo del pare2
            fill[i] = (i < pivot) ? pare1[i] : pare2[i];
        }
        return fill;
    }

    private static void mutar(boolean[] cromosoma, Random random) {
        for (int i = 0; i < cromosoma.length; i++) {
            if (random.nextDouble() * 100 < PROB_MUTACIO) {
                cromosoma[i] = !cromosoma[i];
            }
        }
    }

    private static List<boolean[]> novaGeneracio(List<boolean[]> poblacio, double[] valors, Random random) {
        List<boolean[]> nova = new ArrayList<>();
        while (nova.size() < poblacio.size()) {
            boolean[] pare1 = torneig(poblacio, valors, random);
            boolean[] pare2 = torneig(poblacio, valors, random);

            boolean[] fill;
            if (random.nextDouble() * 100 < PROB_CROSS) {
                fill = crossover(pare1, pare2, random);
            } else {
                fill = pare1.clone();
            }
            mutar(fill, random);
            nova.add(fill);
        }
        return nova;
    }

    // crida a la funció de fitness feta anteriroment i resta el cost del material
    private static double fitnessAmbMaterial(boolean[] cromosoma, List<Mision> misiones,
                                             List<Secuaz> secuaces, long caja) {
        double f = fitness(cromosoma, misiones, secuaces);
        if (f <= 0) return 0.0;     // impossible o sense benefici

        List<Mision> escollides = new ArrayList<>();
        for (int i = 0; i < misiones.size(); i++) {
            if (cromosoma[i]) escollides.add(misiones.get(i));
        }

        long costeTotal = 0;
        for (long cost : costesMaterial(escollides).values()) {
            costeTotal += cost;
        }


        return Math.max(0.0, f - costeTotal);
    }

    // cost del material que falta de cada missio, repartint l'stock en l'ordre de les escollides
    private static Map<Mision, Long> costesMaterial(List<Mision> escollides) {
        Map<Objeto, Integer> stockRestant = new HashMap<>();
        Map<Mision, Long> costes = new HashMap<>();

        for (Mision mision : escollides) {
            long cost = 0;
            for (RequisitoMaterial r : mision.getRequisitosMateriales()) {
                Objeto o = r.getObjeto();
                int disponible = stockRestant.getOrDefault(o, o.getStock());
                int usades = Math.min(disponible, r.getCantidad());
                int falten = r.getCantidad() - usades;
                cost += (long) falten * o.getPrecio();
                stockRestant.put(o, disponible - usades);
            }
            costes.put(mision, cost);
        }
        return costes;
    }

    //part d'assignacio (nomes ho fem amb el millor cromosoma)
    private static Map<Mision, Map<TRol, List<Secuaz>>> assignarEquips(List<Mision> escollides, List<Secuaz> lliures) {
        List<Secuaz> copia = new ArrayList<>(lliures);   //fem una copia per anar treient secuaços
        Map<Mision, Map<TRol, List<Secuaz>>> equips = new LinkedHashMap<>();

        // primera passada: els encaixos (cada secuaz al puesto de la seva especialitat)
        for (Mision mision : escollides) {
            Map<TRol, List<Secuaz>> equip = new HashMap<>(); //creo un mapa que determinara QUI ocupara la posició de cada rol
            for (PlantillaRol pr : mision.getPlantillasRol()) { //recorro tota la plantilla que necessitare a la missio i assigno
                List<Secuaz> assignats = new ArrayList<>();
                Iterator<Secuaz> it = copia.iterator();
                while (it.hasNext() && assignats.size() < pr.getCantidad()) { //recorro tots els sequaços que tinc lliures
                    //fent referencia al iterador per poder eliminar els ja assignats
                    Secuaz s = it.next();
                    if (s.getEspecialidad() == pr.getRol()) {
                        assignats.add(s);
                        it.remove();
                    }
                }
                equip.put(pr.getRol(), assignats);
            }
            equips.put(mision, equip);
        }

        // els que sobren, de menor a major salari
        copia.sort((a, b) -> Double.compare(a.getEspecialidad().getComision(), b.getEspecialidad().getComision()));

        // un cop ordenats simplement omplim de dalt a baix
        for (Mision mision : escollides) {
            for (PlantillaRol pr : mision.getPlantillasRol()) {
                List<Secuaz> assignats = equips.get(mision).get(pr.getRol());
                while (assignats.size() < pr.getCantidad()) {
                    assignats.add(copia.remove(0));
                }
            }
        }
        return equips;
    }




}

