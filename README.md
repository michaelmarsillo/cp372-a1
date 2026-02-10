# CP372 Assignment 1 - Bulletin Board System

This is our implementation of a client-server bulletin board system for CP372. The server handles multiple clients connecting over TCP and manages a shared board where clients can post notes, pin them, and query the board state.

## Project structure

```
cp372-assignment1/
├── server/          - Server side code
│   ├── BBoard.java        - Main server entry point
│   ├── ClientHandler.java - Handles each client connection (one thread per client)
│   ├── Board.java         - The actual board logic with synchronized methods
│   ├── Note.java          - Note object
│   └── Pin.java           - Pin object
├── client/          - Client side code
│   └── BulletinBoardClient.java  - GUI client using Swing
└── README.md
```

## How to compile and run

### Compiling the server

```bash
cd server
javac *.java
```

### Compiling the client

```bash
cd client
javac *.java
```

## Running everything

### First - Start the server

Open a terminal:

```bash
cd server
java BBoard 4554 200 100 20 10 red white green yellow
```

The arguments are:
- Port number (we used 4554)
- Board width (200)
- Board height (100)  
- Note width (20)
- Note height (10)
- Colors (space separated - can add as many as you want)

You should see something like:
```
Bulletin Board Server started on port 4554
Board dimensions: 200x100
Note dimensions: 20x10
Valid colors: [red, white, green, yellow]
Waiting for clients...
```

### Then - Start the client

Open another terminal:

```bash
cd client
java BulletinBoardClient
```

A window pops up. Type in `localhost` for the server and `4554` for port, then hit Connect. You can open multiple client windows to test concurrent connections.

## Using the client

### Connecting
Just enter the server IP (use localhost if running on same machine) and port, then click Connect. Once connected you'll see the handshake messages in the output area and the buttons will become enabled.

### Posting a note
Fill in the X and Y coordinates, pick a color, type your message, and click Post Note. The server will reply with OK NOTE_POSTED if it worked, or an error if something's wrong (like out of bounds or overlapping an existing note).

### Getting notes
You can get all notes by just clicking Get Notes with everything blank. Or you can filter:
- Pick a color to only see notes of that color
- Enter Contains X and Y to find notes at a specific coordinate
- Type in RefersTo to search for notes containing certain text

### Getting pins
Click Get Pins to see all the pin coordinates currently on the board.

### Pinning and unpinning
Enter coordinates and click Pin to add a pin there. If the coordinate is inside multiple overlapping notes, it pins all of them. Unpin removes a pin from that coordinate.

Note: A note is considered "pinned" if it has at least one pin in it.

### Shake and Clear
- Shake removes all unpinned notes (useful for cleaning up the board)
- Clear removes everything - all notes and all pins

### Disconnecting
Hit Disconnect when you're done. The server will keep running for other clients.

## Testing stuff

### Basic tests we did

1. Start the server with valid args and make sure it doesn't crash
2. Connect a client and post some notes
3. Try getting notes with different filters
4. Pin some notes and verify they show as pinned
5. Shake the board and make sure only pinned notes stay
6. Clear everything and verify the board is empty

### Error testing

We tested all the error cases:
- POST at (195, 95) when board is only 200x100 -> gets OUT_OF_BOUNDS error
- POST with a color that wasn't in the startup list -> COLOUR_NOT_SUPPORTED
- POST two notes at exact same position -> COMPLETE_OVERLAP error
- PIN at an empty spot -> NO_NOTE_AT_COORDINATE
- UNPIN where there's no pin -> PIN_NOT_FOUND

### Concurrent client testing

Open 2-3 client windows and connect them all. Then try:
- Post from one client, get from another - you should see the new note
- Pin from one client, the others should see it as pinned when they query
- Shake from one client and everyone sees the updated board

The synchronized methods on the Board class make sure everything stays consistent even with multiple clients hammering the server at the same time.


## Protocol quick reference

Commands the client can send:
- `POST x y color message` - post a new note
- `GET [filters]` - get notes (filters are optional: color=, contains=, refersTo=)
- `GET PINS` - get all pin coordinates
- `PIN x y` - add a pin
- `UNPIN x y` - remove a pin  
- `SHAKE` - remove unpinned notes
- `CLEAR` - remove everything
- `DISCONNECT` - close connection

Server responses:
- `OK <status>` for successful commands
- `OK <count>` followed by data lines for queries
- `ERROR <code> <description>` when something goes wrong

Error codes we implemented:
- INVALID_FORMAT - command syntax is wrong
- OUT_OF_BOUNDS - note doesn't fit on board
- COLOUR_NOT_SUPPORTED - color not in the allowed list
- COMPLETE_OVERLAP - note is in exact same spot as existing note
- PIN_NOT_FOUND - trying to unpin where there's no pin
- NO_NOTE_AT_COORDINATE - trying to pin where there's no note

## Team

- [Michael Marsillo](https://github.com/michaelmarsillo) - worked on server implementation
- [Gurshan Sidhar](https://github.com/GurshanSidhar7) - worked on client GUI
- Both of us - RFC design, testing, debugging
