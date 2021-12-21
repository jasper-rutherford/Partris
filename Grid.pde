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
  public HashMap<String, Color> colorMap;

  public ArrayList<Particle> particleList;

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
    colorMap = new HashMap<String, Color>();
    fillShapes();
    fillTypes();
    fillTypeColors();

    //fill the grid with inactive blocks
    blocks = new Block[gridWidth][gridHeight];
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        blocks[x][y] = new Block(x, y);
      }
    }
  }

  public void setupParticleStuff() {
    //initialize the particleGrid and particleList
    particleGrid = new Particle[gridWidth * particlesPerEdge][gridHeight * particlesPerEdge];
    particleList = new ArrayList<Particle>();

    //all particles default to air
    for (int x = 0; x < gridWidth * particlesPerEdge; x++) {
      for (int y = 0; y < gridHeight * particlesPerEdge; y++) {
        particleGrid[x][y] = new Particle("Air", x, y);
        particleList.add(particleGrid[x][y]);
      }
    }
    shuffleParticles();
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
  public void updateParticles() {
    //int numNonAir = 0;
    //update each particle's location
    for (int lcv = 0; lcv < particleList.size(); lcv++) {
      particleList.get(lcv).move();
    }
    //have all the particles interact
    for (int lcv = 0; lcv < particleList.size(); lcv++) {
      particleList.get(lcv).interact();
    }
    ////count non air particles
    //for (int x = 0; x < particlesPerEdge * gridWidth; x++) {
    //  for (int y = 0; y < particlesPerEdge * gridHeight; y++) {
    //    if (!particleGrid[x][y].type.equals("Air")) {
    //      numNonAir++;
    //    }
    //  }
    //}
    updateBlockStats();
    grid.checkRows();
    //if (lost) {
    //  if (numNonAir == 0) {
    //    println("Lost");
    //    babooska = true;
    //  } else {
    //  }
    //}
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
      for (int lcv = 0; lcv < types.size(); lcv++) {
      }
    }

    int index = int(random(0, types.size()));
    String type = types.get(index);
    types.remove(index);
    return type;
  }

  //map the types to their respective colors
  void fillTypeColors() {
    colorMap.put("Air", new Color(0, 0, 0, 0));
    colorMap.put("Fire", new Color(235, 64, 52));
    colorMap.put("Water", new Color(66, 135, 245));
    colorMap.put("Plant", new Color(50, 168, 82));
    colorMap.put("Lava", new Color(214, 111, 32));
    colorMap.put("Ice", new Color(150, 183, 235));
    colorMap.put("Stone", new Color(112, 112, 112));
    colorMap.put("Charcoal", new Color(43, 43, 43));
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
          p.setType("Air");
        }
      }
    }

    //increase the score
    //every particle you clear is 5 points
    score += numParticles * 5;
    //lower any rows above the newly cleared row

    //bottom up means that empty rows move upward until they are gone
    //does not bother with row 0 because there is nothing to lower into that row
    for (int y = row; y > 0; y--) {

      //copy the above row into this row and clear the above row
      for (int px = 0; px < gridWidth * particlesPerEdge; px++) {
        for (int py = y * particlesPerEdge; py < y * particlesPerEdge + particlesPerEdge; py++) {
          //copy above row to lower
          Particle above = particleGrid[px][py - particlesPerEdge];
          Particle curr = particleGrid[px][py];

          String aboveType = above.type;
          int aboveFuel = above.fuel;

          above.setType(curr.type);
          above.fuel = curr.fuel;

          curr.setType(aboveType);
          curr.fuel = aboveFuel;
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

      if (tetromino.collision(0, 0)) {
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

    swapped = false;
  }

  void render() {
    //draw rectangle background
    fill(200, 200, 200);
    rect(cornerX, cornerY, totWidth, totHeight);


    //loop through/render full blocks
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        blocks[x][y].render();
      }
    }

    //render the falling tetromino
    tetromino.render(true);

    ////draw box for tetromino's particle's type
    // fill(120, 120, 120);
    // strokeWeight(4);
    // rect(cornerX, cornerY - blockWidth * 3, blockWidth * 10, blockWidth * 3);
    //strokeWeight(1);

    //draw the ghost block if enabled
    if (ghostBlock) {
      //duplicate the tetromino, but decrease the alpha
      Tetromino ghost = new Tetromino(tetromino);
      ghost.colour.a = 127;

      if (ghost.type.equals("Charcoal")) {
        ghost.colour.a = int(255 * .75);
      }
      //slam the duplicate but don't place it
      ghost.slam(false);

      //particle slam the duplicate as well
      ghost.particleSlam();

      //render the ghost
      ghost.render(false);
    }

    //render the particles
    //for (int x = 0; x < gridWidth * particlesPerEdge; x++) {
    //  for (int y = 0; y < gridHeight * particlesPerEdge; y++) {
    //    Particle particle = grid.particleGrid[x][y];
    //    if (particle != null) {
    //      fill(particle.colour);
    //      rect(x * particleWidth + cornerX, y * particleWidth + cornerY, particleWidth, particleWidth);
    //    }
    //  }
    //}
    for (int lcv = 0; lcv < particleList.size(); lcv++) {
      particleList.get(lcv).render();
      //if (particleList.get(lcv).type.equals("Air")) {
      // particleList.remove(lcv);
      //}

      //Particle particle = particleList.get(lcv);
      //fill(particle.colour);
      //rect(particle.x, particle.y, particleWidth, particleWidth);
    }

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
    textSize(25);
    fill(255, 255, 255);
    float textX = grid.cornerX - blockWidth * 7;
    float textY = grid.cornerY - blockWidth / 2;

    for (int x = -1; x < 2; x++) {
      text("Score: " + score, textX + x, textY);
      text("Score: " + score, textX, textY + x);
    }
    fill(0, 0, 0);
    text("Score: " + score, textX, textY);
  }
}
