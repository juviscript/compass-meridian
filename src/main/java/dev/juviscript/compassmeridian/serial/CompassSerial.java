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

    private static final int BAUD_RATE        = 115200;
    private static final int TIMEOUT_MS       = 2000;
    private static final int POLL_INTERVAL_MS = 2000;
    private static final int RECONNECT_DELAY_MS = 3000; // cooldown after disconnect

    private SerialPort port;
    private BufferedReader reader;
    private OutputStream writer;
    private volatile boolean connected = false;
    private volatile boolean hotplugListenerRunning = false;
    private volatile long lastDisconnectTime = 0;

    // ── Getters ───────────────────────────────────────────

    public String getPortName() {
        return port != null ? port.getSystemPortName() : "None";
    }

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
        lastDisconnectTime = System.currentTimeMillis();
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

    // ── Attach disconnect listener ────────────────────────

    private void attachDisconnectListener(Runnable onDisconnect) {
        if (port == null || !port.isOpen()) return;
        port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() ==
                        SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
                    if (!connected) return;
                    System.out.println("[serial] Compass disconnected");
                    disconnect(); // ← call full disconnect instead of just setting flag
                    onDisconnect.run();
                }
            }
        });
    }

    // ── Synchronous send/receive ──────────────────────────

    /**
     * Send a command and return the response lines up to
     * "END", "OK", "SAVED", "RESET", or "ERROR".
     */
    private final Object serialLock = new Object();

    public List<String> sendCommand(String command) {
        List<String> response = new ArrayList<>();
        if (!isConnected()) {
            System.err.println("[serial] Not connected");
            return response;
        }

        synchronized (serialLock) {
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
                            || line.startsWith("ERROR")
                            || line.equals("RESTARTING")) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("[serial] Error: " + e.getMessage());
            }
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

        // Attach disconnect listener to current port if open
        attachDisconnectListener(onDisconnect);

        // Only start one polling thread
        if (hotplugListenerRunning) return;
        hotplugListenerRunning = true;

        Thread hotplugThread = new Thread(() -> {
            while (true) {
                if (!connected) {
                    // Cooldown after disconnect — wait for board to finish rebooting
                    long timeSinceDisconnect = System.currentTimeMillis() - lastDisconnectTime;
                    if (timeSinceDisconnect < RECONNECT_DELAY_MS) {
                        System.out.println("[serial] Cooldown — waiting for board to reboot");
                        try { Thread.sleep(RECONNECT_DELAY_MS - timeSinceDisconnect); }
                        catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

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
                            attachDisconnectListener(onDisconnect);
                            onConnect.run();
                        }
                    }
                }

                try { Thread.sleep(POLL_INTERVAL_MS); }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "compass-hotplug-thread");

        hotplugThread.setDaemon(true);
        hotplugThread.start();
    }
}