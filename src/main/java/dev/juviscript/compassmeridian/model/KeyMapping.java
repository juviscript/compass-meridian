package dev.juviscript.compassmeridian.model;

public class KeyMapping {

    private String up;
    private String down;
    private String left;
    private String right;
    private String click;
    private int deadzone;
    private int diagonalDeadzone;

    // Default WASD + SPACE + default deadzones
    public KeyMapping() {
        this("w", "s", "a", "d", "SPACE", 120, 60);
    }

    public KeyMapping(String up, String down, String left, String right,
                      String click, int deadzone, int diagonalDeadzone) {
        this.up              = up;
        this.down            = down;
        this.left            = left;
        this.right           = right;
        this.click           = click;
        this.deadzone        = deadzone;
        this.diagonalDeadzone = diagonalDeadzone;
    }

    public String getUp()               { return up; }
    public String getDown()             { return down; }
    public String getLeft()             { return left; }
    public String getRight()            { return right; }
    public String getClick()            { return click; }
    public int getDeadzone()            { return deadzone; }
    public int getDiagonalDeadzone()    { return diagonalDeadzone; }

    public void setUp(String up)                              { this.up              = up; }
    public void setDown(String down)                          { this.down            = down; }
    public void setLeft(String left)                          { this.left            = left; }
    public void setRight(String right)                        { this.right           = right; }
    public void setClick(String click)                        { this.click           = click; }
    public void setDeadzone(int deadzone)                     { this.deadzone        = deadzone; }
    public void setDiagonalDeadzone(int diagonalDeadzone)     { this.diagonalDeadzone = diagonalDeadzone; }

    @Override
    public String toString() {
        return "KeyMapping{up=" + up + ", down=" + down + ", left=" + left +
                ", right=" + right + ", click=" + click +
                ", deadzone=" + deadzone + ", diagonalDeadzone=" + diagonalDeadzone + "}";
    }
}