package labirinto.solver;

import java.util.Collections;
import java.util.List;

import labirinto.model.No;

/**
 * Resultado de uma busca no labirinto.
 * Guarda o caminho principal, todos os caminhos ótimos encontrados,
 * a árvore, estatísticas e o log passo a passo.
 */
public class ResultadoBusca {

    private final boolean encontrou;
    private final List<No> caminho;                 // caminho principal (o primeiro achado)
    private final List<List<No>> caminhosOtimos;    // todos os caminhos ótimos (inclui o principal)
    private final int totalCaminhosOtimos;          // pode ser maior que caminhosOtimos.size() se passou do limite
    private final No raiz;
    private final int nosVisitados;
    private final int tamanhoCaminho;
    private final List<String> log;

    public ResultadoBusca(boolean encontrou,
                          List<No> caminho,
                          List<List<No>> caminhosOtimos,
                          int totalCaminhosOtimos,
                          No raiz,
                          int nosVisitados,
                          List<String> log) {
        this.encontrou = encontrou;
        this.caminho = caminho == null ? Collections.emptyList() : List.copyOf(caminho);
        this.caminhosOtimos = caminhosOtimos == null
            ? Collections.emptyList()
            : List.copyOf(caminhosOtimos);
        this.totalCaminhosOtimos = totalCaminhosOtimos;
        this.raiz = raiz;
        this.nosVisitados = nosVisitados;
        this.tamanhoCaminho = this.caminho.isEmpty() ? 0 : this.caminho.size() - 1;
        this.log = log == null ? Collections.emptyList() : List.copyOf(log);
    }

    public boolean isEncontrou() { return encontrou; }
    public List<No> getCaminho() { return caminho; }
    public List<List<No>> getCaminhosOtimos() { return caminhosOtimos; }
    public int getTotalCaminhosOtimos() { return totalCaminhosOtimos; }
    public No getRaiz() { return raiz; }
    public int getNosVisitados() { return nosVisitados; }
    public int getTamanhoCaminho() { return tamanhoCaminho; }
    public List<String> getLog() { return log; }
}