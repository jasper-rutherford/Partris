//use sprites instead of dots/squares?

//fix I kicks

//make all the methods public for consistency



/**
 Final Project 5611
 Main File
 Sets everything up and runs everything
 
 Written by Jasper Rutherford
 */

float version = 1.4;

int blockWidth = 30;
int gridWidth = 10;
int gridHeight = 20;

int particlesPerEdge = 10;
int particlesPerBlock = particlesPerEdge * particlesPerEdge;
float particleWidth = blockWidth / float(particlesPerEdge);

float fullFactor = 3.0/5;

//how long in milliseconds that a block takes to fall one row
// float baseBlockTime = 1000;
// float blockTime = baseBlockTime;
// float baseParticleTicksPerSecond = 30.0;

//////////////////
int level;
int levelPoints;
int plantBurnPoints = 1;
int charcoalBurnPoints = 2;
int dissolvePoints = 1;
int pointsPerParticleCleared = 5;
int levelPointsPerLevel = gridWidth * particlesPerEdge * particlesPerEdge * pointsPerParticleCleared * 10;
float[] levelSpeeds = {48, 43, 38, 33, 28, 23, 18, 13, 8, 6, 5, 5, 5, 4, 4, 4, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1};//, 30, 27, 24, 21, 18, 15, 12, 9, 8, 7, 6, 5, 4, 3, 2, 1};//{24, 22, 19, 17, 14, 12, 9, 7, 4, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 1}; //represents how many ticks per block drop  

float ticksPerSecond = 60.0;
float ticksPerBlockDrop = levelSpeeds[0];
float particleUpdatesPerBlockDrop = 30.0;

double nanosPerBlockDrop = 1000000000 / ticksPerSecond * ticksPerBlockDrop;
double nanosPerParticleUpdate = nanosPerBlockDrop / particleUpdatesPerBlockDrop;

double blockDropsDueForCompletion = 0;
double particleUpdatesDueForCompletion = 0;

long prevTime = System.nanoTime();

// long oldDropTime;
// long oldParticleTime;
// long startTime;
// int particleUpdate;




ParticleFactory particleFactory;

Grid grid;

Block templates[][][] = new Block[7][4][4];

ArrayList<Integer> inputs = new ArrayList<Integer>();
ArrayList<Integer> konami = new ArrayList<Integer>();
boolean debug = false;
boolean checkingRows = true;
boolean alphaSleep = false;
boolean autoParticle = true;
boolean autoFall = true;
boolean numRowFunc = false;
boolean renderParticleWideNeighbors = false;

boolean paused = false;
boolean lost;

// long lastTime = System.nanoTime();
// double amountOfTicks = baseParticleTicksPerSecond;
// double ns = 1000000000 / amountOfTicks;
// double delta = 0;
// long timer = System.currentTimeMillis(); 

ArrayList<String> allTypes;

int score;


void setup() {
  size(900, 800);
  surface.setTitle("Partris v" + version);
  lost = false;

  //create the particle factory
  particleFactory = new ParticleFactory();

  println("test1");

  //fill the template array with all piece shapes and rotations
  fillTemplates();

  //not including air because this is used to decide what type a tetromino can be
  allTypes = new ArrayList<String>();
  allTypes.add("Acid");
  allTypes.add("Charcoal");
  allTypes.add("Fire");
  allTypes.add("Goop");
  allTypes.add("Ice");
  allTypes.add("Lava");
  allTypes.add("Plant");
  allTypes.add("Stone");
  allTypes.add("Water");

  //build the grid
  grid = new Grid();
  grid.setupParticleStuff();
  println("setup particle stuff");

  grid.setupTetrominos();

  println("setup tetrominos");

  //set oldTimes to the current time
  prevTime = System.nanoTime();

  if (konami.size() == 0) {
    //setup konami
    inputs.add(38);
    konami.add(38);
    inputs.add(38);
    konami.add(38);
    inputs.add(40);
    konami.add(40);
    inputs.add(40);
    konami.add(40);
    inputs.add(37);
    konami.add(37);
    inputs.add(39);
    konami.add(39);
    inputs.add(37);
    konami.add(37);
    inputs.add(39);
    konami.add(39);
    inputs.add(66);
    konami.add(66);
    inputs.add(65);
    konami.add(65);
  }

  score = 0;
  levelPoints = 0;


  setLevel(0);
}

void lose() {
  if (!lost) {
    //amountOfTicks = 7;
    //ns = 1000000000 / amountOfTicks;
    lost = true;

    println("lost");
  }
}

void draw() 
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

  background(60, 60, 60);
  grid.render();


  if (paused) {
    drawTextWithBorder("Paused", width / 2 - blockWidth * 8.5, height / 2 - blockWidth * 0, 150, new Color(248, 255, 48), new Color(0, 0, 0));
  }
}    

//key controls
void keyPressed() {

  //enter advances to next shape (if debug mode is enabled)
  if (debug && key == '\n') {
    grid.tetromino.shape = (grid.tetromino.shape + 1) % 7;
    grid.tetromino.copyTemplate();
  }
  //\ advances to next type (if debug mode is enabled)
  if (debug && key == '\\') {
    int index = allTypes.indexOf(grid.tetromino.type);
    grid.tetromino.setType(allTypes.get((index + 1) % allTypes.size()));
    grid.tetromino.copyTemplate();
  }
  //a toggles whether sleeping particles have a border around them (if debug mode is enabled)
  if (debug && (key == 'a' || key == 'A')) {
    alphaSleep = !alphaSleep;
    println("toggled alphaSleep", alphaSleep);
  }
  if (debug && (key == 'b' || key == 'B')) {
    renderParticleWideNeighbors = !renderParticleWideNeighbors;
    println("toggled render wide particles", renderParticleWideNeighbors);
  }
  //c holds the current piece
  if (key == 'c' || key == 'C') {
    grid.hold();
  }
  //d toggles whether rows clear (if debug mode is enabled)
  if (debug && (key == 'd' || key == 'D')) {
    checkingRows = !checkingRows;
    println("toggled rowCheck", checkingRows);
  }
  //f toggles whether blocks fall automatically (if debug mode is enabled)
  if (debug && (key == 'f' || key == 'F')) {
    autoFall = !autoFall;
    println("toggled autoFall", autoFall);
  }
  // toggles ghostBlock (if debug mode is enabled)
  if (debug && (key == 'g' || key == 'G')) {
    grid.ghostBlock = !grid.ghostBlock;
  }
  // L moves the tetromino up (only in debug mode)
  if (debug && (key == 'l' || key == 'L')) {
    grid.tetromino.up();
  }
  // p pauses everything
  if (key == 'p' || key == 'P') {
    paused = !paused;
    println("toggled pause");
  }
  //r restarts
  if (key == 'r' || key == 'R') {
    println("restarting");
    setup();
  }
  //s reshuffles the particlelist (if debug mode is enabled)
  if (debug && (key == 's' || key == 'S'))
  {
    println("shuffling particlelist", autoFall);
    grid.shuffleParticleList();
  }
  //x toggles whether particles update automatically (if debug mode is enabled)
  if (debug && (key == 'x' || key == 'X')) {
    autoParticle = !autoParticle;
    println("toggled autoParticle", autoParticle);
  }
  //z manually updates all the particles one tick, only if autoParticle is off (if debug mode is enabled)
  if (debug && (key == 'z' || key == 'Z') && !autoParticle) {
    grid.updateParticles();
    println("forced a particle update");
  }
  
  //, toggles whether the game is lost (if debug mode is enabled)
  if (debug && (key == ',')) {
    lost = !lost;
    println("toggled lose", lost);
  }

  //` toggles the functionality of the number row (if debug mode is enabled)
  if (debug && key == '`') {
    numRowFunc = !numRowFunc;
    if (numRowFunc) {
      println("numRowFunc toggled: shapes");
    } else {
      println("numRowFunc toggled: types");
    }
  }

  // disables debug mode (if debug mode is enabled)
  if (debug && key == '/') {
    debug = false;
    println("debug mode disabled");
  }

  //sets the type to the (n - 1)th type/shape (except 0 is 9) (type/shape decided by numRowFunc) (if debug mode is enabled)
  if (debug && ((key == '1') || (key == '2') || (key == '3') || (key == '4') || (key == '5') || (key == '6') || (key == '7') || (key == '8') || (key == '9') || (key == '0'))) {
    int num = 9;
    if (key == '1') {
      num = 0;
    } else if (key == '2') {
      num = 1;
    } else if (key == '3') {
      num = 2;
    } else if (key == '4') {
      num = 3;
    } else if (key == '5') {
      num = 4;
    } else if (key == '6') {
      num = 5;
    } else if (key == '7') {
      num = 6;
    } else if (key == '8') {
      num = 7;
    } else if (key == '9') {
      num = 8;
    }
    //set type
    if (numRowFunc) {
      grid.tetromino.setType(allTypes.get(num % allTypes.size()));
    } 
    //set shape
    else {
      grid.tetromino.shape = num % 7;
      grid.tetromino.copyTemplate();
    }
  }

  //space slams (doesn't work if paused (unless in debug mode))
  if (((!lost && !paused) || debug) && key == ' ') {
    grid.tetromino.slam(true);
    //set oldTime to the current time
    prevTime = System.nanoTime();
  }

  //rotates the tetromino 90 degrees clockwise (doesn't work if paused or lost (unless in debug mode))
  if (((!lost && !paused) || debug) && keyCode == UP) {
    grid.tetromino.rotateRight();
  }
  //moves the tetromino left one block (doesn't work if paused or lost (unless in debug mode))
  if (((!lost && !paused) || debug) && keyCode == LEFT) {
    grid.tetromino.left();
  }
  //moves the tetromino right one block (doesn't work if paused or lost (unless in debug mode))
  if (((!lost && !paused) || debug) && keyCode == RIGHT) {
    grid.tetromino.right();
  }
  //moves the tetromino down one block (doesn't work if paused or lost (unless in debug mode))
  if (((!lost && !paused) || debug) && keyCode == DOWN) {
    grid.tetromino.down();
    //set oldTime to the current time
    prevTime = System.nanoTime();
  }

  konamiCheck(keyCode);
}

//draws the provided text with a border around it
void drawTextWithBorder(String text, float topLeftCornerX, float topLeftCornerY, int textSize, Color fillColor, Color borderColor)
{
  //render the score
    textSize(textSize);
    fill(borderColor);

    for (int x = -1; x < 2; x++) 
    {
      text(text, topLeftCornerX + x, topLeftCornerY);
      text(text, topLeftCornerX, topLeftCornerY + x);
    }

    fill(fillColor);
    text(text, topLeftCornerX, topLeftCornerY);
}

//add points to the score/level points
void addPoints(int points)
{
  //increase score [points multiplier based on how fast the tetromino is falling]
  score += int(points * (ticksPerSecond / ticksPerBlockDrop));

  //add points to level tracker
  levelPoints += points;

  //go up a level
  if (levelPoints >= levelPointsPerLevel)
  {
    levelPoints -= levelPointsPerLevel;

    setLevel(level + 1);
  }
}

void setLevel(int level)
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
  println("~~~~~~~~~~~");
  println("Level " + level);
  println("Level Points: " + levelPoints + "/" + levelPointsPerLevel);
  println("~~~~~~~~~~~");
}

void konamiCheck(int code) {
  inputs.remove(0);
  inputs.add(code);

  boolean match = true;
  for (int lcv = 0; lcv < konami.size(); lcv++) {
    if (inputs.get(lcv) != konami.get(lcv)) {
      match = false;
    }
  }
  if (!debug && match) {
    debug = true;
    println("Activating debug mode");
  }
}

//lets me send in a Color to fill without any messing around
void fill(Color colour) {
  fill(colour.r, colour.g, colour.b, colour.a);
}

//lets me send in a Color to stroke without any messing around
void stroke(Color colour)
{
  stroke(colour.r, colour.g, colour.b, colour.a);
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
