package labirinto.view;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;

import labirinto.model.Estado;
import labirinto.model.No;
import labirinto.model.Sala;
import labirinto.solver.AlgoritmoBusca;
import labirinto.solver.ResultadoBusca;

public class TelaResultadoController {

    private static final Color COR_PAREDE          = Color.web("#0B3D91");
    private static final Color COR_LIVRE           = Color.WHITE;
    private static final Color COR_BORDA           = Color.web("#C7D2E0");
    private static final Color COR_INICIO          = Color.web("#2FBF71");
    private static final Color COR_FIM             = Color.web("#E63946");
    private static final Color COR_CAMINHO         = Color.web("#FFC72C");
    private static final Color COR_CAMINHO_EXTRA   = Color.web("#FFE08A");
    private static final Color COR_ARVORE_VISITADO = Color.web("#8FA8CC");
    private static final Color COR_ARVORE_SOLUCAO  = Color.web("#FFC72C");
    private static final Color COR_ARVORE_TEXTO    = Color.web("#0B3D91");
    private static final Color COR_LINHA_NORMAL    = Color.web("#CCCCCC");

    private static final double ESPACO_HORIZONTAL = 60;
    private static final double ESPACO_VERTICAL   = 65;
    private static final double RAIO_NORMAL       = 16;
    private static final double RAIO_SOLUCAO      = 20;

    @FXML private Canvas canvasLabirinto;
    @FXML private Pane painelArvore;
    @FXML private Label lblEstatisticas;
    @FXML private Label lblAvisoSemSolucao;
    @FXML private javafx.scene.control.ScrollPane scrollArvore;

    private Sala sala;
    private Estado inicio;
    private Estado fim;
    private ResultadoBusca resultado;
    private double zoom = 1.0;
    private Canvas canvasArvoreAtual;
    private AlgoritmoBusca algoritmoUsado;

    private int proximaColuna = 0;

    public void configurar(Sala sala, Estado inicio, Estado fim, ResultadoBusca resultado, AlgoritmoBusca algoritmo) {
        this.sala = sala;
        this.inicio = inicio;
        this.fim = fim;
        this.resultado = resultado;
        this.algoritmoUsado = algoritmo;

        StringBuilder texto = new StringBuilder();
        texto.append("Algoritmo: ").append(algoritmo.getNome())
             .append("   |   Nós visitados: ").append(resultado.getNosVisitados())
             .append("   |   Tamanho do caminho: ").append(resultado.getTamanhoCaminho());
        
        // Só mostra "Caminhos ótimos" se o algoritmo garantir que são ótimos
        if (algoritmo.garanteCaminhoOtimo()) {
            texto.append("   |   Caminhos ótimos: ").append(resultado.getTotalCaminhosOtimos());
        } else {
            texto.append("   |   (DFS não garante caminho mais curto)");
        }

        lblEstatisticas.setText(texto.toString());

        // Aviso de "sem solução"
        if (resultado.isEncontrou()) {
            lblAvisoSemSolucao.setVisible(false);
            lblAvisoSemSolucao.setManaged(false);
        } else {
            lblAvisoSemSolucao.setText("⚠  Não há solução disponível para este cenário.");
            lblAvisoSemSolucao.setVisible(true);
            lblAvisoSemSolucao.setManaged(true);
        }

        desenharLabirinto();
        desenharArvore();
    }

    // ============================================================
    // LABIRINTO
    // ============================================================

    private void desenharLabirinto() {
        int n = sala.getDimensao();
        double lado = Math.min(canvasLabirinto.getWidth(), canvasLabirinto.getHeight());
        double celula = lado / n;

        GraphicsContext g = canvasLabirinto.getGraphicsContext2D();
        g.clearRect(0, 0, canvasLabirinto.getWidth(), canvasLabirinto.getHeight());

        // 1) Fundo
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                g.setFill(sala.isBarreira(i, j) ? COR_PAREDE : COR_LIVRE);
                g.fillRect(j * celula, i * celula, celula, celula);
                g.setStroke(COR_BORDA);
                g.setLineWidth(1);
                g.strokeRect(j * celula, i * celula, celula, celula);
            }
        }

        // 2) Caminhos EXTRA (por baixo, em amarelo claro)
        List<List<No>> caminhos = resultado.getCaminhosOtimos();
        for (int idx = 1; idx < caminhos.size(); idx++) {
            desenharCaminho(g, caminhos.get(idx), celula, COR_CAMINHO_EXTRA, 0.35);
        }

        // 3) Caminho PRINCIPAL (por cima, em amarelo forte)
        if (!caminhos.isEmpty()) {
            desenharCaminho(g, caminhos.get(0), celula, COR_CAMINHO, 0.32);
        }

        // 4) Início e fim
        g.setFill(COR_INICIO);
        g.fillOval(
            inicio.coluna() * celula + celula * 0.15,
            inicio.linha()  * celula + celula * 0.15,
            celula * 0.7, celula * 0.7
        );
        g.setFill(COR_FIM);
        g.fillOval(
            fim.coluna() * celula + celula * 0.15,
            fim.linha()  * celula + celula * 0.15,
            celula * 0.7, celula * 0.7
        );
    }

    private void desenharCaminho(GraphicsContext g, List<No> caminho,
                                 double celula, Color cor, double margemRelativa) {
        g.setFill(cor);
        double margem = celula * margemRelativa;
        for (No no : caminho) {
            Estado e = no.getEstado();
            g.fillOval(
                e.coluna() * celula + margem,
                e.linha()  * celula + margem,
                celula - 2 * margem,
                celula - 2 * margem
            );
        }
    }

    // ============================================================
    // ÁRVORE DE BUSCA
    // ============================================================

    private void desenharArvore() {
    if (resultado.getRaiz() == null) return;

    proximaColuna = 0;
    calcularPosicoes(resultado.getRaiz(), 0);

    double maxX = 0, maxY = 0;
    for (No no : todosOsNos(resultado.getRaiz())) {
        maxX = Math.max(maxX, no.getX() * ESPACO_HORIZONTAL);
        maxY = Math.max(maxY, no.getY() * ESPACO_VERTICAL);
    }

    double largura = maxX + ESPACO_HORIZONTAL * 2;
    double altura  = maxY + ESPACO_VERTICAL * 2;

    Canvas canvas = new Canvas(largura, altura);
    painelArvore.getChildren().setAll(canvas);

    GraphicsContext g = canvas.getGraphicsContext2D();
    g.clearRect(0, 0, largura, altura);
    g.setTextAlign(TextAlignment.CENTER);
    g.setFont(Font.font("Segoe UI", 9));

    List<No> nosDosCaminhos = new ArrayList<>();
    for (List<No> c : resultado.getCaminhosOtimos()) {
        for (No no : c) {
            if (!nosDosCaminhos.contains(no)) nosDosCaminhos.add(no);
        }
    }
    List<No> principal = resultado.getCaminho();

    for (No no : todosOsNos(resultado.getRaiz())) {
        for (No filho : no.getFilhos()) {
            desenharLinha(g, no, filho, nosDosCaminhos, principal);
        }
    }
    for (No no : todosOsNos(resultado.getRaiz())) {
        desenharNo(g, no, nosDosCaminhos, principal);
    }

    // Guarda o canvas pra aplicar zoom depois
    this.canvasArvoreAtual = canvas;
    aplicarZoom();
}

    private void calcularPosicoes(No no, int profundidade) {
        no.setY(profundidade);
        if (no.getFilhos().isEmpty()) {
            no.setX(proximaColuna++);
            return;
        }
        for (No filho : no.getFilhos()) {
            calcularPosicoes(filho, profundidade + 1);
        }
        double soma = 0;
        for (No filho : no.getFilhos()) soma += filho.getX();
        no.setX(soma / no.getFilhos().size());
    }

    private void desenharLinha(GraphicsContext g, No pai, No filho,
                               List<No> nosCaminhos, List<No> principal) {
        double px = pai.getX()   * ESPACO_HORIZONTAL + ESPACO_HORIZONTAL;
        double py = pai.getY()   * ESPACO_VERTICAL   + ESPACO_VERTICAL;
        double fx = filho.getX() * ESPACO_HORIZONTAL + ESPACO_HORIZONTAL;
        double fy = filho.getY() * ESPACO_VERTICAL   + ESPACO_VERTICAL;

        boolean noPrincipal = principal.contains(pai) && principal.contains(filho);
        boolean emAlgum = nosCaminhos.contains(pai) && nosCaminhos.contains(filho);

        if (noPrincipal) {
            g.setStroke(COR_ARVORE_SOLUCAO);
            g.setLineWidth(2.5);
        } else if (emAlgum) {
            g.setStroke(COR_CAMINHO_EXTRA);
            g.setLineWidth(2);
        } else {
            g.setStroke(COR_LINHA_NORMAL);
            g.setLineWidth(1);
        }
        g.strokeLine(px, py, fx, fy);
    }

    private void desenharNo(GraphicsContext g, No no,
                            List<No> nosCaminhos, List<No> principal) {
        boolean noPrincipal = principal.contains(no);
        boolean emAlgum = nosCaminhos.contains(no);

        double raio = noPrincipal ? RAIO_SOLUCAO
                    : (emAlgum ? RAIO_SOLUCAO - 2 : RAIO_NORMAL);

        double x = no.getX() * ESPACO_HORIZONTAL + ESPACO_HORIZONTAL;
        double y = no.getY() * ESPACO_VERTICAL   + ESPACO_VERTICAL;

        Color cor = noPrincipal ? COR_ARVORE_SOLUCAO
                  : (emAlgum ? COR_CAMINHO_EXTRA : COR_ARVORE_VISITADO);
        g.setFill(cor);
        g.fillOval(x - raio, y - raio, raio * 2, raio * 2);

        if (noPrincipal) {
            g.setStroke(COR_PAREDE);
            g.setLineWidth(2);
            g.strokeOval(x - raio, y - raio, raio * 2, raio * 2);
        }

        g.setFill(COR_ARVORE_TEXTO);
        g.fillText(no.getEstado().toString(), x, y + 3);
    }

    private List<No> todosOsNos(No raiz) {
        List<No> lista = new ArrayList<>();
        coletar(raiz, lista);
        return lista;
    }

    private void coletar(No no, List<No> lista) {
        lista.add(no);
        for (No f : no.getFilhos()) coletar(f, lista);
    }

    // ============================================================
    // EXPORTAR LOG
    // ============================================================

    @FXML
    private void exportarLog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar Log da Solução");
        chooser.setInitialFileName("labirinto_solucao.txt");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Arquivo de texto", "*.txt")
        );

        File arquivo = chooser.showSaveDialog(null);
        if (arquivo == null) return;

        try (PrintWriter pw = new PrintWriter(arquivo, StandardCharsets.UTF_8)) {
            pw.print(gerarConteudoLog());
            mostrarSucesso("Log exportado com sucesso:\n" + arquivo.getAbsolutePath());
        } catch (IOException e) {
            mostrarErro("Erro ao salvar arquivo:\n" + e.getMessage());
        }
    }

    private String gerarConteudoLog() {
        StringBuilder sb = new StringBuilder();

        sb.append("==================================================\n");
        sb.append("  LABIRINTO RESOLVIDO — LOG DA SOLUÇÃO\n");
        sb.append("==================================================\n\n");

        sb.append("Dimensão do labirinto: ")
          .append(sala.getDimensao()).append("x").append(sala.getDimensao()).append("\n");
        sb.append("Posição inicial: ").append(inicio).append("\n");
        sb.append("Posição final:   ").append(fim).append("\n\n");

        sb.append("==================================================\n");
        sb.append("  PASSO A PASSO DA BUSCA\n");
        sb.append("==================================================\n\n");
        for (String linha : resultado.getLog()) {
            sb.append(linha).append("\n");
        }
        sb.append("\n");

        sb.append("==================================================\n");
        sb.append("  CAMINHOS ÓTIMOS\n");
        sb.append("==================================================\n\n");

        int total = resultado.getTotalCaminhosOtimos();
        if (total == 0) {
            sb.append("Nenhum caminho foi encontrado entre o início e o fim.\n\n");
        } else if (total == 1) {
            sb.append("Existe 1 caminho ótimo.\n\n");
            sb.append(descreverCaminho(resultado.getCaminhosOtimos().get(0), "Caminho único"));
        } else {
            sb.append("Existem ").append(total).append(" caminhos ótimos com o mesmo comprimento.\n");
            if (total > resultado.getCaminhosOtimos().size()) {
                sb.append("(Mostrando apenas os primeiros ")
                  .append(resultado.getCaminhosOtimos().size())
                  .append(" por limite de espaço.)\n");
            }
            sb.append("\n");
            int idx = 0;
            for (List<No> c : resultado.getCaminhosOtimos()) {
                idx++;
                sb.append(descreverCaminho(c, "Caminho " + idx));
                sb.append("\n");
            }
        }

        sb.append("==================================================\n");
        sb.append("  ESTATÍSTICAS\n");
        sb.append("==================================================\n\n");
        sb.append("Nós visitados:        ").append(resultado.getNosVisitados()).append("\n");
        sb.append("Tamanho do caminho:   ").append(resultado.getTamanhoCaminho()).append("\n");
        sb.append("Caminhos ótimos:      ").append(total).append("\n");
        if (resultado.getNosVisitados() > 0 && resultado.isEncontrou()) {
            double eficiencia = 100.0 * resultado.getTamanhoCaminho() / resultado.getNosVisitados();
            sb.append(String.format("Eficiência:           %.1f%% (caminho / nós visitados)%n", eficiencia));
        }
        sb.append("\n");

        sb.append("==================================================\n");
        sb.append("  Gerado pelo Simulador de Labirinto do Robô\n");
        sb.append("==================================================\n");

        return sb.toString();
    }

    private String descreverCaminho(List<No> caminho, String titulo) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- ").append(titulo)
          .append(" (").append(caminho.size() - 1).append(" passos) ---\n");
        for (int i = 0; i < caminho.size(); i++) {
            sb.append(String.format("  %2d. %s%n", i + 1, caminho.get(i).getEstado()));
        }
        sb.append("  Movimentos: ").append(descreverMovimentos(caminho)).append("\n");
        return sb.toString();
    }

    private String descreverMovimentos(List<No> caminho) {
        if (caminho.size() < 2) return "(nenhum)";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < caminho.size(); i++) {
            Estado a = caminho.get(i - 1).getEstado();
            Estado b = caminho.get(i).getEstado();
            int dl = b.linha()  - a.linha();
            int dc = b.coluna() - a.coluna();
            String mov;
            if (dl == -1 && dc == 0) mov = "Cima";
            else if (dl == 1 && dc == 0) mov = "Baixo";
            else if (dl == 0 && dc == -1) mov = "Esquerda";
            else if (dl == 0 && dc == 1) mov = "Direita";
            else mov = "?";
            sb.append(mov);
            if (i < caminho.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private void mostrarSucesso(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sucesso");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ============================================================
    // AÇÕES
    // ============================================================

    @FXML
    private void zoomIn() {
     zoom = Math.min(3.0, zoom * 1.25);
    aplicarZoom();
    }

   @FXML
   private void zoomOut() {
    zoom = Math.max(0.25, zoom / 1.25);
    aplicarZoom();
   }

   @FXML
   private void zoomReset() {
    zoom = 1.0;
    aplicarZoom();
   }

   private void aplicarZoom() {
    if (canvasArvoreAtual == null) return;
    // Zera o translate que possa ter sobrado do auto-scale
    canvasArvoreAtual.setScaleX(zoom);
    canvasArvoreAtual.setScaleY(zoom);
    canvasArvoreAtual.setTranslateX(0);
    canvasArvoreAtual.setTranslateY(0);
   }

    @FXML
    private void voltar() {
        GerenciadorTelas.getInstancia().trocarTela("TelaInicial");
    }
}