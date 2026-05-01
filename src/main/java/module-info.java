module dev.juviscript.compassmeridian {
    requires javafx.controls;
    requires javafx.fxml;


    opens dev.juviscript.compassmeridian to javafx.fxml;
    exports dev.juviscript.compassmeridian;
}