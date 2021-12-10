/**
 Final Project 5611
 Grid Class
 Contains all the blocks/tetrominos
 
 Written by Jasper Rutherford
 */

public class Grid {
  //all the blocks in the grid
  public Block blocks[][];

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

  public boolean droplines;

  //default constructor
  public Grid() {
    //fill the grid with inactive blocks
    blocks = new Block[gridWidth][gridHeight];
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        blocks[x][y] = new Block(x, y);
      }
    }

    //calculate pixel width/height of the grid
    totWidth = blockWidth * gridWidth;
    totHeight = blockWidth * gridHeight;

    //calculate coords of top left corner of grid
    cornerX = (width - totWidth) / 2;
    cornerY = (height - totHeight) / 2;

    //swapped defaults to false
    swapped = false;

    //droplines defaults to true
    droplines = true;
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

    //used to track how many rows are cleared (sent into lower rows)
    int numCleared = 0;

    //check every row
    for (int y = 0; y < gridHeight; y++) {

      //check if the row is full
      boolean full = true;
      for (int x = 0; x < gridWidth; x++) {
        //if any block in the row is not active then the row is not full
        if (!blocks[x][y].active) {
          full = false;

          //stop checking the row
          x = gridWidth;
        }
      }

      //if the row is full
      if (full) {
        //clear it
        clearRow(y);

        //track how many rows were cleared
        numCleared++;
      }
    }

    //lower all the rows as needed
    lowerRows(numCleared);
  }

  //clear the row at the given y rank
  void clearRow(int y) {
    for (int x = 0; x < gridWidth; x++) {
      blocks[x][y].active = false;
    }
  }

  //lowers everything into numEmpty empty rows
  void lowerRows(int numEmpty) {

    //loop numEmpty times
    for (int lcv = 0; lcv < numEmpty; lcv++) {

      //bottom up means that empty rows move upward until they are gone
      //does not bother with row 0 because there is nothing to lower into that row
      for (int y = gridHeight - 1; y > 0; y--) {

        //check if the row is empty
        boolean empty = true;
        for (int x = 0; x < gridWidth; x++) {

          //if a block in the row is active, then the row is not empty.
          if (blocks[x][y].active) {

            empty = false;

            //stop checking the row for emptiness
            x = gridWidth;
          }
        }

        //if the row is empty
        if (empty) {

          //copy the above row into this row and clear the above row
          for (int x = 0; x < gridWidth; x++) {
            blocks[x][y].active = blocks[x][y - 1].active;
            blocks[x][y - 1].active = false;
            blocks[x][y].setColour(blocks[x][y - 1].colour);
          }
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
    fill(66, 135, 245);
    rect(cornerX, cornerY, totWidth, totHeight);

    //loop through/render full blocks
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        blocks[x][y].render();
      }
    }

    //render the falling tetromino
    tetromino.render();

    //draw the drop lines if enabled
    if (droplines) {
      int offsetX = tetromino.offsetX;
      int offsetY = tetromino.offsetY;

      int leftX = 5;
      int rightX = -5;

      for (int lcv = 0; lcv < 4; lcv++) {
        int someX = tetromino.blocks[lcv].x;
        if (someX < leftX) {
          leftX = someX;
        }
        if (someX > rightX) {
          rightX = someX;
        }
      }

      int leftY = -5;
      int rightY = -5;
      for (int lcv = 0; lcv < 4; lcv++) {
        int someX = tetromino.blocks[lcv].x;
        int someY = tetromino.blocks[lcv].y;
        if (someX == leftX && someY > leftY) {
          leftY = someY;
        }
        if (someX == rightX && someY > rightY) {
          rightY = someY;
        }
      }

      int leftFloorY = gridHeight;
      int rightFloorY = gridHeight;
      for (int lcv = 0; lcv < gridHeight; lcv++) {
        Block leftBlock = blocks[leftX + offsetX][lcv];
        Block rightBlock = blocks[rightX + offsetX][lcv];

        if (leftBlock.active && leftBlock.y < leftFloorY) {
          leftFloorY = leftBlock.y;
        }
        if (rightBlock.active && rightBlock.y < rightFloorY) {
          rightFloorY = rightBlock.y;
        }
      }

      stroke(255, 220, 122);
      pushMatrix();
      translate(cornerX, cornerY);
      println("offset:", offsetX, offsetY);
      println(leftX, leftY, rightX, rightY);
      line((leftX + offsetX) * blockWidth, (leftY + offsetY + 1) * blockWidth, (leftX + offsetX) * blockWidth, leftFloorY * blockWidth); 
      line((rightX + offsetX + 1) * blockWidth, (rightY + offsetY + 1) * blockWidth, (rightX + offsetX + 1) * blockWidth, rightFloorY * blockWidth); 
      popMatrix();
      stroke(0, 0, 0);
    }

    //draw grid border
    strokeWeight(4);
    noFill();
    rect(cornerX, cornerY, totWidth, totHeight);
    strokeWeight(1);

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
        fill(50, 168, 82);
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
        fill(229, 52, 235);
        rect(centerX + (xOffset + queued.blocks[lcv].x) * blockWidth - blockWidth / 2.0, centerY + (yOffset + queued.blocks[lcv].y) * blockWidth - blockWidth / 2.0, blockWidth, blockWidth);
      }
    }
  }
}
