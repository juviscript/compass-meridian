package dev.juviscript.compassmeridian.model;

public class KeyMapping {

    private String up;
    private String down;
    private String left;
    private String right;

    public KeyMapping(String up, String down, String left, String right) {
        this.up    = up;
        this.down  = down;
        this.left  = left;
        this.right = right;
    }

    // Default WASD
    public KeyMapping() {
        this("w", "s", "a", "d");
    }

    public String getUp()    { return up; }
    public String getDown()  { return down; }
    public String getLeft()  { return left; }
    public String getRight() { return right; }

    public void setUp(String up)       { this.up    = up; }
    public void setDown(String down)   { this.down  = down; }
    public void setLeft(String left)   { this.left  = left; }
    public void setRight(String right) { this.right = right; }

    @Override
    public String toString() {
        return "KeyMapping{up=" + up + ", down=" + down +
                ", left=" + left + ", right=" + right + "}";
    }
}