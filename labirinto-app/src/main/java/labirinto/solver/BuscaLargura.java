package labirinto.solver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import labirinto.model.Estado;
import labirinto.model.No;
import labirinto.model.Sala;

/**
 * Busca em Largura (BFS).
 * Garante o caminho mais curto e detecta todos os caminhos ótimos.
 */
public class BuscaLargura implements AlgoritmoBusca {

    private static final int[][] DIRECOES = {
        {-1, 0}, { 1, 0}, { 0,-1}, { 0, 1}
    };
    private static final String[] NOMES_DIRECOES = {
        "Cima", "Baixo", "Esquerda", "Direita"
    };

    private static final int LIMITE_CAMINHOS = 20;

    private final List<String> log = new ArrayList<>();

    @Override
    public String getNome() {
        return "Busca em Largura (BFS)";
    }

    @Override
    public String getNomeCurto() {
        return "BFS";
    }

    @Override
    public boolean garanteCaminhoOtimo() {
        return true;
    }

    @Override
    public ResultadoBusca resolver(Sala sala, Estado inicio, Estado fim) {
        log.clear();

        No raiz = new No(inicio, null);
        Map<Estado, Integer> profundidadeMin = new HashMap<>();
        Deque<No> fila = new ArrayDeque<>();

        profundidadeMin.put(inicio, 0);
        fila.add(raiz);

        int nosVisitados = 0;
        int iteracao = 0;

        List<List<No>> caminhosOtimos = new ArrayList<>();
        int totalCaminhosOtimos = 0;
        int profundidadeOtima = -1;

        log.add("=== INÍCIO DA BUSCA EM LARGURA (BFS) ===");
        log.add("Dimensão do labirinto: " + sala.getDimensao() + "x" + sala.getDimensao());
        log.add("Posição inicial: " + inicio);
        log.add("Posição final:   " + fim);
        log.add("");
        log.add("Fila inicial: [" + inicio + "]");
        log.add("");

        while (!fila.isEmpty()) {
            No atual = fila.poll();
            nosVisitados++;

            int profundidadeAtual = atual.caminhoAteRaiz().size() - 1;

            if (profundidadeOtima >= 0 && profundidadeAtual > profundidadeOtima) {
                log.add("--- Parando: profundidade atual " + profundidadeAtual
                        + " > profundidade ótima " + profundidadeOtima + " ---");
                break;
            }

            iteracao++;
            log.add("--- Iteração " + iteracao + " ---");
            log.add("  Retirado da fila: " + atual.getEstado()
                    + "  (profundidade " + profundidadeAtual + ")");
            log.add("  É o objetivo? " + (atual.getEstado().equals(fim) ? "SIM" : "não"));

            if (atual.getEstado().equals(fim)) {
                totalCaminhosOtimos++;
                if (caminhosOtimos.size() < LIMITE_CAMINHOS) {
                    caminhosOtimos.add(atual.caminhoAteRaiz());
                }
                if (profundidadeOtima < 0) {
                    profundidadeOtima = profundidadeAtual;
                    log.add("  >>> PRIMEIRO caminho ótimo encontrado! "
                            + "Profundidade: " + profundidadeOtima);
                } else {
                    log.add("  >>> Mais um caminho ótimo encontrado! "
                            + "(total parcial: " + totalCaminhosOtimos + ")");
                }
                continue;
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
                int profundidadeVizinho = profundidadeAtual + 1;
                Integer minConhecido = profundidadeMin.get(vizinho);

                if (minConhecido != null && minConhecido < profundidadeVizinho) {
                    sucessoresIgnorados.add(NOMES_DIRECOES[i] + " → (" + nl + "," + nc
                            + ") já visitado em profundidade menor (" + minConhecido + ")");
                    continue;
                }

                if (minConhecido == null || profundidadeVizinho < minConhecido) {
                    profundidadeMin.put(vizinho, profundidadeVizinho);
                }

                No filho = new No(vizinho, atual);
                atual.adicionarFilho(filho);
                fila.add(filho);
                sucessoresAdicionados.add(NOMES_DIRECOES[i] + " → " + vizinho);
            }

            log.add("  Sucessores adicionados à fila:");
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
            log.add("  Fila agora: " + descreverFila(fila));
            log.add("");
        }

        log.add("");
        if (totalCaminhosOtimos == 0) {
            log.add(">>> NENHUM CAMINHO ENCONTRADO <<<");
        } else {
            log.add(">>> BUSCA CONCLUÍDA <<<");
            log.add("Caminhos ótimos encontrados: " + totalCaminhosOtimos);
            if (totalCaminhosOtimos > LIMITE_CAMINHOS) {
                log.add("(Mostrando apenas os primeiros " + LIMITE_CAMINHOS
                        + " por limite de espaço.)");
            }
        }

        List<No> principal = caminhosOtimos.isEmpty() ? null : caminhosOtimos.get(0);

        return new ResultadoBusca(
            totalCaminhosOtimos > 0,
            principal,
            caminhosOtimos,
            totalCaminhosOtimos,
            raiz,
            nosVisitados,
            new ArrayList<>(log)
        );
    }

    private String descreverFila(Deque<No> fila) {
        if (fila.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean primeiro = true;
        for (No n : fila) {
            if (!primeiro) sb.append(", ");
            sb.append(n.getEstado());
            primeiro = false;
        }
        sb.append("]");
        return sb.toString();
    }
}