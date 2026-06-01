package dev.juviscript.compassmeridian.model;

public class KeyMapping {

    private String up;
    private String down;
    private String left;
    private String right;
    private String click;
    private int threshold;
    private int diagonalThreshold;

    // Default WASD + SPACE + default thresholds
    public KeyMapping() {
        this("w", "s", "a", "d", "SPACE", 120, 60);
    }

    public KeyMapping(String up, String down, String left, String right,
                      String click, int threshold, int diagonalThreshold) {
        this.up                = up;
        this.down              = down;
        this.left              = left;
        this.right             = right;
        this.click             = click;
        this.threshold         = threshold;
        this.diagonalThreshold = diagonalThreshold;
    }

    public String getUp()              { return up; }
    public String getDown()            { return down; }
    public String getLeft()            { return left; }
    public String getRight()           { return right; }
    public String getClick()           { return click; }
    public int getThreshold()          { return threshold; }
    public int getDiagonalThreshold()  { return diagonalThreshold; }

    public void setUp(String up)                          { this.up                = up; }
    public void setDown(String down)                      { this.down              = down; }
    public void setLeft(String left)                      { this.left              = left; }
    public void setRight(String right)                    { this.right             = right; }
    public void setClick(String click)                    { this.click             = click; }
    public void setThreshold(int threshold)               { this.threshold         = threshold; }
    public void setDiagonalThreshold(int diagonalThreshold) { this.diagonalThreshold = diagonalThreshold; }

    @Override
    public String toString() {
        return "KeyMapping{up=" + up + ", down=" + down + ", left=" + left +
                ", right=" + right + ", click=" + click +
                ", threshold=" + threshold + ", diagonal=" + diagonalThreshold + "}";
    }
}