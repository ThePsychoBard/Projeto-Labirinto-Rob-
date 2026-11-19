package labirinto.view;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import labirinto.model.Estado;
import labirinto.model.Sala;
import labirinto.solver.AlgoritmoBusca;

/**
 * Centraliza a navegação entre telas.
 * Uma única janela (Stage) é reutilizada; só a Scene muda.
 * Cada chamada a trocarTela recarrega o FXML do zero, garantindo
 * que o controller esteja sempre no estado inicial.
 */
public class GerenciadorTelas {

    private static GerenciadorTelas instancia;

    private Stage stage;
    private Object controllerAtual;

    private GerenciadorTelas() {}

    public static GerenciadorTelas getInstancia() {
        if (instancia == null) {
            instancia = new GerenciadorTelas();
        }
        return instancia;
    }

    public void inicializar(Stage stage) {
    this.stage = stage;
    this.stage.setTitle("Labirinto do Robô");
    this.stage.setMaximized(true);
}

/** Troca pra tela cujo .fxml tem o nome passado (sem extensão). */
public void trocarTela(String nomeFxml) {
    try {
        URL url = getClass().getResource("/labirinto/" + nomeFxml + ".fxml");
        if (url == null) {
            throw new IllegalStateException("FXML não encontrado: " + nomeFxml);
        }
        FXMLLoader loader = new FXMLLoader(url);
        Parent raiz = loader.load();
        this.controllerAtual = loader.getController();

        Scene scene = new Scene(raiz);
        scene.getStylesheets().add(
            getClass().getResource("/estilo.css").toExternalForm()
        );
        stage.setScene(scene);

        // Truque pra forçar remaximização (algumas versões do JavaFX ignoram setMaximized sozinho)
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            stage.setMaximized(true);
        } else {
            stage.setMaximized(true);
        }
    } catch (IOException e) {
        throw new RuntimeException("Erro ao carregar tela: " + nomeFxml, e);
    }
}
    /**
     * Volta para a tela de configuração restaurando o cenário que o usuário
     * montou (obstáculos, início, fim e algoritmo escolhido).
     */
    public void voltarParaConfiguracao(Sala sala, Estado inicio, Estado fim,
                                       AlgoritmoBusca algoritmo) {
        trocarTela("TelaConfiguracao");
        TelaConfiguracaoController ctrl = (TelaConfiguracaoController) controllerAtual;
        ctrl.restaurar(sala, inicio, fim, algoritmo);
    }

    public Stage getStage() {
        return stage;
    }

    public Object getControllerAtual() {
        return controllerAtual;
    }
}