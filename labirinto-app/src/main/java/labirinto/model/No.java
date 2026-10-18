package labirinto.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nó da árvore de busca.
 * - Guarda o estado (posição no labirinto).
 * - Aponta pro pai (pra reconstruir o caminho).
 * - Guarda os filhos (pra desenhar a árvore).
 * - Também guarda coordenadas (x, y) usadas SÓ pra posicionamento visual
 *   da árvore na tela de resultado. Não afetam a busca.
 */
public class No {

    private final Estado estado;
    private final No pai;
    private final List<No> filhos = new ArrayList<>();

    // Coordenadas usadas apenas pelo layout visual da árvore
    private double x;
    private double y;

    public No(Estado estado, No pai) {
        this.estado = estado;
        this.pai = pai;
    }

    public Estado getEstado() {
        return estado;
    }

    public No getPai() {
        return pai;
    }

    public List<No> getFilhos() {
        return Collections.unmodifiableList(filhos);
    }

    public void adicionarFilho(No filho) {
        filhos.add(filho);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /** Lista do nó raiz até este nó. */
    public List<No> caminhoAteRaiz() {
        List<No> caminho = new ArrayList<>();
        No atual = this;
        while (atual != null) {
            caminho.add(atual);
            atual = atual.pai;
        }
        Collections.reverse(caminho);
        return caminho;
    }

    @Override
    public String toString() {
        return estado.toString();
    }
}