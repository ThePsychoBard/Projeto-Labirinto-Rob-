package labirinto.solver;

import labirinto.model.Estado;
import labirinto.model.Sala;

/**
 * Interface comum a todos os algoritmos de busca.
 * Cada implementação (BFS, DFS, A*) fornece um nome pra exibição
 * e um método que resolve o labirinto.
 */
public interface AlgoritmoBusca {

    /** Nome legível pro usuário (ex: "Busca em Largura (BFS)"). */
    String getNome();

    /** Nome curto (ex: "BFS"). */
    String getNomeCurto();

    /**
     * Indica se o algoritmo garante caminho mais curto.
     * BFS e A* retornam true; DFS retorna false.
     */
    boolean garanteCaminhoOtimo();

    /** Resolve o labirinto e devolve o resultado. */
    ResultadoBusca resolver(Sala sala, Estado inicio, Estado fim);
}