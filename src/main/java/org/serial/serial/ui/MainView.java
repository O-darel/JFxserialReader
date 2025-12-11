package org.serial.serial.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.*;
import org.serial.serial.service.SerialService;
import org.serial.serial.service.MqttService;
import org.serial.serial.util.LogManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainView {
    private BorderPane root;
    private ComboBox<String> portComboBox;
    private TextField brokerField;
    private TextField topicField;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button connectButton;
    private Button startButton;
    private Label statusLabel;
    private Label connectionStatusLabel;
    private TextArea dataTextArea;
    private TextArea logTextArea;
    private Label startTimeLabel;
    private Label stopTimeLabel;
    private Label messagesCountLabel;

    private SerialService serialService;
    private MqttService mqttService;
    private LogManager logManager;

    private boolean isRunning = false;
    private int messageCount = 0;
    private LocalDateTime sessionStartTime;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MainView() {
        logManager = LogManager.getInstance();
        serialService = new SerialService();
        mqttService = new MqttService();

        initializeUI();
        setupEventHandlers();
        refreshPortList();
    }

    private void initializeUI() {
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // Top bar with title and status
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Left sidebar with controls
        VBox sidebar = createSidebar();
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.getStyleClass().add("sidebar-scroll");
        root.setLeft(sidebarScroll);
        //root.setLeft(sidebar);

        // Center content with data display and logs
        //VBox centerContent = createCenterContent();
        ScrollPane centerContent = createCenterContent();
        root.setCenter(centerContent);

        // Bottom status bar
        HBox bottomBar = createBottomBar();
        root.setBottom(bottomBar);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));

        FontIcon appIcon = new FontIcon(MaterialDesignC.CHART_LINE);
        appIcon.setIconSize(32);
        appIcon.setIconColor(Color.web("#2196F3"));

        Label titleLabel = new Label("Serial to MQTT Bridge");
        titleLabel.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        connectionStatusLabel = new Label("Disconnected");
        connectionStatusLabel.getStyleClass().add("status-disconnected");
        FontIcon statusIcon = new FontIcon(MaterialDesignC.CIRCLE);
        statusIcon.setIconSize(12);
        connectionStatusLabel.setGraphic(statusIcon);

        topBar.getChildren().addAll(appIcon, titleLabel, spacer, connectionStatusLabel);
        return topBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(350);

        // Serial Port Section
        VBox serialSection = createSerialSection();

        // MQTT Configuration Section
        VBox mqttSection = createMqttSection();

        // Control Section
        VBox controlSection = createControlSection();

        // Session Info Section
        VBox sessionSection = createSessionSection();

        sidebar.getChildren().addAll(serialSection, new Separator(),
                mqttSection, new Separator(),
                controlSection, sessionSection);

        return sidebar;
    }

    private VBox createSerialSection() {
        VBox section = new VBox(10);

        Label sectionLabel = new Label("Serial Port Configuration");
        sectionLabel.getStyleClass().add("section-title");

        HBox portBox = new HBox(10);
        portBox.setAlignment(Pos.CENTER_LEFT);

        portComboBox = new ComboBox<>();
        portComboBox.setPromptText("Select COM Port");
        portComboBox.setPrefWidth(200);

        Button refreshButton = new Button();
        FontIcon refreshIcon = new FontIcon(MaterialDesignR.REFRESH);
        refreshIcon.setIconSize(18);
        refreshButton.setGraphic(refreshIcon);
        refreshButton.getStyleClass().add("icon-button");
        refreshButton.setOnAction(e -> refreshPortList());

        portBox.getChildren().addAll(new Label("Port:"), portComboBox, refreshButton);

        section.getChildren().addAll(sectionLabel, portBox);
        return section;
    }

    private VBox createMqttSection() {
        VBox section = new VBox(10);

        Label sectionLabel = new Label("MQTT Broker Configuration");
        sectionLabel.getStyleClass().add("section-title");

        brokerField = new TextField("tcp://18.219.121.50:1883");
        brokerField.setPromptText("Broker URL");

        topicField = new TextField("sensor/weight");
        topicField.setPromptText("Topic");

//        usernameField = new TextField("bahati");
//        usernameField.setPromptText("Username");
        usernameField = new PasswordField();
        usernameField.setText("bahati");  // optional default
        usernameField.setPromptText("Username");

        passwordField = new PasswordField();
        passwordField.setText("We1ght@RDr");
        passwordField.setPromptText("Password");

        connectButton = new Button("Connect to Broker");
        FontIcon connectIcon = new FontIcon(MaterialDesignL.LAN_CONNECT);
        connectButton.setGraphic(connectIcon);
        connectButton.getStyleClass().add("primary-button");
        connectButton.setMaxWidth(Double.MAX_VALUE);

        section.getChildren().addAll(
                sectionLabel,
                new Label("Broker:"), brokerField,
                new Label("Topic:"), topicField,
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                connectButton
        );

        return section;
    }

    private VBox createControlSection() {
        VBox section = new VBox(10);

        startButton = new Button("Start Monitoring");
        FontIcon playIcon = new FontIcon(MaterialDesignP.PLAY);
        startButton.setGraphic(playIcon);
        startButton.getStyleClass().add("success-button");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setDisable(true);

        section.getChildren().addAll(startButton);
        return section;
    }

    private VBox createSessionSection() {
        VBox section = new VBox(8);
        section.getStyleClass().add("info-box");
        section.setPadding(new Insets(15));

        Label sessionLabel = new Label("Session Information");
        sessionLabel.getStyleClass().add("section-title");

        startTimeLabel = new Label("Start: N/A");
        stopTimeLabel = new Label("Stop: N/A");
        messagesCountLabel = new Label("Messages: 0");

        startTimeLabel.getStyleClass().add("info-label");
        stopTimeLabel.getStyleClass().add("info-label");
        messagesCountLabel.getStyleClass().add("info-label");

        section.getChildren().addAll(sessionLabel, startTimeLabel,
                stopTimeLabel, messagesCountLabel);
        return section;
    }

    private ScrollPane createCenterContent() {
        VBox centerContent = new VBox(15);
        centerContent.setPadding(new Insets(20));

        // Data Display Section
        VBox dataSection = new VBox(10);
        Label dataLabel = new Label("Received Data");
        dataLabel.getStyleClass().add("section-title");

        dataTextArea = new TextArea();
        dataTextArea.setEditable(false);
        dataTextArea.setPromptText("Serial data will appear here...");
        dataTextArea.setPrefHeight(250);
        dataTextArea.getStyleClass().add("data-area");

        Button clearDataButton = new Button("Clear");
        FontIcon clearIcon = new FontIcon(MaterialDesignD.DELETE_SWEEP);
        clearDataButton.setGraphic(clearIcon);
        clearDataButton.setOnAction(e -> dataTextArea.clear());

        HBox dataHeader = new HBox(10);
        dataHeader.setAlignment(Pos.CENTER_LEFT);
        Region dataSpacer = new Region();
        HBox.setHgrow(dataSpacer, Priority.ALWAYS);
        dataHeader.getChildren().addAll(dataLabel, dataSpacer, clearDataButton);

        dataSection.getChildren().addAll(dataHeader, dataTextArea);
        VBox.setVgrow(dataTextArea, Priority.ALWAYS);

        // Log Section
        VBox logSection = new VBox(10);
        Label logLabel = new Label("Application Logs");
        logLabel.getStyleClass().add("section-title");

        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setPromptText("Application logs will appear here...");
        logTextArea.setPrefHeight(200);
        logTextArea.getStyleClass().add("log-area");

        Button clearLogButton = new Button("Clear");
        FontIcon clearLogIcon = new FontIcon(MaterialDesignD.DELETE_SWEEP);
        clearLogButton.setGraphic(clearLogIcon);
        clearLogButton.setOnAction(e -> logTextArea.clear());

        HBox logHeader = new HBox(10);
        logHeader.setAlignment(Pos.CENTER_LEFT);
        Region logSpacer = new Region();
        HBox.setHgrow(logSpacer, Priority.ALWAYS);
        logHeader.getChildren().addAll(logLabel, logSpacer, clearLogButton);

        logSection.getChildren().addAll(logHeader, logTextArea);
        VBox.setVgrow(logTextArea, Priority.ALWAYS);

        centerContent.getChildren().addAll(dataSection, logSection);
        VBox.setVgrow(dataSection, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(centerContent);
        scrollPane.setFitToWidth(true);  // Only fit to width, not height
        scrollPane.setFitToHeight(false); // Allow vertical scrolling
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPadding(new Insets(0));

        //return centerContent;
        return scrollPane;
    }

    private HBox createBottomBar() {
        HBox bottomBar = new HBox(15);
        bottomBar.getStyleClass().add("bottom-bar");
        bottomBar.setPadding(new Insets(10, 20, 10, 20));
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("Ready");
        FontIcon statusIcon = new FontIcon(MaterialDesignI.INFORMATION);
        statusIcon.setIconSize(16);
        statusLabel.setGraphic(statusIcon);

        bottomBar.getChildren().add(statusLabel);
        return bottomBar;
    }

    private void setupEventHandlers() {
        connectButton.setOnAction(e -> handleMqttConnect());
        startButton.setOnAction(e -> handleStartStop());

        // Setup log callback
        logManager.setLogCallback(message ->
                Platform.runLater(() -> {
                    logTextArea.appendText(message + "\n");
                    logTextArea.setScrollTop(Double.MAX_VALUE);
                })
        );
    }

    private void refreshPortList() {
        portComboBox.getItems().clear();
        portComboBox.getItems().addAll(serialService.getAvailablePorts());

        if (!portComboBox.getItems().isEmpty()) {
            portComboBox.getSelectionModel().selectFirst();
        }

        updateStatus("Port list refreshed");
        logManager.info("Available ports refreshed");
    }

    private void handleMqttConnect() {
        if (mqttService.isConnected()) {
            mqttService.disconnect();
            updateMqttConnectionStatus(false);
            connectButton.setText("Connect to Broker");
            startButton.setDisable(true);
            logManager.info("Disconnected from MQTT broker");
        } else {
            String broker = brokerField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (broker.isEmpty()) {
                showError("Broker URL is required");
                return;
            }

            try {
                mqttService.connect(broker, username, password);
                updateMqttConnectionStatus(true);
                connectButton.setText("Disconnect from Broker");
                startButton.setDisable(false);
                logManager.info("Connected to MQTT broker: " + broker);
                updateStatus("Connected to MQTT broker");
            } catch (Exception ex) {
                showError("Failed to connect to MQTT broker: " + ex.getMessage());
                logManager.error("MQTT connection failed: " + ex.getMessage());
            }
        }
    }

    private void handleStartStop() {
        if (!isRunning) {
            String selectedPort = portComboBox.getValue();
            if (selectedPort == null) {
                showError("Please select a COM port");
                return;
            }

            try {
                serialService.connect(selectedPort, 9600);
                serialService.setDataCallback(this::handleSerialData);

                sessionStartTime = LocalDateTime.now();
                messageCount = 0;
                updateSessionInfo();

                isRunning = true;
                startButton.setText("Stop Monitoring");
                startButton.getStyleClass().remove("success-button");
                startButton.getStyleClass().add("danger-button");
                FontIcon stopIcon = new FontIcon(MaterialDesignS.STOP);
                startButton.setGraphic(stopIcon);

                portComboBox.setDisable(true);
                connectButton.setDisable(true);

                updateStatus("Monitoring started on " + selectedPort);
                logManager.info("Serial monitoring started on port: " + selectedPort);

            } catch (Exception ex) {
                showError("Failed to start monitoring: " + ex.getMessage());
                logManager.error("Failed to start serial monitoring: " + ex.getMessage());
            }
        } else {
            stopMonitoring();
        }
    }

    private void stopMonitoring() {
        serialService.disconnect();

        isRunning = false;
        startButton.setText("Start Monitoring");
        startButton.getStyleClass().remove("danger-button");
        startButton.getStyleClass().add("success-button");
        FontIcon playIcon = new FontIcon(MaterialDesignP.PLAY);
        startButton.setGraphic(playIcon);

        portComboBox.setDisable(false);
        connectButton.setDisable(false);

        stopTimeLabel.setText("Stop: " + LocalDateTime.now().format(TIME_FORMATTER));

        updateStatus("Monitoring stopped");
        logManager.info("Serial monitoring stopped. Total messages: " + messageCount);
    }

    private void handleSerialData(String data) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            String displayData = String.format("[%s] WEIGHT: %s", timestamp, data);

            dataTextArea.appendText(displayData + "\n");
            dataTextArea.setScrollTop(Double.MAX_VALUE);

            messageCount++;
            messagesCountLabel.setText("Messages: " + messageCount);

            // Publish to MQTT
            if (mqttService.isConnected()) {
                try {
                    String topic = topicField.getText();
                    String payload = String.format("{\"timestamp\":\"%s\",\"weight\":\"%s\"}",
                            timestamp, data);
                    mqttService.publish(topic, payload);
                    logManager.debug("Published to MQTT: " + payload);
                } catch (Exception ex) {
                    logManager.error("Failed to publish to MQTT: " + ex.getMessage());
                }
            }
        });
    }

    private void updateMqttConnectionStatus(boolean connected) {
        Platform.runLater(() -> {
            if (connected) {
                connectionStatusLabel.setText("Connected");
                connectionStatusLabel.getStyleClass().remove("status-disconnected");
                connectionStatusLabel.getStyleClass().add("status-connected");
            } else {
                connectionStatusLabel.setText("Disconnected");
                connectionStatusLabel.getStyleClass().remove("status-connected");
                connectionStatusLabel.getStyleClass().add("status-disconnected");
            }
        });
    }

    private void updateSessionInfo() {
        if (sessionStartTime != null) {
            startTimeLabel.setText("Start: " + sessionStartTime.format(TIME_FORMATTER));
        }
        messagesCountLabel.setText("Messages: " + messageCount);
        stopTimeLabel.setText("Stop: N/A");
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void shutdown() {
        if (isRunning) {
            stopMonitoring();
        }
        if (mqttService.isConnected()) {
            mqttService.disconnect();
        }
    }

    public BorderPane getRoot() {
        return root;
    }
}
