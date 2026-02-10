import java.util.ArrayList;
import java.util.List;

// represents a note on the bulletin board
public class Note {
    private int x;
    private int y;
    private String color;
    private String message;
    private List<Pin> pins;
    
    public Note(int x, int y, String color, String message) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.message = message;
        this.pins = new ArrayList<>();
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<Pin> getPins() {
        return pins;
    }
    
    // check if this note contains a given point
    public boolean containsPoint(int px, int py, int noteWidth, int noteHeight) {
        return px >= x && px < x + noteWidth && 
               py >= y && py < y + noteHeight;
    }
    
    // check if this note is pinned (has at least one pin)
    public boolean isPinned() {
        return !pins.isEmpty();
    }
    
    // check if this note completely overlaps another note
    public boolean completelyOverlaps(Note other, int noteWidth, int noteHeight) {
        return this.x == other.x && this.y == other.y;
    }
    
    // add a pin to this note
    public void addPin(Pin pin) {
        if (!pins.contains(pin)) {
            pins.add(pin);
        }
    }
    
    // remove a pin from this note
    public void removePin(Pin pin) {
        pins.remove(pin);
    }
}
