package labirinto.view;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import labirinto.model.Estado;
import labirinto.model.Sala;
import labirinto.solver.AlgoritmoBusca;

import labirinto.solver.ResultadoBusca;

public class TelaComparacaoController {
    // Mesmo valor de -fx-cell-size do .tabela-comparacao no estilo.css
    private static final double ALTURA_LINHA = 40;
    private Sala sala;
    private Estado inicio;
    private Estado fim;
    private AlgoritmoBusca algoritmoSelecionado;

    @FXML private TableView<LinhaComparacao> tabelaComparacao;
    @FXML private TableColumn<LinhaComparacao, String> colAlgoritmo;
    @FXML private TableColumn<LinhaComparacao, Integer> colVisitados;
    @FXML private TableColumn<LinhaComparacao, Integer> colCaminho;
    @FXML private TableColumn<LinhaComparacao, String> colOtimo;

    // Classe auxiliar para representar uma linha da tabela
    public static class LinhaComparacao {
        private final SimpleStringProperty algoritmo;
        private final SimpleIntegerProperty visitados;
        private final SimpleIntegerProperty caminho;
        private final SimpleStringProperty otimo;


        public LinhaComparacao(String algoritmo, int visitados, int caminho, String otimo) {
            this.algoritmo = new SimpleStringProperty(algoritmo);
            this.visitados = new SimpleIntegerProperty(visitados);
            this.caminho = new SimpleIntegerProperty(caminho);
            this.otimo = new SimpleStringProperty(otimo);
        }

        public SimpleStringProperty algoritmoProperty() { return algoritmo; }
        public SimpleIntegerProperty visitadosProperty() { return visitados; }
        public SimpleIntegerProperty caminhoProperty() { return caminho; }
        public SimpleStringProperty otimoProperty() { return otimo; }
    }

    /** Chamado pelo GerenciadorTelas antes de mostrar a tela. */
            public void configurar(Sala sala, Estado inicio, Estado fim,
                           AlgoritmoBusca algoritmoSelecionado,
                           ResultadoBusca resBfs, ResultadoBusca resDfs, ResultadoBusca resAStar) {
        this.sala = sala;
        this.inicio = inicio;
        this.fim = fim;
        this.algoritmoSelecionado = algoritmoSelecionado;

        colAlgoritmo.setCellValueFactory(cell -> cell.getValue().algoritmoProperty());
        colVisitados.setCellValueFactory(cell -> cell.getValue().visitadosProperty().asObject());
        colCaminho.setCellValueFactory(cell -> cell.getValue().caminhoProperty().asObject());
        colOtimo.setCellValueFactory(cell -> cell.getValue().otimoProperty());

        // Preenche a tabela
        tabelaComparacao.setItems(FXCollections.observableArrayList(
            new LinhaComparacao("BFS (Busca em Largura)", 
                resBfs.getNosVisitados(), 
                resBfs.getTamanhoCaminho(), 
                "Sim"),
            new LinhaComparacao("DFS (Busca em Profundidade)", 
                resDfs.getNosVisitados(), 
                resDfs.getTamanhoCaminho(), 
                "Não"),
            new LinhaComparacao("A* (A-Estrela)", 
                resAStar.getNosVisitados(), 
                resAStar.getTamanhoCaminho(), 
                "Sim")
        ));

        // Sem linhas vazias abaixo dos dados: altura = cabeçalho + linhas + bordas
        tabelaComparacao.setFixedCellSize(ALTURA_LINHA);
        Platform.runLater(this::ajustarAlturaDaTabela);
    }

        /**
     * Mede o cabeçalho e as bordas reais da tabela (já com o CSS aplicado) e
     * fixa a altura para caber exatamente nas linhas de dados.
     */
    private void ajustarAlturaDaTabela() {
        // Força CSS + layout para a tabela já ter cabeçalho medido
        tabelaComparacao.applyCss();
        tabelaComparacao.layout();

        Node cabecalho = tabelaComparacao.lookup(".column-header-background");
        double alturaCabecalho = (cabecalho != null && cabecalho.getLayoutBounds().getHeight() > 0)
            ? cabecalho.getLayoutBounds().getHeight()
            : ALTURA_LINHA;

        Insets margens = tabelaComparacao.getInsets();   // borda + padding
        int linhas = tabelaComparacao.getItems().size();
        double altura = alturaCabecalho
                      + linhas * ALTURA_LINHA
                      + margens.getTop() + margens.getBottom();

        tabelaComparacao.setMinHeight(altura);
        tabelaComparacao.setPrefHeight(altura);
        tabelaComparacao.setMaxHeight(altura);
    }

    @FXML
    private void mostrarAjuda() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Como interpretar");
        a.setHeaderText("Entendendo a tabela");
        a.setContentText(
            "• Nós Visitados:\n" +
            "Quantas células o algoritmo precisou olhar até achar o destino. " +
            "Quanto menor, mais eficiente foi a busca em termos de processamento.\n\n" +
            "• Tamanho do Caminho:\n" +
            "Quantos passos o robô deu do início ao fim. " +
            "Quanto menor, mais curta é a solução encontrada.\n\n" +
            "• Garante o menor caminho?:\n" +
            "Indica se o algoritmo é matematicamente projetado para sempre " +
            "encontrar a rota mais curta possível (BFS e A*), ou se ele apenas " +
            "encontra uma rota qualquer (DFS)."
        );
        a.showAndWait();
    }

        /** Volta à configuração mantendo o cenário que foi comparado. */
    @FXML
    private void voltar() {
        GerenciadorTelas.getInstancia()
            .voltarParaConfiguracao(sala, inicio, fim, algoritmoSelecionado);
    }
}