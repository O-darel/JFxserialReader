module org.serial.serial {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.eclipse.paho.client.mqttv3;
    requires com.fazecast.jSerialComm;

    opens org.serial.serial to javafx.fxml;
    exports org.serial.serial;
}