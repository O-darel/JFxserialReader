package org.serial.serial.service;


import com.fazecast.jSerialComm.SerialPort;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SerialService {
    private SerialPort serialPort;
    private ExecutorService executorService;
    private Consumer<String> dataCallback;
    private volatile boolean isReading = false;

    public List<String> getAvailablePorts() {
        List<String> ports = new ArrayList<>();
        SerialPort[] availablePorts = SerialPort.getCommPorts();

        for (SerialPort port : availablePorts) {
            ports.add(port.getSystemPortName());
        }

        return ports;
    }

    public void connect(String portName, int baudRate) throws Exception {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baudRate);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(1);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

        if (!serialPort.openPort()) {
            throw new Exception("Failed to open port: " + portName);
        }

        startReading();
    }

    public void disconnect() {
        isReading = false;

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }

    public void setDataCallback(Consumer<String> callback) {
        this.dataCallback = callback;
    }

    private void startReading() {
        isReading = true;
        executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            byte[] readBuffer = new byte[1024];
            StringBuilder messageBuilder = new StringBuilder();

            while (isReading && !Thread.currentThread().isInterrupted()) {
                try {
                    // Send request for reading (equivalent to Python's ser.write(b"\r\n"))
                    serialPort.writeBytes(new byte[]{'\r', '\n'}, 2);

                    // Wait a bit for response
                    Thread.sleep(300);

                    // Read available data
                    int numRead = serialPort.readBytes(readBuffer, readBuffer.length);

                    if (numRead > 0) {
                        String data = new String(readBuffer, 0, numRead);

                        // Process data character by character
                        for (char c : data.toCharArray()) {
                            if (c == '\n' || c == '\r') {
                                if (messageBuilder.length() > 0) {
                                    String message = messageBuilder.toString().trim();
                                    if (!message.isEmpty() && dataCallback != null) {
                                        dataCallback.accept(message);
                                    }
                                    messageBuilder.setLength(0);
                                }
                            } else if (c >= 32 && c < 127) { // Printable ASCII characters
                                messageBuilder.append(c);
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    if (isReading) {
                        System.err.println("Error reading serial data: " + e.getMessage());
                    }
                }
            }
        });
    }

    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }
}
