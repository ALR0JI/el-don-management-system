package main.uc;

import main.model.Mision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Planificador {

    //se aplica el algoritmo de Khant para devolver una secuencia valida de ejeucion que devuelva 
    //el mayor numero de botin posible 
    public List<Mision> planificarGolpes(List<Mision> misiones) {
        if (misiones == null) {
            throw new IllegalArgumentException("La lista de misiones no puede ser nula.");
        }

        Map<Mision, Integer> gradoEntrada = new HashMap<>();
        Map<Mision, List<Mision>> adjacencia = new HashMap<>();

        //inicializar gradoEntrada y adjacencia para todas las misiones
        for (Mision m : misiones) {
            gradoEntrada.put(m, 0);
            adjacencia.put(m, new ArrayList<>());
        }

        //construir el grafo de dependencias y calcular grados de entrada
        for (Mision m : misiones) {
            for (Mision pre : m.getPrerrequisitos()) {
                if (gradoEntrada.containsKey(pre)) {
                    adjacencia.get(pre).add(m);
                    gradoEntrada.put(m, gradoEntrada.get(m)+1);
                }
            }
        }

        //priority queue que ordena las misiones sin dependencias de mayor a menor botin
        PriorityQueue<Mision> libres = new PriorityQueue<>(
            (m1, m2) -> Long.compare(m2.getBotin(), m1.getBotin()) //le dice a la pq como debe ordenar
        );

        for (Map.Entry<Mision, Integer> entry : gradoEntrada.entrySet()) {
            if (entry.getValue() == 0) {
                libres.add(entry.getKey());
            }
        }

        List<Mision> planOrdenado = new ArrayList<>();

        //añade las misiones por prioridad a planOrdenado y recalcula 
        //el gradEntrada eliminando la mision añadida 
        while (!libres.isEmpty()) {
            Mision actual = libres.poll();
            planOrdenado.add(actual);

            for (Mision dependiente : adjacencia.get(actual)) {
                int nuevoGrado = gradoEntrada.get(dependiente) - 1;
                gradoEntrada.put(dependiente, nuevoGrado);
                
                if (nuevoGrado == 0) {
                    libres.add(dependiente);
                }
            }
        }

        if (planOrdenado.size() != misiones.size()) {
            throw new IllegalStateException("No se puede generar el plan ordenado porque existe una dependencia circular.");
        }

        return Collections.unmodifiableList(planOrdenado);
    }
}