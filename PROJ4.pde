//use sprites instead of dots/squares?

//fix I kicks

//make all the methods public for consistency



/**
 Final Project 5611
 Main File
 Sets everything up and runs everything
 
 Written by Jasper Rutherford
 */

int blockWidth = 30;
int gridWidth = 10;
int gridHeight = 20;

int particlesPerEdge = 10;
int particlesPerBlock = particlesPerEdge * particlesPerEdge;
float particleWidth = int(blockWidth / particlesPerEdge);

//how long in milliseconds that a block takes to fall one row
float blockTime = 1000;
float particleTime = 333;

long oldDropTime;
long oldParticleTime;
int particleUpdate;

int baseFuel = 500;

Grid grid;

Block templates[][][] = new Block[7][4][4];

boolean debug = true;
boolean checkingRows = true;
boolean alphaSleep = false;
boolean autoParticle = true;
boolean autoFall = true;

boolean lost;

long lastTime = System.nanoTime();
double amountOfTicks = 30.0;
double ns = 1000000000 / amountOfTicks;
double delta = 0;
long timer = System.currentTimeMillis(); 

ArrayList<String> allTypes;


void setup() {
  size(900, 800);
  surface.setTitle("Partris");
  lost = false;

  //fill the template array with all piece shapes and rotations
  fillTemplates();

  allTypes = new ArrayList<String>();
  allTypes.add("Fire");
  allTypes.add("Water");
  allTypes.add("Plant");

  //build the grid
  grid = new Grid();
  grid.setupParticleStuff();
  grid.setupTetrominos();

  //set oldTimes to the current time
  oldDropTime = System.currentTimeMillis();
  oldParticleTime = System.currentTimeMillis();
}

void lose() {
  if (!lost) {
    lost = true;
    System.out.println("lost");
  }
}

void draw() {
  long now = System.nanoTime();
  delta += (now - lastTime) / ns;
  lastTime = now;
  while (delta >= 1)
  {
    if (autoParticle) {
      grid.updateParticles();
    }
    delta--;
  }
  long currTime = System.currentTimeMillis();

  //if (!lost && currTime - oldDropTime >= particleTime) {
  //  //update the particles roughly the same amount of times per second
  //  grid.updateParticles();
  //}

  //lower the block once a second (TODO: make this speed up over time)
  if (!lost && currTime - oldDropTime >= blockTime) {
    oldDropTime = currTime;

    if (autoFall) {
      grid.tetromino.down();
    }
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
  //\ advances to next shape (if debug mode is enabled)
  if (debug && key == '\\') {
    int index = allTypes.indexOf(grid.tetromino.type);
    grid.tetromino.setType(allTypes.get((index + 1) % allTypes.size()));
    grid.tetromino.copyTemplate();
  }

  //d toggles whether rows clear (if debug mode is enabled)
  if (debug && key == 'd' || key == 'D') {
    checkingRows = !checkingRows;
    println("toggled rowCheck", checkingRows);
  }
  //f toggles whether blocks fall automatically (if debug mode is enabled)
  if (debug && key == 'f' || key == 'F') {
    autoFall = !autoFall;
    println("toggled autoFall", autoFall);
  }

  //x toggles whether particles update automatically (if debug mode is enabled)
  if (debug && key == 'x' || key == 'X') {
    autoParticle = !autoParticle;
    println("toggled autoParticle", autoParticle);
  }
  //z manually updates all the particles one tick, only if autoParticle is off (if debug mode is enabled)
  if (debug && key == 'z' || key == 'Z' && !autoParticle) {
    grid.updateParticles();
    println("forced a particle update");
  }
  //a toggles whether sleeping particles half reduced alpha (if debug mode is enabled)
  if (debug && key == 'a' || key == 'A') {
    alphaSleep = !alphaSleep;
    println("toggled alphaSleep", alphaSleep);
  }

  //space slams
  if (key == ' ') {
    grid.tetromino.slam(true);
    //set oldTime to the current time
    oldDropTime = System.currentTimeMillis();
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
    //set oldTime to the current time
    oldDropTime = System.currentTimeMillis();
  }
}

//lets me send in a Color to fill without any messing around
void fill(Color colour) {
  fill(colour.r, colour.g, colour.b, colour.a);
}

//randomizes the order of all the particles in the particleList
void shuffleParticles() {
  ArrayList<Particle> pList = grid.particleList;
  ArrayList<Particle> temp = new ArrayList<Particle>();
  int size = pList.size();
  for (int lcv = 0; lcv < size; lcv++) {
    int index = int(random(0, pList.size()));
    temp.add(pList.get(index));
    pList.remove(index);
  }
  grid.particleList = temp;
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
