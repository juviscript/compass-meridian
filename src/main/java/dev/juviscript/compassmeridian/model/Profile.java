package dev.juviscript.compassmeridian.model;

public class Profile {

    private final String filename;
    private final String displayName;
    private final boolean builtin;
    private final boolean active;

    public Profile(String filename, String displayName,
                   boolean builtin, boolean active) {
        this.filename    = filename;
        this.displayName = displayName;
        this.builtin     = builtin;
        this.active      = active;
    }

    /**
     * Parse a profile line from GET_PROFILES response.
     * Format: builtin:wasd:WASD:active
     *      or custom:my_fps:My FPS:inactive
     */
    public static Profile fromLine(String line) {
        // Split on first 3 colons only — display name may contain colons
        String[] parts = line.split(":", 4);
        if (parts.length < 4) return null;

        boolean builtin    = parts[0].equals("builtin");
        String filename    = parts[1];
        String displayName = parts[2];
        boolean active     = parts[3].trim().equals("active");

        return new Profile(filename, displayName, builtin, active);
    }

    public String getFilename()    { return filename; }
    public String getDisplayName() { return displayName; }
    public boolean isBuiltin()     { return builtin; }
    public boolean isActive()      { return active; }

    @Override
    public String toString() { return displayName; }
}