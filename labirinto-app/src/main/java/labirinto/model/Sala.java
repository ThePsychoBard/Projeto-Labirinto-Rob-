package labirinto.model;

/**
 * O labirinto em si: uma grade N x N onde cada célula é livre ou barreira.
 */
public class Sala {

    private final boolean[][] celulas;
    private final int dimensao;

    public Sala(int dimensao) {
        if (dimensao <= 0) {
            throw new IllegalArgumentException("Dimensão deve ser > 0");
        }
        this.dimensao = dimensao;
        this.celulas = new boolean[dimensao][dimensao]; // false = livre
    }

    public int getDimensao() {
        return dimensao;
    }

    public boolean isDentro(int linha, int coluna) {
        return linha >= 0 && linha < dimensao
            && coluna >= 0 && coluna < dimensao;
    }

    public boolean isBarreira(int linha, int coluna) {
        if (!isDentro(linha, coluna)) {
            return true; // fora do tabuleiro conta como barreira
        }
        return celulas[linha][coluna];
    }

    public void marcarBarreira(int linha, int coluna) {
        if (!isDentro(linha, coluna)) {
            throw new IllegalArgumentException(
                "Posição fora do tabuleiro: (" + linha + "," + coluna + ")");
        }
        celulas[linha][coluna] = true;
    }

    public void limparBarreira(int linha, int coluna) {
        if (isDentro(linha, coluna)) {
            celulas[linha][coluna] = false;
        }
    }

    public void limparTudo() {
        for (int i = 0; i < dimensao; i++) {
            for (int j = 0; j < dimensao; j++) {
                celulas[i][j] = false;
            }
        }
    }
}