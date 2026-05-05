package dev.juviscript.compassmeridian.model;

public class Preset {

    private String name;
    private KeyMapping mapping;

    public Preset(String name, KeyMapping mapping) {
        this.name    = name;
        this.mapping = mapping;
    }

    // ── Built-in presets ──────────────────────────────────

    public static Preset wasd() {
        return new Preset("WASD", new KeyMapping("w", "s", "a", "d"));
    }

    public static Preset arrows() {
        return new Preset("Arrow Keys", new KeyMapping("UP", "DOWN", "LEFT", "RIGHT"));
    }

    public static Preset ijkl() {
        return new Preset("IJKL", new KeyMapping("i", "k", "j", "l"));
    }

    // ── Getters / Setters ─────────────────────────────────

    public String getName()          { return name; }
    public KeyMapping getMapping()   { return mapping; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }
}