package dev.juviscript.compassmeridian.serial;

import dev.juviscript.compassmeridian.model.KeyMapping;
import dev.juviscript.compassmeridian.model.Profile;

import java.util.ArrayList;
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

    public List<String> getProfiles() {
        return serial.sendCommand("GET_PROFILES");
    }

    public boolean saveProfile(String name) {
        List<String> response = serial.sendCommand("SAVE_PROFILE " + name);
        return response.contains("OK");
    }

    public boolean loadProfile(String filename) {
        List<String> response = serial.sendCommand("LOAD_PROFILE " + filename);
        return response.contains("OK");
    }

    public boolean deleteProfile(String filename) {
        List<String> response = serial.sendCommand("DELETE_PROFILE " + filename);
        return response.contains("OK");
    }

    public boolean renameProfile(String oldFilename, String newName) {
        List<String> response = serial.sendCommand("RENAME_PROFILE " + oldFilename + " " + newName);
        return response.contains("OK");
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

    public List<Profile> getProfileList() {
        List<String> lines = serial.sendCommand("GET_PROFILES");
        List<Profile> profiles = new ArrayList<>();
        for (String line : lines) {
            if (line.equals("END")) break;
            Profile p = Profile.fromLine(line);
            if (p != null) profiles.add(p);
        }
        return profiles;
    }
}