plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "2.0.0"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.1.3"
}

group = "org.serial"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("org.serial.serial")
    mainClass.set("org.serial.serial.SerialApp")
}


javafx {
    version = "17"
    modules = listOf("javafx.controls",
        "javafx.fxml","javafx.web","javafx.swing",
        "javafx.media")
}

dependencies {
    // ControlsFX
    implementation("org.controlsfx:controlsfx:11.2.1")

    // FormsFX
    implementation("com.dlsc.formsfx:formsfx-core:11.6.0") {
        exclude(group = "org.openjfx")
    }

    // ValidatorFX
    implementation("net.synedra:validatorfx:0.5.0") {
        exclude(group = "org.openjfx")
    }

    // Ikonli Icons (Material Design 2)
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-materialdesign2-pack:12.3.1")

    // BootstrapFX
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")

    // TilesFX
    implementation("eu.hansolo:tilesfx:17.1.31") {
        exclude(group = "org.openjfx")
    }

    // jSerialComm for Serial Port Communication
    implementation("com.fazecast:jSerialComm:2.10.4")

    // Eclipse Paho MQTT Client
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    // JUnit for Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

jlink {

    imageZip.set(layout.buildDirectory.file("distributions/serial-mqtt-bridge-${javafx.platform.classifier}.zip"))
    options.set(listOf(
        "--strip-debug",
        "--compress", "2",
        "--no-header-files",
        "--no-man-pages"
    ))
    launcher {
        name = "serial-mqtt-bridge"

    }

    //forceMerge("ALL-MODULES")

    mergedModule {
        // Add the service used by paho so the merged automatic-module can provide it
        uses("org.eclipse.paho.client.mqttv3.internal.NetworkModuleService")
    }

    //for better compatibility(but will need wix)
    jpackage {
        installerType = "msi"
        imageName = "Serial MQTT Bridge"
        installerName = "Serial-MQTT-Bridge"
        //appVersion = version.toString()
        appVersion = version.toString().replace(Regex("[^0-9.]"), "")
        installerOptions = listOf(
            "--vendor", "Company",
            "--description", "Serial to MQTT Bridge Application",
            "--win-menu",
            "--win-shortcut",
            "--win-dir-chooser",
            "--win-menu-group", "Weighing System",
            "--icon", "src/main/resources/static/img/serial.ico"

        )
    }
}
