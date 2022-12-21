import processing.core.*;

import java.util.ArrayList;
import java.util.Random;

public class Grid
{
    //the grid (singleton pattern)
    private static Grid grid;

    //width/height of the grid in number of blocks
    public final int gridWidth = 10;
    public final int gridHeight = 20;

    //width of a block in number of pixels (blocks are squares. [height = width]
    public final static int blockWidth = 30;

    //number of particles per block edge
    public final int particlesPerEdge = 10;

    //what percentage of a block's particles need to be non-air for the block to count as full
    private float fullFactor = 3.0f / 5;

    private int level;
    private float[] levelSpeeds = {48, 43, 38, 33, 28, 23, 18, 13, 8, 6, 5, 5, 5, 4, 4, 4, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1};//, 30, 27, 24, 21, 18, 15, 12, 9, 8, 7, 6, 5, 4, 3, 2, 1};//{24, 22, 19, 17, 14, 12, 9, 7, 4, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 1}; //represents how many ticks per block drop

    private final float ticksPerSecond = 60.0f;
    private float ticksPerBlockDrop = levelSpeeds[0];
    private float particleUpdatesPerBlockDrop = 30.0f;

    private double nanosPerBlockDrop = 1000000000 / ticksPerSecond * ticksPerBlockDrop;
    private double nanosPerParticleUpdate = nanosPerBlockDrop / particleUpdatesPerBlockDrop;

    private double blockDropsDueForCompletion = 0;
    private double particleUpdatesDueForCompletion = 0;

    public long prevTime = System.nanoTime();

    private boolean checkingRows = true;

    public static boolean autoParticle = true;
    public static boolean autoFall = true;
    public static boolean paused = false;
    public static boolean lost;

    //all the blocks in the grid
    public Block blocks[][];
    public Particle particleGrid[][];

    //list of all particles in grid
    public ArrayList<Particle> particleList;

    //the current tetromino
    public Tetromino tetromino;

    //the queue of next tetrominos
    public Tetromino queue[];

    //the tetromino currently being held
    public Tetromino held;

    //width and height in blocks
    public float totWidth;
    public float totHeight;

    //coords of the top left corner of the grid
    public float cornerX;
    public float cornerY;

    //tracks if a swap just occurred
    public boolean swapped;

    public boolean ghostBlock;

    public ArrayList<Integer> shapes;
    public ArrayList<String> types;

    public void toggleCheckingRows()
    {
        checkingRows = !checkingRows;
        System.out.println("toggled checkingRows -> " + checkingRows);
    }

    public void toggleAutoFall()
    {
        autoFall = !autoFall;
        System.out.println("toggled autoFall -> " + autoFall);
    }

    public boolean getAutoFall()
    {
        return autoFall;
    }

    public void toggleAutoParticle()
    {
        autoParticle = !autoParticle;
        System.out.println("toggled autoParticle -> " + autoParticle);
    }

    public void togglePaused()
    {
        paused = !paused;
        System.out.println("toggled paused -> " + paused);
    }

    //calculates the points modifier (you get more points the faster the blocks are falling)
    public float calcScoreLevelModifier()
    {
        return ticksPerSecond / ticksPerBlockDrop;
    }

    //updates the current level
    public void setLevel(int level)
    {
        //update level counter
        this.level = level;

        //set game speed
        if (level < levelSpeeds.length)
        {
            ticksPerBlockDrop = levelSpeeds[level];
        }
        else
        {
            ticksPerBlockDrop = levelSpeeds[levelSpeeds.length - 1];
        }

        nanosPerBlockDrop = 1000000000 / ticksPerSecond * ticksPerBlockDrop;
        nanosPerParticleUpdate = nanosPerBlockDrop / particleUpdatesPerBlockDrop;


        //print level update
        System.out.println("~~~~~~~~~~~");
        System.out.println("Level " + level);
    }

    public void levelUp()
    {
        setLevel(level + 1);
    }

    //default constructor
    private Grid()
    {
        //set oldTimes to the current time
        prevTime = System.nanoTime();

        //reset level
        setLevel(0);

        //no longer lost
        lost = false;

        //calculate pixel width/height of the grid
        totWidth = blockWidth * gridWidth;
        totHeight = blockWidth * gridHeight;

        //calculate coords of top left corner of grid
        cornerX = (Main.screenWidth - totWidth) / 2;
        cornerY = (Main.screenHeight - totHeight) / 2;

        //swapped defaults to false
        swapped = false;

        //droplines defaults to true
        ghostBlock = true;

        shapes = new ArrayList<Integer>();
        types = new ArrayList<String>();
        fillShapes();
        fillTypes();

        //fill the grid with inactive blocks
        blocks = new Block[gridWidth][gridHeight];
        for (int x = 0; x < gridWidth; x++)
        {
            for (int y = 0; y < gridHeight; y++)
            {
                blocks[x][y] = new Block(x, y, blockWidth);
            }
        }

        System.out.println("filled grid");
    }

    public void lose()
    {
        if (!lost)
        {
            lost = true;
            System.out.println("lost");
        }
    }

    public void toggleLost()
    {
        lost = !lost;
        System.out.println("toggled checkingRows -> " + lost);
    }

    //singleton pattern
    public static Grid getGrid()
    {
        if (grid == null)
        {
            grid = new Grid();
        }

        return grid;
    }

    public void setupParticleStuff()
    {
        //initialize the particleGrid
        particleGrid = new Particle[gridWidth * particlesPerEdge][gridHeight * particlesPerEdge];

        //initialize the particle list
        particleList = new ArrayList<Particle>();

        //all particles default to air
        for (int x = 0; x < gridWidth * particlesPerEdge; x++)
        {
            for (int y = 0; y < gridHeight * particlesPerEdge; y++)
            {
                //create a new particle
                Particle particle = ParticleFactory.generateAirParticle(new Point(x, y));

                //add particle to grid
                particleGrid[x][y] = particle;

                //add particle to particle list
                particleList.add(particle);

                //set particle's position in the list
                particle.listIndex = particleList.size() - 1;
            }
        }

        //shuffle the particle list
        shuffleParticleList();

        //loop through particle list
        for (int i = 0; i < particleList.size(); i++)
        {
            //get particle
            Particle particle = particleList.get(i);

            //set each particle's position in the list
            particle.listIndex = i;
        }
    }

    //randomizes the order of all the particles in the particleList
    //ensures that each particle knows its new index in the particleList
    public void shuffleParticleList()
    {
        System.out.println("particle list size before shuffle: " + particleList.size());
        ArrayList<Particle> temp = new ArrayList<Particle>();

        //need to save size because the original list will shrink as the loop progresses
        int size = particleList.size();

        //loop through all particles in the particle list
        for (int lcv = 0; lcv < size; lcv++)
        {
            //get random particle from the old list
            int index = PApplet.parseInt((new Random()).nextInt(particleList.size()));
            Particle particle = particleList.get(index);

            //add it to the new list (in new position)
            temp.add(particle);

            //update that particle's position
            particle.listIndex = lcv;

            //remove the particle from the old list
            particleList.remove(index);
        }

        //replace old list with new list
        particleList = temp;
        System.out.println("particle list size after shuffle: " + particleList.size());
    }

    //refills the shapes list with all shape options
    public void fillShapes()
    {
        for (int lcv = 0; lcv < 7; lcv++)
        {
            shapes.add(lcv);
        }
    }

    //picks a random shape from the list, removes it from the list, and returns it
    public int pickShape()
    {
        if (shapes.size() == 0)
        {
            fillShapes();
        }

        int index = PApplet.parseInt((new Random()).nextInt(shapes.size()));
        int shape = shapes.get(index);
        shapes.remove(index);
        return shape;
    }

    //fill the particle types
    public void fillTypes()
    {
        for (int lcv = 0; lcv < Main.allTypes.size(); lcv++)
        {
            types.add(Main.allTypes.get(lcv));
        }
        System.out.println("Filled types");
    }

    //updates all the particles, then updates which blocks in the grid are active/full
    public void updateParticles()
    {
        //try to move each particle
        // for (Particle[] line : particleGrid)
        // {
        //   for (Particle particle : line)
        //   {
        // for (int x = gridWidth * particlesPerEdge - 1; x >= 0; x--) {
        //   // for (int y = gridHeight * particlesPerEdge - 1; y >= 0; y--) {
        //   for (int y = 0; y < gridHeight * particlesPerEdge; y++) {
        for (Particle particle : particleList)
        {
            // if (!particle.type.equals("Air"))
            // println("updating a particle of type " + particle.type);
            // Particle particle = particleGrid[x][y];
            //if the particle is awake and not fresh
            if (particle.awake && !particle.fresh)
            {
                //mark the particle as not moved
                particle.moved = false;

                //have it try to move
                particle.move();
            }
            // }
        }

        //try to interact each particle
        // for (Particle[] line : particleGrid)
        // {
        //   for (Particle particle : line)
        //   {
        // for (int x = gridWidth * particlesPerEdge - 1; x >= 0; x--) {
        //   // for (int y = gridHeight * particlesPerEdge - 1; y >= 0; y--) {
        //   for (int y = 0; y < gridHeight * particlesPerEdge; y++) {
        //     Particle particle = particleGrid[x][y];
        for (Particle particle : particleList)
        {

            //if the particle is awake
            if (particle.awake)
            {
                //if the particle is fresh, it does nothing and becomes non-fresh
                //if the particle was created this step by another particle (probably in an interaction), then this particle does nothing this step
                if (particle.fresh)
                {
                    particle.fresh = false;
                }
                //if it's not fresh though then it's good to go
                else
                {
                    //mark the particle as not interacted
                    particle.interacted = false;

                    //have it try to interact
                    particle.interact();

                    //check if the particle should go to sleep
                    //if this particle did not move or interact with anything then it goes to sleep (stops being awake)
                    if (!particle.moved && !particle.interacted)
                    {
                        particle.sleep();
                    }
                }
            }
        }
        // }

        //update the block stats
        updateBlockStats();

        //check if any rows have been cleared
        grid.checkRows();
    }

    //updates which blocks in the grid are active/full
    //a block is active if it is not 100% full of air
    //a block is full if more than half of its particles are not air
    public void updateBlockStats()
    {
        //updates every block's activity and fullness in the grid
        for (int x = 0; x < gridWidth; x++)
        {
            for (int y = 0; y < gridHeight; y++)
            {
                //a block is active if it is not empty
                boolean active = false;

                //count how many particles are in the block
                int numParticles = 0;

                //check every particle within the block's space
                for (int px = 0; px < particlesPerEdge; px++)
                {
                    for (int py = 0; py < particlesPerEdge; py++)
                    {
                        Particle particle = particleGrid[x * particlesPerEdge + px][y * particlesPerEdge + py];
                        if (!particle.type.equals("Air"))
                        {
                            active = true;
                            numParticles++;
                        }
                    }
                }

                blocks[x][y].active = active;
                int particlesPerBlock = particlesPerEdge * particlesPerEdge;
                blocks[x][y].full = numParticles > particlesPerBlock * fullFactor;
            }
        }
    }

    //picks a random type from the list, removes it from the list, and returns it
    public String pickType()
    {
        if (types.size() == 0)
        {
            fillTypes();
        }

        int index = PApplet.parseInt((new Random()).nextInt(types.size()));
        String type = types.get(index);
        types.remove(index);
        return type;
    }

    public int calcLevelPoints(int pointsPerParticleCleared)
    {
        return gridWidth * particlesPerEdge * particlesPerEdge * pointsPerParticleCleared * 10;
    }

    //swaps the two particles in the grid
    public void swapParticles(Particle p1, Particle p2)
    {
        //swap indices in the particle grid
        Point temp = p1.getIndices();
        p1.setIndices(p2.getIndices());
        p2.setIndices(temp);

        //put into new positions
        particleGrid[p1.getIndices().x][p1.getIndices().y] = p1;
        particleGrid[p2.getIndices().x][p2.getIndices().y] = p2;

        //swap indices in the particle list
        int temp2 = p1.listIndex;
        p1.listIndex = p2.listIndex;
        p2.listIndex = temp2;

        //put into new positions
        particleList.set(p1.listIndex, p1);
        particleList.set(p2.listIndex, p2);

        //wake each particle
        p1.wake();
        p2.wake();

        //wakes each particle's neighbors
        p1.wakeNeighbors();
        p2.wakeNeighbors();

        //mark both particles as fresh
        p1.fresh = true;
        p2.fresh = true;
    }

    //update the grid
    public void update()
    {
        //update/calculate how much time has passed
        long currentTime = System.nanoTime();
        long difference = currentTime - prevTime;
        prevTime = currentTime;

        if (!lost && !paused)
        {
            //calculate how many things should have happened during that time
            blockDropsDueForCompletion += difference / nanosPerBlockDrop;
            particleUpdatesDueForCompletion += difference / nanosPerParticleUpdate;

            //update particles
            while (particleUpdatesDueForCompletion >= 1)
            {
                //render particles once per tick
                if (autoParticle)
                {
                    grid.updateParticles();
                }

                particleUpdatesDueForCompletion--;
            }

            //update block drops
            while (blockDropsDueForCompletion >= 1)
            {
                if (autoFall)
                {
                    grid.tetromino.down();
                }

                blockDropsDueForCompletion--;
            }
        }
    }

    public static void reset()
    {
        grid = new Grid();
    }

    //replaces the first particle with the second particle (p2 will exist where p1 did)
    public void replaceParticle(Particle p1, Particle p2)
    {
        //update p2's coordinates
        p2.setIndices(p1.getIndices());

        //puts the second particle into the grid
        particleGrid[p1.getIndices().x][p1.getIndices().y] = p2;

        //update p2's position in the particle list
        p2.listIndex = p1.listIndex;

        //puts the second particle into the particle list
        particleList.set(p2.listIndex, p2);

        //wake the new particle (possibly redundant)
        p2.wake();

        //wake the new particle's neighbors
        p2.wakeNeighbors();

        //mark the new particle as fresh (possibly redundant)
        p2.fresh = true;
    }

    //replaces the provided particle with a particle of the provided type
    public void replaceParticle(Particle oldParticle, String newType)
    {
        //create new particle
        Particle newParticle = ParticleFactory.generateParticle(newType, oldParticle);

        //replace the old particle with the new particle
        replaceParticle(oldParticle, newParticle);
    }

    //sets up the tetrominos. Used immediately after grid is constructed. Cannot just be part of the constructor because tetrominos need to access grid's blocks and I don't want to send them into tetromino's constructor every time.
    public void setupTetrominos()
    {
        //build the initial current tetromino
        tetromino = new Tetromino();

        //fill the queue with random tetrominos
        queue = new Tetromino[3];
        for (int lcv = 0; lcv < 3; lcv++)
        {
            queue[lcv] = new Tetromino();
        }

        //set the held tetromino to null
        held = null;
    }

    //checks for any full rows, clears them, and moves everything downward
    public void checkRows()
    {
        if (checkingRows)
        {
            //check every row
            for (int y = 0; y < gridHeight; y++)
            {

                //check if the row is full
                boolean full = true;
                for (int x = 0; x < gridWidth; x++)
                {
                    //if any block in the row is not full then the row is not full
                    if (!blocks[x][y].full)
                    {
                        full = false;
                    }
                }

                //if the row is full
                if (full)
                {
                    //clear it
                    clearRow(y);
                }
            }

            grid.updateBlockStats();
        }
    }

    //clear the row at the given row
    public void clearRow(int row)
    {
        int numParticles = 0;
        //clear the row
        for (int px = 0; px < gridWidth * particlesPerEdge; px++)
        {
            for (int py = row * particlesPerEdge; py < row * particlesPerEdge + particlesPerEdge; py++)
            {
                Particle p = particleGrid[px][py];

                //if the particle is not air
                if (!p.type.equals("Air"))
                {
                    //increment the number of particles and set the particle to air
                    numParticles++;

                    //replace that particle with a new air particle
                    grid.replaceParticle(p, "Air");
                }
            }
        }

        //add points
        ScoreKeeper.getScoreKeeper().clearParticles(numParticles);

        //lower any rows above the newly cleared row

        //bottom up means that empty rows move upward until they are gone
        //does not bother with row 0 because there is nothing to lower into that row
        for (int y = row; y > 0; y--)
        {

            //swap this row with the row above it
            for (int px = 0; px < gridWidth * particlesPerEdge; px++)
            {
                for (int py = y * particlesPerEdge; py < y * particlesPerEdge + particlesPerEdge; py++)
                {
                    //copy above row to lower
                    Particle above = particleGrid[px][py - particlesPerEdge];
                    Particle curr = particleGrid[px][py];

                    grid.swapParticles(above, curr);
                }
            }
        }
    }


    //attempts to hold the current piece - cannot swap if the piece was just swapped out
    public void hold()
    {
        //if this is the first to swap
        if (held == null)
        {
            //just move it into the held slot and get a new tetromino as usual
            held = tetromino;
            newTetromino();
            swapped = true;
        }
        //otherwise swap the tetrominos
        else if (!swapped)
        {
            Tetromino temp = tetromino;
            tetromino = held;
            held = temp;
            swapped = true;

            //reset held's offset

            //x offset is at about the center of the screen
            tetromino.offsetX = (gridWidth - 1) / 2;

            //y offset is the negative of the lowest y value in the piece's blocks
            tetromino.offsetY = tetromino.blocks[0].y;
            for (int lcv = 1; lcv < 4; lcv++)
            {
                int y = tetromino.blocks[lcv].y;
                if (y < tetromino.offsetY)
                {
                    tetromino.offsetY = y;
                }
            }
            tetromino.offsetY *= -1;

            //if the new piece is colliding with any particles
            if (tetromino.collision(0, 0))
            {
                //lose the game
                lose();
            }
        }
    }

    //pull a new tetromino from the queue, and add a new tetromino to the back of the queue
    //also resets swapped to false
    public void newTetromino()
    {
        tetromino = queue[0];
        queue[0] = queue[1];
        queue[1] = queue[2];
        queue[2] = new Tetromino();


        //if the new tetromino is colliding with a particle
        if (tetromino.collision(0, 0))
        {
            //lose the game
            lose();
        }

        //update whether the current piece was swapped
        swapped = false;
    }

    public void render(Main drawer)
    {
        //render drop speed
        drawer.drawTextWithBorder("BPS: " + (ticksPerSecond / ticksPerBlockDrop), grid.cornerX - blockWidth * 6, grid.cornerY + blockWidth * 7, 20, new Color(255, 255, 255), new Color(0, 0, 0));

        //draw rectangle background
        drawer.fill(200, 200, 200);
        drawer.rect(cornerX, cornerY, totWidth, totHeight);

        //loop through/render full blocks
        // int val = 150;
        // stroke(0, 0, 0, 0);
        // for (int x = 0; x < gridWidth; x++) {
        //   for (int y = 0; y < gridHeight; y++) {
        //     blocks[x][y].render();
        //   }
        // }
        // stroke(0, 0, 0);

        // //loop through/render full blocks
        // for (int x = 0; x < gridWidth; x++) {
        //   for (int y = 0; y < gridHeight; y++) {
        //     blocks[x][y].render();
        //   }
        // }

        //render the falling tetromino
        tetromino.render(true, drawer);

        //draw the ghost block if enabled
        if (ghostBlock)
        {
            //duplicate the tetromino, but decrease the alpha
            Tetromino ghost = new Tetromino(tetromino);
            ghost.colour.a = 127;
            // ghost.stroke = particleFactory.getColour(tetromino.type);

            if (ghost.type.equals("Charcoal"))
            {
                ghost.colour.a = PApplet.parseInt(255 * .75f);
            }
            //slam the duplicate but don't place it
            ghost.slam(false);

            //particle slam the duplicate as well
            ghost.particleSlam();

            //render the ghost
            ghost.render(true, drawer);
        }

        //render the particles
        for (Particle[] line : particleGrid)
        {
            for (Particle particle : line)
            {
                particle.render(drawer);
            }
        }

        //render blocks
        for (int x = 0; x < gridWidth; x++)
        {
            for (int y = 0; y < gridHeight; y++)
            {
                blocks[x][y].render(drawer);
            }
        }
        for (int x = 0; x < gridWidth; x++)
        {
            for (int y = 0; y < gridHeight; y++)
            {
                blocks[x][y].renderEdgeOutlines(drawer);
            }
        }
        for (int x = 0; x < gridWidth; x++)
        {
            for (int y = 0; y < gridHeight; y++)
            {
                blocks[x][y].renderEdges(drawer);
            }
        }
        drawer.stroke(0, 0, 0);

        //draw grid border
        drawer.strokeWeight(4);
        drawer.noFill();
        if (Main.debug)
        {
            drawer.stroke(255, 0, 0);
        }
        drawer.rect(cornerX, cornerY, totWidth, totHeight);
        drawer.strokeWeight(1);
        if (Main.debug)
        {
            drawer.stroke(0, 0, 0);
        }

        //the held box
        drawer.fill(120, 120, 120);
        drawer.strokeWeight(4);
        drawer.rect(cornerX - 7 * blockWidth, cornerY, 6 * blockWidth, 6 * blockWidth);
        drawer.strokeWeight(1);

        //the held tetromino
        if (grid.held != null)
        {

            //calculate width and height of the tetromino
            int xMin = grid.held.blocks[0].x;
            int xMax = xMin;
            int yMin = grid.held.blocks[0].y;
            int yMax = yMin;
            for (int lcv = 1; lcv < 4; lcv++)
            {
                int x = grid.held.blocks[lcv].x;
                int y = grid.held.blocks[lcv].y;

                if (x < xMin)
                {
                    xMin = x;
                }
                else if (x > xMax)
                {
                    xMax = x;
                }
                if (y < yMin)
                {
                    yMin = y;
                }
                else if (y > yMax)
                {
                    yMax = y;
                }
            }

            int tWidth = xMax - xMin + 1;
            int tHeight = yMax - yMin + 1;

            //these goals are how many units away from the center of the box we want the centers of the block with minimum values to end up at
            float xGoal = (tWidth - 1) / -2.0f;
            float yGoal = (tHeight - 1) / -2.0f;

            //offset is how far the block needs to move to get there (and accordingly how far all blocks need to move to be on target)
            float xOffset = xGoal - xMin;
            float yOffset = yGoal - yMin;

            //calculate the center coords of the box
            float centerX = (cornerX - 7 * blockWidth) + (3 * blockWidth);
            float centerY = cornerY + (3 * blockWidth);

            //draw the blocks around the center of the held box
            for (int lcv = 0; lcv < 4; lcv++)
            {
                drawer.fill(held.colour);
                drawer.rect(centerX + (xOffset + held.blocks[lcv].x) * blockWidth - blockWidth / 2.0f, centerY + (yOffset + held.blocks[lcv].y) * blockWidth - blockWidth / 2.0f, blockWidth, blockWidth);
            }
        }

        //the box that contains the queued tetrominos
        drawer.fill(120, 120, 120);
        drawer.strokeWeight(4);
        drawer.rect(cornerX + 11 * blockWidth, cornerY, 6 * blockWidth, 18 * blockWidth);
        drawer.strokeWeight(1);

        //the queued tetrominos
        for (int loop = 0; loop < 3; loop++)
        {
            Tetromino queued = queue[loop];

            //calculate width and height of the tetromino
            int xMin = queued.blocks[0].x;
            int xMax = xMin;
            int yMin = queued.blocks[0].y;
            int yMax = yMin;
            for (int lcv = 1; lcv < 4; lcv++)
            {
                int x = queued.blocks[lcv].x;
                int y = queued.blocks[lcv].y;

                if (x < xMin)
                {
                    xMin = x;
                }
                else if (x > xMax)
                {
                    xMax = x;
                }
                if (y < yMin)
                {
                    yMin = y;
                }
                else if (y > yMax)
                {
                    yMax = y;
                }
            }

            int tWidth = xMax - xMin + 1;
            int tHeight = yMax - yMin + 1;

            //these goals are how many units away from the center of the box we want the centers of the block with minimum values to end up at
            float xGoal = (tWidth - 1) / -2.0f;
            float yGoal = (tHeight - 1) / -2.0f;

            //offset is how far the block needs to move to get there (and accordingly how far all blocks need to move to be on target)
            float xOffset = xGoal - xMin;
            float yOffset = yGoal - yMin;

            //calculate the center coords of the box
            float centerX = (cornerX - 7 * blockWidth) + (3 * blockWidth) + 18 * blockWidth;
            float centerY = cornerY + (3 * blockWidth) + 6 * blockWidth * loop;

            //draw the blocks around the center of the held box
            for (int lcv = 0; lcv < 4; lcv++)
            {
                drawer.fill(queued.blocks[lcv].colour);
                drawer.rect(centerX + (xOffset + queued.blocks[lcv].x) * blockWidth - blockWidth / 2.0f, centerY + (yOffset + queued.blocks[lcv].y) * blockWidth - blockWidth / 2.0f, blockWidth, blockWidth);
            }
        }

        ScoreKeeper.getScoreKeeper().render(drawer);

        if (paused)
        {
            drawer.drawTextWithBorder("Paused", Main.screenWidth / 2 - blockWidth * 8.5f, Main.screenHeight / 2 - blockWidth * 0, 150, new Color(248, 255, 48), new Color(0, 0, 0));
        }
    }

    public int getLevel()
    {
        return level;
    }
}
