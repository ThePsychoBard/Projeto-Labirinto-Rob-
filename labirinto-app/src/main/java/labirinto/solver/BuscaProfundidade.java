package labirinto.solver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import labirinto.model.Estado;
import labirinto.model.No;
import labirinto.model.Sala;

/**
 * Busca em Profundidade (DFS).
 * Explora o máximo possível em uma direção antes de voltar (backtracking).
 * NÃO garante o caminho mais curto.
 */
public class BuscaProfundidade implements AlgoritmoBusca {

    private static final int[][] DIRECOES = {
        {-1, 0}, { 1, 0}, { 0,-1}, { 0, 1}
    };
    private static final String[] NOMES_DIRECOES = {
        "Cima", "Baixo", "Esquerda", "Direita"
    };

    private final List<String> log = new ArrayList<>();

    @Override
    public String getNome() {
        return "Busca em Profundidade (DFS)";
    }

    @Override
    public String getNomeCurto() {
        return "DFS";
    }

    @Override
    public boolean garanteCaminhoOtimo() {
        return false; // DFS não garante o caminho mais curto
    }

        @Override
    public String getComplexidade() {
        return "O(V)";
    }

    @Override
    public String getExplicacao() {
        return """
            • Como funciona:
            Segue por um caminho até não poder mais avançar e só então volta atrás para tentar outra direção. Usa uma pilha, e a ordem em que as direções são testadas decide qual caminho ela encontra.

            • O que você está vendo:
            Os nós amarelos são o caminho que ela encontrou: o primeiro que chegou ao fim, não necessariamente o mais curto.

            • Atenção:
            Poucos nós visitados não significam um bom caminho. A DFS pode visitar poucas células e mesmo assim devolver uma rota bem mais longa que a ideal. Compare o tamanho do caminho com o da BFS ou do A*.

            • Complexidade:
            O(V): cada célula é visitada no máximo uma vez. (V = número de células)""";
    }

    @Override
    public ResultadoBusca resolver(Sala sala, Estado inicio, Estado fim) {
        log.clear();

        No raiz = new No(inicio, null);
        Set<Estado> visitados = new HashSet<>();
        Deque<No> pilha = new ArrayDeque<>();

        int nosVisitados = 0;
        int iteracao = 0;

        log.add("=== INÍCIO DA BUSCA EM PROFUNDIDADE (DFS) ===");
        log.add("Dimensão do labirinto: " + sala.getDimensao() + "x" + sala.getDimensao());
        log.add("Posição inicial: " + inicio);
        log.add("Posição final:   " + fim);
        log.add("");
        log.add("Pilha inicial: [" + inicio + "]");
        log.add("");

        pilha.push(raiz);

        while (!pilha.isEmpty()) {
            No atual = pilha.pop();
            iteracao++;

            log.add("--- Iteração " + iteracao + " ---");
            log.add("  Desempilhado: " + atual.getEstado());

            // Se já visitamos esse estado, ignora (evita ciclos)
            if (visitados.contains(atual.getEstado())) {
                log.add("  (já visitado anteriormente, ignorando)");
                log.add("");
                continue;
            }

            visitados.add(atual.getEstado());
            nosVisitados++;

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
                    1, // Apenas 1 caminho encontrado
                    raiz,
                    nosVisitados,
                    new ArrayList<>(log)
                );
            }

            // Empilha sucessores (na ordem inversa pra ficar visualmente "Cima, Baixo, Esq, Dir")
            List<String> sucessoresAdicionados = new ArrayList<>();
            List<String> sucessoresIgnorados = new ArrayList<>();

            for (int i = DIRECOES.length - 1; i >= 0; i--) {
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
                
                // Se já visitou, nem empilha
                if (visitados.contains(vizinho)) {
                    sucessoresIgnorados.add(NOMES_DIRECOES[i] + " → (" + nl + "," + nc + ") já visitado");
                    continue;
                }

                No filho = new No(vizinho, atual);
                atual.adicionarFilho(filho);
                pilha.push(filho);
                sucessoresAdicionados.add(NOMES_DIRECOES[i] + " → " + vizinho);
            }

            log.add("  Sucessores empilhados:");
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
            log.add("  Pilha agora (topo à esquerda): " + descreverPilha(pilha));
            log.add("");
        }

        log.add(">>> NENHUM CAMINHO ENCONTRADO <<<");
        log.add("A busca terminou após " + iteracao + " iterações sem alcançar o objetivo.");

        return new ResultadoBusca(false, null, Collections.emptyList(), 0, raiz, nosVisitados, new ArrayList<>(log));
    }

    private String descreverPilha(Deque<No> pilha) {
        if (pilha.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean primeiro = true;
        for (No n : pilha) {
            if (!primeiro) sb.append(", ");
            sb.append(n.getEstado());
            primeiro = false;
        }
        sb.append("]");
        return sb.toString();
    }
}