based off Random World demo

create two constructors for hallway with random length each time called
and room with random dimensions each time called

Separate constructor for vertical and horizontal hallways?

What should be the max length of hallway and dimension of room?

function that starts with a random room near the middle of the board
and builds hallways and rooms off it from a random wall tile in a chain like way, ie
random room/hallway spawns from the wall of the start room, and 
then random room/hallway spawns from that random room/hallway

Do we want more than 1 random room/hallway to spawn from the wall of 
a single room/hallway?

if spawns inwardly, any tile where there is floor and wall is meant to be built will stay 
floor (floor always overrides wall, useful for new hallways)

When to stop spawning new rooms/hallways? 
