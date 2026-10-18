package labirinto.view;

import java.util.Optional;

import javafx.scene.control.TextInputDialog;

public class DialogoDimensao {

    /** Abre um diálogo perguntando o tamanho do labirinto. Retorna vazio se cancelar. */
    public static Optional<Integer> pedir() {
        TextInputDialog dialog = new TextInputDialog("8");
        dialog.setTitle("Nova Simulação");
        dialog.setHeaderText("Configurar Labirinto");
        dialog.setContentText("Dimensão (N x N):");

        while (true) {
            Optional<String> resultado = dialog.showAndWait();
            if (resultado.isEmpty()) {
                return Optional.empty();
            }
            try {
                int n = Integer.parseInt(resultado.get().trim());
                if (n < 2 || n > 30) {
                    dialog.setContentText("Dimensão deve estar entre 2 e 30. Tente de novo:");
                    continue;
                }
                return Optional.of(n);
            } catch (NumberFormatException e) {
                dialog.setContentText("Digite um número válido (entre 2 e 30):");
            }
        }
    }
}