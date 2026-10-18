module labirinto {
    requires javafx.controls;
    requires javafx.fxml;

    opens labirinto to javafx.fxml;
    opens labirinto.view to javafx.fxml;

    exports labirinto;
    exports labirinto.view;
    exports labirinto.model;
    exports labirinto.solver;
}