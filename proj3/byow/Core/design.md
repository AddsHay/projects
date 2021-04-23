
Do we want rooms to be able to be built off rooms?

Function that chooses random wall tile on each structure 

using that random tile as the position, we call a random one of our three build functions 
from that spot.

how to deal with conflicts:
if a new build would overlap an existing build, cancel it and do another random call
 

Do a random number of those build calls in a certain range

If you have x failed build calls in a row, end early

Logic of DrawBuild:
1. Set up
   1. Place empty tiles 
   2. generate initial room
   3. start the Steps list
2. Add next branch data to the Steps list
3. Run bloom(base) on the Steps list

Logic of Bloom:
   1. 'Fix' the base data (prevent collisions, out-of-bounds errors)
   2. Build the defined structure
        1. 'Room' or 'hall', up/down/left/right
   3. Add random number of random structures to the Steps list
   4. Run bloom(base) on the next item in Steps



