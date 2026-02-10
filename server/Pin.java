import java.util.Objects;

// represents a pin at a specific coordinate on the board
public class Pin {
    private int x;
    private int y;
    
    public Pin(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pin pin = (Pin) o;
        return x == pin.x && y == pin.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
