package labirinto.solver;

import labirinto.model.Estado;

public class Heuristica {
    
    /**
     * Distância de Manhattan: soma da diferença de linha e coluna.
     * É a heurística clássica para labirintos (só permite movimento ortogonal).
     */
    public static int manhattan(Estado atual, Estado objetivo) {
        return Math.abs(atual.linha() - objetivo.linha()) +
               Math.abs(atual.coluna() - objetivo.coluna());
    }
}