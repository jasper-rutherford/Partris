/**
 Final Project 5611
 Tetromino Class
 Represents one Tetromino
 
 Written by Jasper Rutherford
 */

public class Tetromino {
  public int shape;
  public int rotation;
  public String type;

  public Color colour;
  public Block blocks[];
  public Particle particles[][];

  //the x,y coords of the central block of the piece
  public int offsetX;
  public int offsetY;

  //creates a tetromino with a random shape and rotation
  public Tetromino() {
    //get random shape and rotation
    shape = grid.pickShape();         //pickShape uses 7 bag
    rotation = int(random(0, 4));

    //sets up the tetromino with the random shape and rotation, plus random type
    type = grid.pickType();    //pickType uses the same kind of randomness as pickShape

    setupTetromino(shape, rotation, type);
  }

  //creates a tetromino with defined shape and rotation
  public Tetromino(int shape, int rotation) {

    //sets up the tetromino with the given shape and rotation, plus random type
    type = grid.pickType();    //pickType uses the same kind of randomness as pickShape

    setupTetromino(shape, rotation, type);
  }

  public Tetromino(Tetromino tetromino) {
    shape = tetromino.shape;
    rotation = tetromino.rotation;
    type = tetromino.type;
    colour  = tetromino.colour.copy();

    //instantiate blocks
    blocks = new Block[4];

    //copy in relevant blocks from the template
    copyTemplate();

    offsetX = tetromino.offsetX;
    offsetY = tetromino.offsetY;

    if (collision(0, 0)) {
      lose();
    }
  }  

  public void setupTetromino(int shape, int rotation, String type) {

    //instantiate blocks
    blocks = new Block[4];

    //set shape and rotation
    this.shape = shape;
    this.rotation = rotation;
    this.type = type;

    //set colour according to particle type
    colour = grid.typeColors.get(type).copy();

    //copy in relevant blocks from the template
    copyTemplate();

    //x offset is at about the center of the screen
    offsetX = (gridWidth - 1) / 2;

    //y offset is the negative of the lowest y value in the piece's blocks
    offsetY = blocks[0].y;
    for (int lcv = 1; lcv < 4; lcv++) {
      int y = blocks[lcv].y;
      if (y < offsetY) {
        offsetY = y;
      }
    }
    offsetY *= -1;

    if (collision(0, 0)) {
      lose();
    }
  }

  public void left() {
    //if a downward movement wouldn't collide with anything
    if (!grid.tetromino.collision(-1, 0))
    {
      //move the block down one row
      grid.tetromino.offsetX--;
    }
  }

  public void right() {
    //if a downward movement wouldn't collide with anything
    if (!collision(1, 0))
    {
      //move the block down one row
      offsetX++;
    }
  }

  public void down() {
    //if a downward movement wouldn't collide with anything
    if (!collision(0, 1))
    {
      //move the block down one row
      offsetY++;
    }
    //if there would be a collision
    else {
      //put the block on the grid and create a new tetromino
      place();
      grid.checkRows();

      //get the next tetromino
      grid.newTetromino();
    }
  }

  public void slam(boolean place) {

    //drop tetromino as far as it will go
    boolean slamming = true;
    while (slamming) {
      //if a downward movement wouldn't collide with anything
      if (!collision(0, 1))
      {
        //move the block down one row
        offsetY++;
      }
      //otherwise the block has descended as far as it will go
      else {
        slamming = false;
      }
    }

    //if place is true
    if (place) {
      //put the block on the grid and create a new tetromino
      place();
      grid.checkRows();

      //get the next tetromino
      grid.newTetromino();
    }
  }

  public void rotateLeft() {
  }

  //one 90 degree clockwise rotation
  public void rotateRight() {

    //create new tetromino with rotation from current location
    Tetromino rotated = new Tetromino(shape, (rotation + 1) % 4);
    rotated.type = type;
    rotated.colour = colour;
    rotated.offsetX = offsetX;
    rotated.offsetY = offsetY;

    //if that new piece has no collisions
    if (!rotated.collision(0, 0)) {
      //set it to be the current piece
      grid.tetromino = rotated;
    }
    //otherwise check for wall kicks
    else {
      boolean leftCollide = rotated.collision(-1, 0);
      boolean rightCollide = rotated.collision(1, 0);
      boolean upCollide = rotated.collision(0, -1);
      boolean downCollide = rotated.collision(0, 1);

      //if only left kick works, then do a left kick and set the rotated kicked piece to be the current piece
      if (!leftCollide && rightCollide) {
        rotated.offsetX--;
        grid.tetromino = rotated;
      }
      //if only right kick works then do a right kick and set the rotated kicked piece to be the current piece
      else if (leftCollide && !rightCollide) {
        rotated.offsetX++;
        grid.tetromino = rotated;
      }
      //if only up kick works and down doesnt yada ya you get it
      else if (!upCollide && downCollide) {
        rotated.offsetY--;
        grid.tetromino = rotated;
      }
      //down kick
      else if (upCollide && !downCollide) {
        rotated.offsetY++;
        grid.tetromino = rotated;
      }
    }

    copyTemplate();
  }

  //fills the block's space on the grid with particles
  void place() {
    //loop through each block in the tetromino
    for (int lcv = 0; lcv < 4; lcv++) {
      Block block = blocks[lcv];
      //generate particles
      for (int particleX = 0; particleX < particlesPerEdge; particleX++) {
        for (int particleY = 0; particleY < particlesPerEdge; particleY++) {
          int xIndex = int((block.x + offsetX) * particlesPerEdge + particleX);
          int yIndex = int((block.y + offsetY) * particlesPerEdge + particleY);

          Particle particle = new Particle(type, xIndex, yIndex);
          grid.particleGrid[xIndex][yIndex] = particle; 
          grid.particleList.add(particle);
        }
      }
    }

    grid.updateBlockStats();
    shuffleParticles();
  }
  
  public void setType(String type) {
   this.type = type;
   this.colour = grid.typeColors.get(type).copy();
  }

  //checks if the tetromino would collide with anything if it moved according to the given x, y
  boolean collision(int x, int y) {
    boolean out = false;

    //check all blocks in the tetromino
    for (int lcv = 0; lcv < 4; lcv++) {
      Block block = blocks[lcv];
      int netX = block.x + x + offsetX;
      int netY = block.y + y + offsetY;

      //checks for if the given block is outside the grid
      if (netX < 0 || netY < 0 || netX >= gridWidth || netY >= gridHeight) {
        out = true;
      }
      // or inside the grid but overlapping another block
      else if (netX >= 0 && netY >= 0 && netX < gridWidth && netY < gridHeight && grid.blocks[netX][netY].active) {
        out = true;
      }
    }

    return out;
  }

  //copies relevant blocks from template
  void copyTemplate() {
    for (int lcv = 0; lcv < 4; lcv++) {
      Block block = templates[shape][rotation][lcv];
      blocks[lcv] = b(block.x, block.y);
      blocks[lcv].colour = colour;
    }
  }

  public void render() {
    fill(colour);
    for (int lcv = 0; lcv < 4; lcv++) {
      Block block = blocks[lcv];
      rect(grid.cornerX + (block.x + offsetX) * blockWidth, grid.cornerY + (block.y + offsetY) * blockWidth, blockWidth, blockWidth);
    }
  }
}
