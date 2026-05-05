package dev.juviscript.compassmeridian.serial;

import com.fazecast.jSerialComm.SerialPort;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CompassSerial {

    private static final int BAUD_RATE  = 115200;
    private static final int TIMEOUT_MS = 2000;

    private SerialPort port;
    private BufferedReader reader;
    private OutputStream writer;
    private boolean connected = false;

    // ── Port discovery ────────────────────────────────────

    public static List<SerialPort> getAvailablePorts() {
        List<SerialPort> ports = new ArrayList<>();
        for (SerialPort p : SerialPort.getCommPorts()) {
            ports.add(p);
        }
        return ports;
    }

    public static SerialPort findCompass() {
        for (SerialPort p : SerialPort.getCommPorts()) {
            System.out.println("[serial] Scanning: " + p.getSystemPortName()
                    + " | " + p.getPortDescription()
                    + " | VID: " + Integer.toHexString(p.getVendorID()).toUpperCase()
                    + " | PID: " + Integer.toHexString(p.getProductID()).toUpperCase());

            String desc = p.getPortDescription().toUpperCase();
            if (p.getVendorID() == 0x2E8A
                    || desc.contains("PICO")
                    || desc.contains("COMPASS")
                    || desc.contains("RP2040")) {
                System.out.println("[serial] Found Compass on " + p.getSystemPortName());
                return p;
            }
        }
        return null;
    }

    // ── Connection ────────────────────────────────────────

    public boolean connect(SerialPort serialPort) {
        this.port = serialPort;
        port.setBaudRate(BAUD_RATE);
        port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING, TIMEOUT_MS, 0
        );

        if (!port.openPort()) {
            System.err.println("[serial] Failed to open port");
            return false;
        }

        reader = new BufferedReader(new InputStreamReader(port.getInputStream()));
        writer = port.getOutputStream();
        connected = true;
        System.out.println("[serial] Connected to " + port.getSystemPortName());
        return true;
    }

    public void disconnect() {
        connected = false;
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
        } catch (Exception e) {
            System.err.println("[serial] Error closing streams: " + e.getMessage());
        }
        if (port != null && port.isOpen()) port.closePort();
        System.out.println("[serial] Disconnected");
    }

    public boolean isConnected() {
        return connected && port != null && port.isOpen();
    }

    // ── Synchronous send/receive ──────────────────────────

    /**
     * Send a command and return the response lines up to "END" or "OK"/"SAVED"/"RESET".
     */
    public List<String> sendCommand(String command) {
        List<String> response = new ArrayList<>();
        if (!isConnected()) {
            System.err.println("[serial] Not connected");
            return response;
        }

        try {
            // Send command
            String msg = command + "\n";
            writer.write(msg.getBytes());
            writer.flush();
            System.out.println("[serial] Sent: " + command);

            // Read response
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                System.out.println("[serial] Recv: " + line);
                response.add(line);

                // Stop reading on terminal responses
                if (line.equals("END")
                        || line.equals("OK")
                        || line.equals("SAVED")
                        || line.equals("RESET")
                        || line.startsWith("ERROR")) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[serial] Error: " + e.getMessage());
        }

        return response;
    }

    public String getPortName() {
        return port != null ? port.getSystemPortName() : "None";
    }
}