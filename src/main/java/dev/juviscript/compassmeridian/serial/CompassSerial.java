package dev.juviscript.compassmeridian.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

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
     * Send a command and return the response lines up to
     * "END", "OK", "SAVED", "RESET", or "ERROR".
     */
    public List<String> sendCommand(String command) {
        List<String> response = new ArrayList<>();
        if (!isConnected()) {
            System.err.println("[serial] Not connected");
            return response;
        }

        try {
            String msg = command + "\n";
            writer.write(msg.getBytes());
            writer.flush();
            System.out.println("[serial] Sent: " + command);

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                System.out.println("[serial] Recv: " + line);
                response.add(line);

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

    // ── Hotplug detection ─────────────────────────────────

    /**
     * Starts listening for Compass connect/disconnect events.
     *
     * Disconnect is detected via jSerialComm's PORT_DISCONNECTED event.
     * Connect is detected by polling for new ports every 2 seconds
     * since jSerialComm has no native hotplug listener.
     *
     * @param onConnect    called when a Compass is detected and connected
     * @param onDisconnect called when the Compass is unplugged
     */
    public void startHotplugListener(Runnable onConnect, Runnable onDisconnect) {

        // ── Disconnect detector ───────────────────────────
        // Attach a listener to the currently open port.
        // jSerialComm fires PORT_DISCONNECTED when the device is unplugged.
        if (port != null && port.isOpen()) {
            port.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType()
                            == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
                        System.out.println("[serial] Compass disconnected");
                        connected = false;
                        onDisconnect.run();
                    }
                }
            });
        }

        // ── Connect detector ──────────────────────────────
        // Poll every 2 seconds for a new Compass port appearing.
        // When found, connect and re-attach the hotplug listener
        // so disconnect detection works on the new connection.
        // ── Connect detector ──────────────────────────────────────
        Thread hotplugThread = new Thread(() -> {
            while (true) {
                if (!connected) {
                    SerialPort found = findCompass();
                    if (found != null) {
                        System.out.println("[serial] Compass detected: "
                                + found.getSystemPortName());

                        try { Thread.sleep(1500); }
                        catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }

                        if (connect(found)) {
                            startHotplugListener(onConnect, onDisconnect);
                            onConnect.run();
                        }
                    }
                }

                try { Thread.sleep(2000); }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "compass-hotplug-thread");

        hotplugThread.setDaemon(true);  // ← dies automatically when app closes
        hotplugThread.start();
    }

    // ── Getters ───────────────────────────────────────────

    public String getPortName() {
        return port != null ? port.getSystemPortName() : "None";
    }
}