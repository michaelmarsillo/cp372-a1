import java.util.ArrayList;
import java.util.List;

// manages the bulletin board state with synchronized access
public class Board {
    private int boardWidth;
    private int boardHeight;
    private int noteWidth;
    private int noteHeight;
    private List<String> validColors;
    private List<Note> notes;
    private List<Pin> pins;
    
    public Board(int boardWidth, int boardHeight, int noteWidth, int noteHeight, List<String> validColors) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.validColors = validColors;
        this.notes = new ArrayList<>();
        this.pins = new ArrayList<>();
    }
    
    public int getBoardWidth() {
        return boardWidth;
    }
    
    public int getBoardHeight() {
        return boardHeight;
    }
    
    public int getNoteWidth() {
        return noteWidth;
    }
    
    public int getNoteHeight() {
        return noteHeight;
    }
    
    public List<String> getValidColors() {
        return new ArrayList<>(validColors);
    }
    
    // POST command - add a new note
    public synchronized String postNote(int x, int y, String color, String message) {
        // validate color
        if (!validColors.contains(color)) {
            return "ERROR COLOUR_NOT_SUPPORTED " + color + " is not a valid color";
        }
        
        // check bounds
        if (x < 0 || y < 0 || x + noteWidth > boardWidth || y + noteHeight > boardHeight) {
            return "ERROR OUT_OF_BOUNDS Note exceeds board boundaries";
        }
        
        // check for complete overlap
        Note newNote = new Note(x, y, color, message);
        for (Note existing : notes) {
            if (newNote.completelyOverlaps(existing, noteWidth, noteHeight)) {
                return "ERROR COMPLETE_OVERLAP Note overlaps an existing note entirely";
            }
        }
        
        notes.add(newNote);
        return "OK NOTE_POSTED";
    }
    
    // GET command - retrieve notes based on filters
    public synchronized String getNotes(String colorFilter, Integer containsX, Integer containsY, String refersToFilter) {
        List<Note> filtered = new ArrayList<>();
        
        for (Note note : notes) {
            boolean matches = true;
            
            // filter by color
            if (colorFilter != null && !note.getColor().equals(colorFilter)) {
                matches = false;
            }
            
            // filter by contains coordinate
            if (matches && containsX != null && containsY != null) {
                if (!note.containsPoint(containsX, containsY, noteWidth, noteHeight)) {
                    matches = false;
                }
            }
            
            // filter by refersTo substring
            if (matches && refersToFilter != null) {
                if (!note.getMessage().contains(refersToFilter)) {
                    matches = false;
                }
            }
            
            if (matches) {
                filtered.add(note);
            }
        }
        
        // build response
        StringBuilder response = new StringBuilder();
        response.append("OK ").append(filtered.size()).append("\n");
        for (Note note : filtered) {
            response.append("NOTE ")
                    .append(note.getX()).append(" ")
                    .append(note.getY()).append(" ")
                    .append(note.getColor()).append(" ")
                    .append(note.getMessage()).append(" ")
                    .append("PINNED=").append(note.isPinned())
                    .append("\n");
        }
        
        return response.toString().trim();
    }
    
    // GET PINS command - retrieve all pins
    public synchronized String getPins() {
        StringBuilder response = new StringBuilder();
        response.append("OK ").append(pins.size()).append("\n");
        for (Pin pin : pins) {
            response.append("PIN ")
                    .append(pin.getX()).append(" ")
                    .append(pin.getY())
                    .append("\n");
        }
        
        return response.toString().trim();
    }
    
    // PIN command - add a pin at coordinate
    public synchronized String addPin(int x, int y) {
        // find all notes that contain this coordinate
        List<Note> notesAtPoint = new ArrayList<>();
        for (Note note : notes) {
            if (note.containsPoint(x, y, noteWidth, noteHeight)) {
                notesAtPoint.add(note);
            }
        }
        
        if (notesAtPoint.isEmpty()) {
            return "ERROR NO_NOTE_AT_COORDINATE No note contains the given point";
        }
        
        // add pin to the global list
        Pin pin = new Pin(x, y);
        if (!pins.contains(pin)) {
            pins.add(pin);
        }
        
        // add pin to all notes at this coordinate
        for (Note note : notesAtPoint) {
            note.addPin(pin);
        }
        
        return "OK PIN_ADDED";
    }
    
    // UNPIN command - remove a pin at coordinate
    public synchronized String removePin(int x, int y) {
        Pin pin = new Pin(x, y);
        
        // check if pin exists
        if (!pins.contains(pin)) {
            return "ERROR PIN_NOT_FOUND No pin exists at the given coordinates";
        }
        
        // remove pin from global list
        pins.remove(pin);
        
        // remove pin from all notes
        for (Note note : notes) {
            note.removePin(pin);
        }
        
        return "OK PIN_REMOVED";
    }
    
    // SHAKE command - remove all unpinned notes
    public synchronized String shake() {
        List<Note> pinnedNotes = new ArrayList<>();
        
        for (Note note : notes) {
            if (note.isPinned()) {
                pinnedNotes.add(note);
            }
        }
        
        notes = pinnedNotes;
        return "OK SHAKE_COMPLETE";
    }
    
    // CLEAR command - remove all notes and pins
    public synchronized String clear() {
        notes.clear();
        pins.clear();
        return "OK BOARD_CLEARED";
    }
}
