package dev.juviscript.compassmeridian.serial;

import dev.juviscript.compassmeridian.model.KeyMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompassProtocol {

    private final CompassSerial serial;

    public CompassProtocol(CompassSerial serial) {
        this.serial = serial;
    }

    // ── Commands ──────────────────────────────────────────

    public Map<String, String> getInfo() {
        List<String> lines = serial.sendCommand("GET_INFO");
        return parseKeyValue(lines);
    }

    public KeyMapping getConfig() {
        List<String> lines = serial.sendCommand("GET_CONFIG");
        Map<String, String> map = parseKeyValue(lines);
        return new KeyMapping(
                map.getOrDefault("up",    "w"),
                map.getOrDefault("down",  "s"),
                map.getOrDefault("left",  "a"),
                map.getOrDefault("right", "d")
        );
    }

    public boolean setKey(String direction, String key) {
        List<String> lines = serial.sendCommand("SET " + direction + " " + key);
        return lines.contains("OK");
    }

    public boolean save() {
        List<String> lines = serial.sendCommand("SAVE");
        return lines.contains("SAVED");
    }

    public boolean reset() {
        List<String> lines = serial.sendCommand("RESET");
        return lines.contains("RESET");
    }

    public boolean restart() {
        List<String> response = serial.sendCommand("RESTART");
        return response.contains("RESTARTING");
    }

    public boolean setName(String name) {
        List<String> lines = serial.sendCommand("SET_NAME " + name);
        return lines.contains("OK");
    }

    // ── Parsing ───────────────────────────────────────────

    private Map<String, String> parseKeyValue(List<String> lines) {
        Map<String, String> map = new HashMap<>();
        for (String line : lines) {
            int sep = line.indexOf('=');
            if (sep != -1) {
                map.put(line.substring(0, sep).trim(),
                        line.substring(sep + 1).trim());
            }
        }
        return map;
    }
}