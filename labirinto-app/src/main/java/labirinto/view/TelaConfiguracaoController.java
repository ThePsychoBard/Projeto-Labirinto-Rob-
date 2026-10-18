package labirinto.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import labirinto.model.Estado;
import labirinto.model.Sala;
import labirinto.solver.ResultadoBusca;
import labirinto.solver.Resolvedor;

public class TelaConfiguracaoController {

    private enum Modo { OBSTACULO, INICIO, FIM }

    private static final String COR_LIVRE     = "celula-livre";
    private static final String COR_OBSTACULO = "celula-obstaculo";
    private static final String COR_INICIO    = "celula-inicio";
    private static final String COR_FIM       = "celula-fim";

    @FXML private GridPane grade;
    @FXML private Label lblModo;
    @FXML private Button btnModoObstaculo;
    @FXML private Button btnModoInicio;
    @FXML private Button btnModoFim;

    private Sala sala;
    private int dimensao;
    private Modo modo = Modo.OBSTACULO;
    private Estado inicio;
    private Estado fim;

    private StackPane[][] celulas;

    public void configurar(int dimensao) {
        this.dimensao = dimensao;
        this.sala = new Sala(dimensao);
        this.inicio = null;
        this.fim = null;
        this.modo = Modo.OBSTACULO;
        this.celulas = new StackPane[dimensao][dimensao];

        construirGrade();
        atualizarLabelModo();
    }

    private void construirGrade() {
        grade.getChildren().clear();

        for (int i = 0; i < dimensao; i++) {
            for (int j = 0; j < dimensao; j++) {
                StackPane celula = new StackPane();
                celula.getStyleClass().addAll("celula", COR_LIVRE);

                final int linha = i;
                final int coluna = j;
                celula.setOnMouseClicked(e -> tratarClique(linha, coluna));

                celulas[i][j] = celula;
                grade.add(celula, j, i);
            }
        }
    }

    private void tratarClique(int linha, int coluna) {
        Estado pos = new Estado(linha, coluna);
        switch (modo) {
            case OBSTACULO -> toggleObstaculo(pos);
            case INICIO   -> definirInicio(pos);
            case FIM      -> definirFim(pos);
        }
    }

    private void toggleObstaculo(Estado pos) {
        if (pos.equals(inicio) || pos.equals(fim)) return;
        if (sala.isBarreira(pos.linha(), pos.coluna())) {
            sala.limparBarreira(pos.linha(), pos.coluna());
            trocarEstilo(pos, COR_OBSTACULO, COR_LIVRE);
        } else {
            sala.marcarBarreira(pos.linha(), pos.coluna());
            trocarEstilo(pos, COR_LIVRE, COR_OBSTACULO);
        }
    }

    private void definirInicio(Estado pos) {
        if (sala.isBarreira(pos.linha(), pos.coluna())) return;
        if (pos.equals(fim)) return;
        if (inicio != null) trocarEstilo(inicio, COR_INICIO, COR_LIVRE);
        inicio = pos;
        trocarEstilo(pos, COR_LIVRE, COR_INICIO);
    }

    private void definirFim(Estado pos) {
        if (sala.isBarreira(pos.linha(), pos.coluna())) return;
        if (pos.equals(inicio)) return;
        if (fim != null) trocarEstilo(fim, COR_FIM, COR_LIVRE);
        fim = pos;
        trocarEstilo(pos, COR_LIVRE, COR_FIM);
    }

    private void trocarEstilo(Estado pos, String antigo, String novo) {
        StackPane c = celulas[pos.linha()][pos.coluna()];
        c.getStyleClass().remove(antigo);
        if (!c.getStyleClass().contains(novo)) {
            c.getStyleClass().add(novo);
        }
    }

    private void atualizarLabelModo() {
        String texto = switch (modo) {
            case OBSTACULO -> "Modo: Adicionar Obstáculos";
            case INICIO   -> "Modo: Definir Ponto Inicial";
            case FIM      -> "Modo: Definir Ponto Final";
        };
        lblModo.setText(texto);
    }

    @FXML
    private void selecionarModoObstaculo() {
        modo = Modo.OBSTACULO;
        atualizarLabelModo();
    }

    @FXML
    private void selecionarModoInicio() {
        modo = Modo.INICIO;
        atualizarLabelModo();
    }

    @FXML
    private void selecionarModoFim() {
        modo = Modo.FIM;
        atualizarLabelModo();
    }

    @FXML
    private void limparTudo() {
        sala.limparTudo();
        inicio = null;
        fim = null;

        for (int i = 0; i < dimensao; i++) {
            for (int j = 0; j < dimensao; j++) {
                StackPane c = celulas[i][j];
                c.getStyleClass().removeAll(COR_OBSTACULO, COR_INICIO, COR_FIM);
                if (!c.getStyleClass().contains(COR_LIVRE)) {
                    c.getStyleClass().add(COR_LIVRE);
                }
            }
        }
    }

    @FXML
    private void resolver() {
        if (inicio == null || fim == null) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Atenção");
            a.setHeaderText(null);
            a.setContentText("Defina o ponto inicial e o final antes de resolver.");
            a.showAndWait();
            return;
        }

        Resolvedor r = new Resolvedor();
        ResultadoBusca res = r.resolver(sala, inicio, fim);

        GerenciadorTelas g = GerenciadorTelas.getInstancia();
        g.trocarTela("TelaResultado");
        TelaResultadoController ctrl = (TelaResultadoController) g.getControllerAtual();
        ctrl.configurar(sala, inicio, fim, res);
    }

    @FXML
    private void voltar() {
        GerenciadorTelas.getInstancia().trocarTela("TelaInicial");
    }
}