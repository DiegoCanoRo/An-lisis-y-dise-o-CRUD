module comdiegocano.agendas {
    requires javafx.controls;
    requires javafx.fxml;
        requires java.sql;        
    
    opens comdiegocano.agendas to javafx.fxml;
    exports comdiegocano.agendas;
}
