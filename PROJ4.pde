//use sprites instead of dots/squares?






/**
 Final Project 5611
 Main File
 Sets everything up and runs everything
 
 Written by Jasper Rutherford
 */

float blockWidth = 30;
int gridWidth = 10;
int gridHeight = 20;

//how long in milliseconds that a block takes to fall one row
float droptime = 1000;

long oldTime;

Grid grid;

Block templates[][][] = new Block[7][4][4];

boolean debug = true;

boolean lost;

void setup() {
  size(900, 800);
  lost = false;

  //fill the template array with all piece shapes and rotations
  fillTemplates();

  //build the grid
  grid = new Grid();
  grid.setupTetrominos();

  //set oldTime to the current time
  oldTime = System.currentTimeMillis();
}

void lose() {
  lost = true;
  System.out.println("lost");
}

void draw() {
  long currTime = System.currentTimeMillis();

  if (!lost && currTime - oldTime >= droptime) {
    oldTime = currTime;

    grid.tetromino.down();
  }

  background(60, 60, 60);
  grid.render();
}

//key controls
void keyPressed() {

  //c holds the current piece
  if (key == 'c' || key == 'C') {
    grid.hold();
  }

  //r restarts
  if (key == 'r' || key == 'R') {
    setup();
  }

  //enter advances to next shape (if debug mode is enabled)
  if (debug && key == '\n') {
    grid.tetromino.shape = (grid.tetromino.shape + 1) % 7;
    grid.tetromino.copyTemplate();
  }

  //space slams
  if (key == ' ') {
    grid.tetromino.slam(true);
  }

  //rotates the tetromino 90 degrees clockwise
  if (keyCode == UP) {
    grid.tetromino.rotateRight();
  }
  //moves the tetromino left one block
  if (keyCode == LEFT) {
    grid.tetromino.left();
  }
  //moves the tetromino right one block
  if (keyCode == RIGHT) {
    grid.tetromino.right();
  }
  //moves the tetromino down one block
  if (keyCode == DOWN) {
    grid.tetromino.down();
  }
}


/*
  Sets up the tetromino templates
 */

//shortcut to create a block
Block b(int x, int y) {
  return new Block(x, y);
}

//sends in the four given blocks to the template array at once
void pack(int shape, int rotation, Block a, Block b, Block c, Block d) {
  Block[] packed = {a, b, c, d};
  templates[shape][rotation] = packed;
  //templates[shape][rotation][0] = a;
  //templates[shape][rotation][1] = b;
  //templates[shape][rotation][2] = c;
  //templates[shape][rotation][3] = d;
}

//all the templates for all the tetrominos and all their rotations. Each increment of the rotation index is a 90 degree clockwise rotation with arbitrary starting positions.
//tetrominos are in alphabetical order (IJLOSTZ -> 0123456)
void fillTemplates() {
  //I
  pack(0, 0, b(0, 0), b( 0, -1), b(0, -2), b( 0, 1));
  pack(0, 1, b(-1, -1), b(0, -1), b(1, -1), b(2, -1));
  pack(0, 2, b(1, -2), b( 1, -1), b( 1, 0), b( 1, 1));
  pack(0, 3, b(0, 0), b( -1, 0), b( 1, 0), b( 2, 0));

  //J
  pack(1, 0, b(0, 0), b( 0, 1), b(-1, 1), b( 0, -1));
  pack(1, 1, b(0, 0), b(-1, 0), b(-1, -1), b( 1, 0));
  pack(1, 2, b(0, 0), b( 0, -1), b( 1, -1), b( 0, 1));
  pack(1, 3, b(0, 0), b( 1, 0), b( 1, 1), b( -1, 0));

  //L
  pack(2, 0, b(0, 0), b( 0, 1), b( 1, 1), b( 0, -1));
  pack(2, 1, b(0, 0), b( -1, 0), b( 1, 0), b( -1, 1));
  pack(2, 2, b(0, 0), b( 0, -1), b( -1, -1), b( 0, 1));
  pack(2, 3, b(0, 0), b( 1, 0), b(-1, 0), b(1, -1));

  //O
  pack(3, 0, b(0, 0), b( 0, -1), b( 1, -1), b( 1, 0));
  pack(3, 1, b(0, 0), b( 0, -1), b( 1, -1), b( 1, 0));
  pack(3, 2, b(0, 0), b( 0, -1), b( 1, -1), b( 1, 0));
  pack(3, 3, b(0, 0), b( 0, -1), b( 1, -1), b( 1, 0));
  //pack(3, 1, b(0, 0), b( 1, 0), b( 1, 1), b( 0, 1));
  //pack(3, 2, b(0, 0), b( 0, 1), b(-1, 1), b(-1, 0));
  //pack(3, 3, b(0, 0), b(-1, 0), b(-1, -1), b( 0, -1));

  //S
  pack(4, 0, b(0, 0), b( 1, 0), b( 0, 1), b( -1, 1));
  pack(4, 1, b(0, 0), b( 0, 1), b( -1, 0), b(-1, -1));
  pack(4, 2, b(0, 0), b( -1, 0), b(0, -1), b( 1, -1));
  pack(4, 3, b(0, 0), b(0, -1), b( 1, 0), b(1, 1));

  //T
  pack(5, 0, b(0, 0), b(-1, 0), b( 1, 0), b( 0, 1));
  pack(5, 1, b(0, 0), b( 0, -1), b( 0, 1), b(-1, 0));
  pack(5, 2, b(0, 0), b( 1, 0), b(-1, 0), b( 0, -1));
  pack(5, 3, b(0, 0), b( 0, 1), b( 0, -1), b( 1, 0));

  //Z
  pack(6, 0, b(0, 0), b(-1, 0), b( 0, 1), b( 1, 1));
  pack(6, 1, b(0, 0), b( 0, -1), b(-1, 0), b(-1, 1));
  pack(6, 2, b(0, 0), b( 1, 0), b( 0, -1), b(-1, -1));
  pack(6, 3, b(0, 0), b( 0, 1), b( 1, 0), b( 1, -1));
}
