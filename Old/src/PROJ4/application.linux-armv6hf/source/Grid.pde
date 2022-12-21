/**
 Final Project 5611
 Grid Class
 Contains all the blocks/tetrominos/particles
 
 Written by Jasper Rutherford
 */

public class Grid {
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

  //default constructor
  public Grid() {

    //calculate pixel width/height of the grid
    totWidth = blockWidth * gridWidth;
    totHeight = blockWidth * gridHeight;

    //calculate coords of top left corner of grid
    cornerX = (width - totWidth) / 2;
    cornerY = (height - totHeight) / 2;

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
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        blocks[x][y] = new Block(x, y);
      }
    }

    println("filled grid");
  }

  public void setupParticleStuff() {
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
        Particle particle = particleFactory.generateAirParticle(new Point(x, y));

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
  void shuffleParticleList() 
  {
    println("particle list size before shuffle: " + particleList.size());
    ArrayList<Particle> temp = new ArrayList<Particle>();

    //need to save size because the original list will shrink as the loop progresses
    int size = particleList.size();

    //loop through all particles in the particle list
    for (int lcv = 0; lcv < size; lcv++) 
    {
      //get random particle from the old list
      int index = int(random(0, particleList.size()));
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
    println("particle list size after shuffle: " + particleList.size());
  }

  //refills the shapes list with all shape options
  public void fillShapes() {
    for (int lcv = 0; lcv < 7; lcv++) {
      shapes.add(lcv);
    }
  }

  //picks a random shape from the list, removes it from the list, and returns it
  public int pickShape() {
    if (shapes.size() == 0) {
      fillShapes();
    }

    int index = int(random(0, shapes.size()));
    int shape = shapes.get(index);
    shapes.remove(index);
    return shape;
  }

  //fill the particle types
  void fillTypes() {
    for (int lcv = 0; lcv < allTypes.size(); lcv++) {
      types.add(allTypes.get(lcv));
    }
    println("Filled types");
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
    for (Particle particle : particleList) {

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
  public void updateBlockStats() {
    //updates every block's activity and fullness in the grid
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        //a block is active if it is not empty
        boolean active = false;

        //count how many particles are in the block
        int numParticles = 0;

        //check every particle within the block's space
        for (int px = 0; px < particlesPerEdge; px++) {
          for (int py = 0; py < particlesPerEdge; py++) {
            Particle particle = particleGrid[x * particlesPerEdge + px][y * particlesPerEdge + py];
            if (!particle.type.equals("Air")) {
              active = true;
              numParticles++;
            }
          }
        }

        blocks[x][y].active = active;
        blocks[x][y].full = numParticles > particlesPerBlock * fullFactor;
      }
    }
  }

  //picks a random type from the list, removes it from the list, and returns it
  public String pickType() {
    if (types.size() == 0) {
      fillTypes();
    }

    int index = int(random(0, types.size()));
    String type = types.get(index);
    types.remove(index);
    return type;
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
    Particle newParticle = particleFactory.generateParticle(newType, oldParticle);

    //replace the old particle with the new particle
    replaceParticle(oldParticle, newParticle);
  }

  //sets up the tetrominos. Used immediately after grid is constructed. Cannot just be part of the constructor because tetrominos need to access grid's blocks and I don't want to send them into tetromino's constructor every time.
  void setupTetrominos() {
    //build the initial current tetromino
    tetromino = new Tetromino();

    //fill the queue with random tetrominos
    queue = new Tetromino[3];
    for (int lcv = 0; lcv < 3; lcv++) {
      queue[lcv] = new Tetromino();
    }

    //set the held tetromino to null
    held = null;
  }

  //checks for any full rows, clears them, and moves everything downward
  void checkRows() {
    if (checkingRows) {
      //check every row
      for (int y = 0; y < gridHeight; y++) {

        //check if the row is full
        boolean full = true;
        for (int x = 0; x < gridWidth; x++) {
          //if any block in the row is not full then the row is not full
          if (!blocks[x][y].full) {
            full = false;
          }
        }

        //if the row is full
        if (full) {
          //clear it
          clearRow(y);
        }
      }

      grid.updateBlockStats();
    }
  }

  //clear the row at the given row
  void clearRow(int row) {
    int numParticles = 0;
    //clear the row
    for (int px = 0; px < gridWidth * particlesPerEdge; px++) {
      for (int py = row * particlesPerEdge; py < row * particlesPerEdge + particlesPerEdge; py++) {
        Particle p = particleGrid[px][py];

        //if the particle is not air
        if (!p.type.equals("Air")) {
          //increment the number of particles and set the particle to air
          numParticles++;

          //replace that particle with a new air particle
          grid.replaceParticle(p, "Air");
        }
      }
    }

    //add points
    addPoints(numParticles * pointsPerParticleCleared);

    //lower any rows above the newly cleared row

    //bottom up means that empty rows move upward until they are gone
    //does not bother with row 0 because there is nothing to lower into that row
    for (int y = row; y > 0; y--) {

      //swap this row with the row above it
      for (int px = 0; px < gridWidth * particlesPerEdge; px++) {
        for (int py = y * particlesPerEdge; py < y * particlesPerEdge + particlesPerEdge; py++) {
          //copy above row to lower
          Particle above = particleGrid[px][py - particlesPerEdge];
          Particle curr = particleGrid[px][py];

          grid.swapParticles(above, curr);
        }
      }
    }
  }



  //attempts to hold the current piece - cannot swap if the piece was just swapped out
  void hold() {
    //if this is the first to swap
    if (held == null) {
      //just move it into the held slot and get a new tetromino as usual
      held = tetromino;
      newTetromino();
      swapped = true;
    }
    //otherwise swap the tetrominos
    else if (!swapped) {
      Tetromino temp = tetromino;
      tetromino = held;
      held = temp;
      swapped = true;

      //reset held's offset

      //x offset is at about the center of the screen
      tetromino.offsetX = (gridWidth - 1) / 2;

      //y offset is the negative of the lowest y value in the piece's blocks
      tetromino.offsetY = tetromino.blocks[0].y;
      for (int lcv = 1; lcv < 4; lcv++) {
        int y = tetromino.blocks[lcv].y;
        if (y < tetromino.offsetY) {
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
  void newTetromino() {
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

  void render() 
  {
    //render drop speed
    drawTextWithBorder("BPS: " + (ticksPerSecond / ticksPerBlockDrop ), grid.cornerX - blockWidth * 6, grid.cornerY + blockWidth * 7, 20, new Color(255, 255, 255), new Color(0, 0, 0));

    //draw rectangle background
    fill(200, 200, 200);
    rect(cornerX, cornerY, totWidth, totHeight);

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
    tetromino.render(true);

    //draw the ghost block if enabled
    if (ghostBlock) {
      //duplicate the tetromino, but decrease the alpha
      Tetromino ghost = new Tetromino(tetromino);
      ghost.colour.a = 127;
      // ghost.stroke = particleFactory.getColour(tetromino.type);

      if (ghost.type.equals("Charcoal")) {
        ghost.colour.a = int(255 * .75);
      }
      //slam the duplicate but don't place it
      ghost.slam(false);

      //particle slam the duplicate as well
      ghost.particleSlam();

      //render the ghost
      ghost.render(true);
    }

    //render the particles
    for (Particle[] line : particleGrid)
    {
      for (Particle particle : line)
      {
        particle.render();
      }
    }

    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        blocks[x][y].render();
      }
    }
    stroke(0, 0, 0);

    //draw grid border
    strokeWeight(4);
    noFill();
    if (debug) {
      stroke(255, 0, 0);
    }
    rect(cornerX, cornerY, totWidth, totHeight);
    strokeWeight(1);
    if (debug) {
      stroke(0, 0, 0);
    }

    //the held box
    fill(120, 120, 120);
    strokeWeight(4);
    rect(cornerX - 7 * blockWidth, cornerY, 6 * blockWidth, 6 * blockWidth);
    strokeWeight(1);

    //the held tetromino
    if (grid.held != null) {

      //calculate width and height of the tetromino
      int xMin = grid.held.blocks[0].x;
      int xMax = xMin;
      int yMin = grid.held.blocks[0].y;
      int yMax = yMin;
      for (int lcv = 1; lcv < 4; lcv++) {
        int x = grid.held.blocks[lcv].x;
        int y = grid.held.blocks[lcv].y;

        if (x < xMin) {
          xMin = x;
        } else if (x > xMax) {
          xMax = x;
        }
        if (y < yMin) {
          yMin = y;
        } else if (y > yMax) {
          yMax = y;
        }
      }

      int tWidth = xMax - xMin + 1;
      int tHeight = yMax - yMin + 1;

      //these goals are how many units away from the center of the box we want the centers of the block with minimum values to end up at
      float xGoal = (tWidth - 1) / -2.0;
      float yGoal = (tHeight - 1) / -2.0;

      //offset is how far the block needs to move to get there (and accordingly how far all blocks need to move to be on target)
      float xOffset = xGoal - xMin;
      float yOffset = yGoal - yMin;

      //calculate the center coords of the box
      float centerX = (cornerX - 7 * blockWidth) + (3 * blockWidth);
      float centerY = cornerY + (3 * blockWidth);

      //draw the blocks around the center of the held box
      for (int lcv = 0; lcv < 4; lcv++) {
        fill(held.colour);
        rect(centerX + (xOffset + held.blocks[lcv].x) * blockWidth - blockWidth / 2.0, centerY + (yOffset + held.blocks[lcv].y) * blockWidth - blockWidth / 2.0, blockWidth, blockWidth);
      }
    }

    //the box that contains the queued tetrominos
    fill(120, 120, 120);
    strokeWeight(4);
    rect(cornerX + 11 * blockWidth, cornerY, 6 * blockWidth, 18 * blockWidth);
    strokeWeight(1);

    //the queued tetrominos
    for (int loop = 0; loop < 3; loop++) {
      Tetromino queued = queue[loop];

      //calculate width and height of the tetromino
      int xMin = queued.blocks[0].x;
      int xMax = xMin;
      int yMin = queued.blocks[0].y;
      int yMax = yMin;
      for (int lcv = 1; lcv < 4; lcv++) {
        int x = queued.blocks[lcv].x;
        int y = queued.blocks[lcv].y;

        if (x < xMin) {
          xMin = x;
        } else if (x > xMax) {
          xMax = x;
        }
        if (y < yMin) {
          yMin = y;
        } else if (y > yMax) {
          yMax = y;
        }
      }

      int tWidth = xMax - xMin + 1;
      int tHeight = yMax - yMin + 1;

      //these goals are how many units away from the center of the box we want the centers of the block with minimum values to end up at
      float xGoal = (tWidth - 1) / -2.0;
      float yGoal = (tHeight - 1) / -2.0;

      //offset is how far the block needs to move to get there (and accordingly how far all blocks need to move to be on target)
      float xOffset = xGoal - xMin;
      float yOffset = yGoal - yMin;

      //calculate the center coords of the box
      float centerX = (cornerX - 7 * blockWidth) + (3 * blockWidth) + 18 * blockWidth;
      float centerY = cornerY + (3 * blockWidth) + 6 * blockWidth * loop;

      //draw the blocks around the center of the held box
      for (int lcv = 0; lcv < 4; lcv++) {
        fill(queued.blocks[lcv].colour);
        rect(centerX + (xOffset + queued.blocks[lcv].x) * blockWidth - blockWidth / 2.0, centerY + (yOffset + queued.blocks[lcv].y) * blockWidth - blockWidth / 2.0, blockWidth, blockWidth);
      }
    }

    //render the score
    drawTextWithBorder("Score: " + score, grid.cornerX - blockWidth * 7, grid.cornerY - blockWidth / 4, 25, new Color(255, 255, 255), new Color(0, 0, 0));

    //render the level counter
    drawTextWithBorder("Level: " + level, grid.cornerX + blockWidth * 11, grid.cornerY - blockWidth * 1.25, 25, new Color(255, 255, 255), new Color(0, 0, 0));

    //render level points bar
    fill(new Color(199, 199, 199));
    rect(cornerX + 11.5 * blockWidth, cornerY - blockWidth * 5 / 6, 5 * blockWidth, 2.0 / 3 * blockWidth);
    fill(new Color(61, 132, 245));
    rect(cornerX + 11.5 * blockWidth, cornerY - blockWidth * 5 / 6, 5 * blockWidth * levelPoints / levelPointsPerLevel, 2.0 / 3 * blockWidth);

    //render the level points
    drawTextWithBorder(levelPoints + "/" + levelPointsPerLevel, grid.cornerX + blockWidth * 12, grid.cornerY - blockWidth / 3.7, 15, new Color(255, 255, 255), new Color(0, 0, 0));  
    drawTextWithBorder("(" + int(levelPoints * 100 / levelPointsPerLevel) + "%)", grid.cornerX + blockWidth * (12 + 4.75), grid.cornerY - blockWidth / 3.7, 15, new Color(255, 255, 255), new Color(0, 0, 0));  
  }
}
