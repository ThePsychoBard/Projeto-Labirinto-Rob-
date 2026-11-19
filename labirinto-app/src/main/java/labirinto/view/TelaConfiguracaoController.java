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
import labirinto.solver.AlgoritmoBusca;
import labirinto.solver.BuscaAEstrela;
import labirinto.solver.BuscaLargura;
import labirinto.solver.ResultadoBusca;
import labirinto.solver.BuscaProfundidade;
import labirinto.solver.BuscaAEstrela;
import javafx.scene.control.ChoiceBox;

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
    @FXML private ChoiceBox<AlgoritmoBusca> cbAlgoritmo;

    private Sala sala;
    private int dimensao;
    private Modo modo = Modo.OBSTACULO;
    private Estado inicio;
    private Estado fim;

    private StackPane[][] celulas;

    /** Cenário novo (vazio) com a dimensão escolhida. */
    public void configurar(int dimensao) {
        prepararTela(new Sala(dimensao));
    }

    /**
     * Reabre a configuração com um cenário já montado (usado pelo "Voltar" das
     * telas de resultado e comparação). A grade volta pintada com os obstáculos,
     * o ponto inicial, o ponto final e o algoritmo que estavam selecionados.
     */
    public void restaurar(Sala salaSalva, Estado inicioSalvo, Estado fimSalvo,
                          AlgoritmoBusca algoritmoSalvo) {
        prepararTela(salaSalva);

        for (int i = 0; i < dimensao; i++) {
            for (int j = 0; j < dimensao; j++) {
                if (sala.isBarreira(i, j)) {
                    trocarEstilo(new Estado(i, j), COR_LIVRE, COR_OBSTACULO);
                }
            }
        }
        if (inicioSalvo != null) {
            inicio = inicioSalvo;
            trocarEstilo(inicio, COR_LIVRE, COR_INICIO);
        }
        if (fimSalvo != null) {
            fim = fimSalvo;
            trocarEstilo(fim, COR_LIVRE, COR_FIM);
        }
        if (algoritmoSalvo != null) {
            // Os itens da ChoiceBox são instâncias novas: compara pelo nome curto
            for (AlgoritmoBusca a : cbAlgoritmo.getItems()) {
                if (a.getNomeCurto().equals(algoritmoSalvo.getNomeCurto())) {
                    cbAlgoritmo.getSelectionModel().select(a);
                    break;
                }
            }
        }
    }

    private void prepararTela(Sala novaSala) {
        this.sala = novaSala;
        this.dimensao = novaSala.getDimensao();
        this.inicio = null;
        this.fim = null;
        this.modo = Modo.OBSTACULO;
        this.celulas = new StackPane[dimensao][dimensao];

        // Popula a ChoiceBox 
        if (cbAlgoritmo.getItems().isEmpty()) {
            // 1. Ensina a ChoiceBox a converter o objeto em texto
            cbAlgoritmo.setConverter(new javafx.util.StringConverter<AlgoritmoBusca>() {
                @Override
                public String toString(AlgoritmoBusca alg) {
                    return alg == null ? "" : alg.getNome();
                }

                @Override
                public AlgoritmoBusca fromString(String string) {
                    return null; 
                }
            });

            // 2. Popula com os algoritmos
            cbAlgoritmo.getItems().addAll(
                new BuscaLargura(),
                new BuscaProfundidade(),
                new BuscaAEstrela()
            );
            cbAlgoritmo.getSelectionModel().selectFirst();
        }

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

        // Pega o algoritmo selecionado pelo usuário
        AlgoritmoBusca algoritmo = cbAlgoritmo.getValue();
        ResultadoBusca res = algoritmo.resolver(sala, inicio, fim);

        GerenciadorTelas g = GerenciadorTelas.getInstancia();
        g.trocarTela("TelaResultado");
        TelaResultadoController ctrl = (TelaResultadoController) g.getControllerAtual();
        
        // Passa o algoritmo para a tela de resultado saber quem resolveu
        ctrl.configurar(sala, inicio, fim, res, algoritmo); 
    }

        @FXML
    private void comparar() {
        if (inicio == null || fim == null) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Atenção");
            a.setHeaderText(null);
            a.setContentText("Defina o ponto inicial e o final antes de comparar.");
            a.showAndWait();
            return;
        }

        // 1. Roda os três algoritmos no mesmo cenário
        ResultadoBusca resBfs = new BuscaLargura().resolver(sala, inicio, fim);
        ResultadoBusca resDfs = new BuscaProfundidade().resolver(sala, inicio, fim);
        ResultadoBusca resAStar = new BuscaAEstrela().resolver(sala, inicio, fim);

        // 2. Abre a tela de comparação e passa os resultados
        GerenciadorTelas g = GerenciadorTelas.getInstancia();
        g.trocarTela("TelaComparacao");
        TelaComparacaoController ctrl = (TelaComparacaoController) g.getControllerAtual();
        ctrl.configurar(sala, inicio, fim, cbAlgoritmo.getValue(),
                       resBfs, resDfs, resAStar);
    }

    @FXML
    private void voltar() {
        GerenciadorTelas.getInstancia().trocarTela("TelaInicial");
    }
}