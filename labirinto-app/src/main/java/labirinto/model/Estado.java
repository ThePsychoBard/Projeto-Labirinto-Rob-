package labirinto.model;

/**
 * Representa uma posição no labirinto (linha, coluna).
 * Imutável — toda operação de mover retorna um novo Estado.
 */
public record Estado(int linha, int coluna) {

    @Override
    public String toString() {
        return "(" + linha + "," + coluna + ")";
    }
}