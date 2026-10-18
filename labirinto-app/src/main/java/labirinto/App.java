package labirinto;

import javafx.application.Application;
import javafx.stage.Stage;
import labirinto.view.GerenciadorTelas;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        GerenciadorTelas.getInstancia().inicializar(stage);
        GerenciadorTelas.getInstancia().trocarTela("TelaInicial");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}