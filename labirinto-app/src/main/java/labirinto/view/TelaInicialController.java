package labirinto.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import labirinto.model.Estado;
import labirinto.model.Sala;
import labirinto.solver.ResultadoBusca;
import labirinto.solver.Resolvedor;

public class TelaInicialController {

    @FXML
    private void abrirSimulacao() {
        DialogoDimensao.pedir().ifPresent(dimensao -> {
          GerenciadorTelas g = GerenciadorTelas.getInstancia();
          g.trocarTela("TelaConfiguracao");
          TelaConfiguracaoController ctrl = (TelaConfiguracaoController)
            g.getControllerAtual();
        ctrl.configurar(dimensao);
    });
}

    @FXML
    private void mostrarExplicacao() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Como Funciona");
        a.setHeaderText("Simulador de Cenário do Robô");
        a.setContentText(
                "1. Defina o tamanho do labirinto\n" +
                "2. Marque o ponto inicial, o final e os obstáculos\n" +
                "3. O sistema resolve automaticamente via busca em largura\n" +
                "4. Você vê o caminho no labirinto e na árvore de busca.\n" +
                "5. Você pode exportar o log da busca feita pelo algoritmo.");
        a.showAndWait();
    }
}