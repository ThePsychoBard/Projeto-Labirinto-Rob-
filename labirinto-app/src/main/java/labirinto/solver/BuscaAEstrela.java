package labirinto.solver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import labirinto.model.Estado;
import labirinto.model.No;
import labirinto.model.Sala;

/**
 * Algoritmo A* (A-Estrela).
 * Combina o custo real (g) com uma estimativa do futuro (h).
 * Garante o caminho mais curto, mas explora muito menos nós que o BFS.
 */
public class BuscaAEstrela implements AlgoritmoBusca {

    private static final int[][] DIRECOES = {
        {-1, 0}, { 1, 0}, { 0,-1}, { 0, 1}
    };
    private static final String[] NOMES_DIRECOES = {
        "Cima", "Baixo", "Esquerda", "Direita"
    };

    private final List<String> log = new ArrayList<>();

    @Override
    public String getNome() {
        return "A* (A-Estrela)";
    }

    @Override
    public String getNomeCurto() {
        return "A*";
    }

    @Override
    public boolean garanteCaminhoOtimo() {
        return true;
    }

    // Classe auxiliar para a fila de prioridade
    private static class NoAEstrela {
        No no;
        int g; // Custo do início até aqui
        int f; // Custo total (g + h)

        NoAEstrela(No no, int g, int f) {
            this.no = no;
            this.g = g;
            this.f = f;
        }
    }

    @Override
    public ResultadoBusca resolver(Sala sala, Estado inicio, Estado fim) {
        log.clear();

        No raiz = new No(inicio, null);
        
        // Fila de prioridade ordenada pelo menor f
        PriorityQueue<NoAEstrela> abertos = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        
        // Para não revisitar nós pior do que já foram visitados
        Map<Estado, Integer> melhorG = new HashMap<>();
        Set<Estado> fechados = new HashSet<>();

        abertos.add(new NoAEstrela(raiz, 0, Heuristica.manhattan(inicio, fim)));
        melhorG.put(inicio, 0);

        int nosVisitados = 0;
        int iteracao = 0;

        log.add("=== INÍCIO DA BUSCA A* ===");
        log.add("Dimensão do labirinto: " + sala.getDimensao() + "x" + sala.getDimensao());
        log.add("Posição inicial: " + inicio);
        log.add("Posição final:   " + fim);
        log.add("");

        while (!abertos.isEmpty()) {
            NoAEstrela atualA = abertos.poll();
            No atual = atualA.no;

            // Se já foi fechado (visitado), ignora. Isso pode acontecer se 
            // colocamos o mesmo nó várias vezes na fila com custos diferentes.
            if (fechados.contains(atual.getEstado())) {
                continue;
            }

            iteracao++;
            fechados.add(atual.getEstado());
            nosVisitados++;

            int g = atual.caminhoAteRaiz().size() - 1;
            int h = Heuristica.manhattan(atual.getEstado(), fim);
            
            log.add("--- Iteração " + iteracao + " ---");
            log.add("  Retirado da fila: " + atual.getEstado());
            log.add("  f = " + atualA.f + " (g=" + g + ", h=" + h + ")");
            log.add("  É o objetivo? " + (atual.getEstado().equals(fim) ? "SIM" : "não"));

            if (atual.getEstado().equals(fim)) {
                log.add("");
                log.add(">>> OBJETIVO ENCONTRADO na iteração " + iteracao + "! <<<");
                log.add("");
                
                List<No> caminho = atual.caminhoAteRaiz();
                List<List<No>> listaCaminhos = new ArrayList<>();
                listaCaminhos.add(caminho);

                return new ResultadoBusca(
                    true,
                    caminho,
                    listaCaminhos,
                    1,
                    raiz,
                    nosVisitados,
                    new ArrayList<>(log)
                );
            }

            List<String> sucessoresAdicionados = new ArrayList<>();
            List<String> sucessoresIgnorados = new ArrayList<>();

            for (int i = 0; i < DIRECOES.length; i++) {
                int[] d = DIRECOES[i];
                int nl = atual.getEstado().linha()  + d[0];
                int nc = atual.getEstado().coluna() + d[1];

                if (!sala.isDentro(nl, nc)) {
                    sucessoresIgnorados.add(NOMES_DIRECOES[i] + " → fora do tabuleiro");
                    continue;
                }
                if (sala.isBarreira(nl, nc)) {
                    sucessoresIgnorados.add(NOMES_DIRECOES[i] + " → (" + nl + "," + nc + ") é parede");
                    continue;
                }

                Estado vizinho = new Estado(nl, nc);
                
                if (fechados.contains(vizinho)) {
                    sucessoresIgnorados.add(NOMES_DIRECOES[i] + " → (" + nl + "," + nc + ") já fechado");
                    continue;
                }

                int novoG = g + 1;
                int novoH = Heuristica.manhattan(vizinho, fim);
                int novoF = novoG + novoH;

                // Se já temos um caminho melhor (menor g) para esse vizinho, ignoramos
                Integer gExistente = melhorG.get(vizinho);
                if (gExistente != null && novoG >= gExistente) {
                    sucessoresIgnorados.add(NOMES_DIRECOES[i] + " → (" + nl + "," + nc 
                            + ") pior que o caminho já conhecido (g=" + gExistente + ")");
                    continue;
                }

                melhorG.put(vizinho, novoG);
                No filho = new No(vizinho, atual);
                atual.adicionarFilho(filho);
                
                abertos.add(new NoAEstrela(filho, novoG, novoF));
                sucessoresAdicionados.add(NOMES_DIRECOES[i] + " → " + vizinho 
                        + " (f=" + novoF + ": g=" + novoG + ", h=" + novoH + ")");
            }

            log.add("  Sucessores adicionados à fila de prioridade:");
            if (sucessoresAdicionados.isEmpty()) {
                log.add("    (nenhum)");
            } else {
                for (String s : sucessoresAdicionados) log.add("    + " + s);
            }
            log.add("  Sucessores ignorados:");
            if (sucessoresIgnorados.isEmpty()) {
                log.add("    (nenhum)");
            } else {
                for (String s : sucessoresIgnorados) log.add("    - " + s);
            }
            log.add("");
        }

        log.add(">>> NENHUM CAMINHO ENCONTRADO <<<");
        log.add("A busca terminou após " + iteracao + " iterações sem alcançar o objetivo.");

        return new ResultadoBusca(false, null, null, 0, raiz, nosVisitados, new ArrayList<>(log));
    }
}