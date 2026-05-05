module dev.juviscript.compassmeridian {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires com.fazecast.jSerialComm;
    requires java.desktop;

    opens dev.juviscript.compassmeridian to javafx.fxml;
    opens dev.juviscript.compassmeridian.controllers to javafx.fxml;
    exports dev.juviscript.compassmeridian;
    exports dev.juviscript.compassmeridian.controllers;
}