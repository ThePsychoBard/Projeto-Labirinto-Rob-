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

    /**
     * Indica se o algoritmo enumera TODOS os caminhos ótimos (só a BFS faz isso).
     * Quando false, o resultado traz um único caminho e a contagem de
     * "caminhos ótimos" não tem significado, então a interface não deve exibi-la.
     */
    default boolean enumeraCaminhosOtimos() {
        return false;
    }

        /** Ordem de complexidade resumida, para exibir na tabela (ex: "O(V)"). */
    String getComplexidade();

    /**
     * Texto "Como interpretar" desta busca, em itens iniciados por "•".
     * Descreve o que é fixo no algoritmo; os números da execução atual
     * são acrescentados pela tela.
     */
    String getExplicacao();

    /** Resolve o labirinto e devolve o resultado. */
    ResultadoBusca resolver(Sala sala, Estado inicio, Estado fim);
}