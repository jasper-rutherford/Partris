import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Collections; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class PROJ4 extends PApplet {

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
float particleWidth = blockWidth / PApplet.parseFloat(particlesPerEdge);

float fullFactor = 3.0f/5;

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

float ticksPerSecond = 60.0f;
float ticksPerBlockDrop = levelSpeeds[0];
float particleUpdatesPerBlockDrop = 30.0f;

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


public void setup() {
  
  surface.setTitle("Partris");
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

public void lose() {
  if (!lost) {
    //amountOfTicks = 7;
    //ns = 1000000000 / amountOfTicks;
    lost = true;

    println("lost");
  }
}

public void draw() 
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
    drawTextWithBorder("Paused", width / 2 - blockWidth * 8.5f, height / 2 - blockWidth * 0, 150, new Color(248, 255, 48), new Color(0, 0, 0));
  }
}    

//key controls
public void keyPressed() {

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
public void drawTextWithBorder(String text, float topLeftCornerX, float topLeftCornerY, int textSize, Color fillColor, Color borderColor)
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
public void addPoints(int points)
{
  //increase score [points multiplier based on how fast the tetromino is falling]
  score += PApplet.parseInt(points * (ticksPerSecond / ticksPerBlockDrop));

  //add points to level tracker
  levelPoints += points;

  //go up a level
  if (levelPoints >= levelPointsPerLevel)
  {
    levelPoints -= levelPointsPerLevel;

    setLevel(level + 1);
  }
}

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
  println("~~~~~~~~~~~");
  println("Level " + level);
  println("Level Points: " + levelPoints + "/" + levelPointsPerLevel);
  println("~~~~~~~~~~~");
}

public void konamiCheck(int code) {
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
public void fill(Color colour) {
  fill(colour.r, colour.g, colour.b, colour.a);
}

//lets me send in a Color to stroke without any messing around
public void stroke(Color colour)
{
  stroke(colour.r, colour.g, colour.b, colour.a);
}

/*
  Sets up the tetromino templates
 */

//shortcut to create a block
public Block b(int x, int y) {
  return new Block(x, y);
}

//sends in the four given blocks to the template array at once
public void pack(int shape, int rotation, Block a, Block b, Block c, Block d) {
  Block[] packed = {a, b, c, d};
  templates[shape][rotation] = packed;
  //templates[shape][rotation][0] = a;
  //templates[shape][rotation][1] = b;
  //templates[shape][rotation][2] = c;
  //templates[shape][rotation][3] = d;
}

//all the templates for all the tetrominos and all their rotations. Each increment of the rotation index is a 90 degree clockwise rotation with arbitrary starting positions.
//tetrominos are in alphabetical order (IJLOSTZ -> 0123456)
public void fillTemplates() {
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
public class AcidParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(226, 255, 97);

  private int health = 1;
  private int consumeCost = 1;

  public AcidParticle(Point indices, String prevType)
  {
    super(indices, "Acid", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {
    //it checks 1. directly below, 2. the diagonals below, 3. the neighbors directly left and right. 
    //if 1 exists (and is not acid), move there
    //if 2 exists (and is not acid), move to one of those
    //if 3 exists (and is not acid), move to one of those

    //create a list of particles which this particle can move into
    ArrayList<Particle> openSpaces = new ArrayList<Particle>();

    //get particle which is below this particle
    Particle down = adjacentDown();

    //if that particle is not null
    if (down != null) 
    {
      //if the particle below this one is not acid or stone
      if (!down.type.equals("Acid") && !down.type.equals("Stone") && !down.type.equals("Lava")) 
      {
        //add that particle to the list of spaces to move into
        openSpaces.add(down);
      } 

      //if no space has been found
      if (openSpaces.size() == 0)
      {
        //check the diagonals 

        //get the diagonal particles
        Particle downLeft = down.adjacentLeft();
        Particle downRight = down.adjacentRight();

        //check the left diagonal for non-acid
        if (downLeft != null && !downLeft.type.equals("Acid") && !downLeft.type.equals("Stone") && !downLeft.type.equals("Lava")) 
        {
          //add that particle to the list of spaces to move into
          openSpaces.add(downLeft);
        }

        //check the right diagonal for non-acid
        if (downRight != null && !downRight.type.equals("Acid") && !downRight.type.equals("Stone") && !downRight.type.equals("Lava")) 
        {
          //add that particle to the list of spaces to move into
          openSpaces.add(downRight);
        }
      }
    }

    //if no spaces have been found
    if (openSpaces.size() == 0)
    {
      //check the left/right neighbors

      //get left/right neighbor particles
      Particle left = adjacentLeft();
      Particle right = adjacentRight();

      //check the left neighbor for non-acid
      if (left != null && !left.type.equals("Acid") && !left.type.equals("Stone") && !left.type.equals("Lava")) 
      {
        //add to the list of spaces to move into
        openSpaces.add(left);
      }

      //check the right neighbor for non-acid
      if (right != null && !right.type.equals("Acid") && !right.type.equals("Stone") && !right.type.equals("Lava")) 
      {
        //add to the list of spaces to move into
        openSpaces.add(right);
      }
    }

    //if any potential particles have been found to move into
    if (openSpaces.size() != 0) 
    {
      //choose a random available particle
      Particle rand = openSpaces.get(PApplet.parseInt(random(0, openSpaces.size())));

      //dissolve the particle
      rand = dissolve(rand);

      //if the acid still has health
      if (health > 0) //(doesnt have to kill the acid here, it dies in the dissolve method)
      {
        //swap this particle with the dissolved particle
        grid.swapParticles(this, rand);

        //mark that this particle moved
        moved = true;
      }
    }
  }

  public Particle dissolve(Particle particle)
  {
    //if the particle is not air (air doesnt dissolve)
    if (!particle.type.equals("Air") && !particle.type.equals("Stone") && !particle.type.equals("Lava"))
    {
      //gain points for dissolving stuff
      addPoints(dissolvePoints);

      //create a new air particle
      Particle air = particleFactory.generateParticle("Air", particle);

      //replace the dissolving particle with the air particle
      grid.replaceParticle(particle, air);

      //consume acid health
      health -= consumeCost;

      //if no more health
      if (health <= 0)
      {
        //replace this particle with a new air particle
        grid.replaceParticle(this, "Air");
      }

      //return the air that is in the dissolved spot
      return air;
    }

    //return the air
    return particle;
  }

  public void interact()
  {
    //get the above particle
    Particle up = adjacentUp();

    //check if it's not acid or air
    if (up != null && !up.type.equals("Air") && !up.type.equals("Acid"))
    {
      //dissolve it
      dissolve(up);
    }
  }
}
public class AirParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(0, 0, 0, 0);

  public AirParticle(Point indices, String prevType)
  {
    super(indices, "Air", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {

  }

  public void interact()
  {
    
  }
}
/**
 Final Project 5611
 Block Class
 Represents one block of a tetromino
 
 Written by Jasper Rutherford
 */

public class Block {
  public int x;
  public int y;
  public boolean active;
  public boolean full;
  public int type;
  public Color colour;

  public Block(int x, int y) {
    this.x = x;
    this.y = y;
    active = false;
    full = false;
    colour = new Color(1, 1, 1);
  }

  public void render()
  {
    // //if (active) {
    // //  noFill();
    // //strokeWeight(2);
    // //  rect(grid.cornerX + blockWidth * x, grid.cornerY + blockWidth * y, blockWidth, blockWidth);
    // //strokeWeight(1);
    // //}
    // Color fullColor = new Color(248, 143, 255);//209, 250, 160);//238, 255, 230);//
    // Color emptyColor = new Color(200, 200, 200);//252, 241, 237);//255, 233, 227);//250, 178, 160);
    // if (full) 
    // {
    //   fill(fullColor);
    // }
    // else
    // {
    //   fill(emptyColor);
    // }
    // // else
    // // {
    // //   fill(emptyColor);
    // // }
    // rect(grid.cornerX + blockWidth * x, grid.cornerY + blockWidth * y, blockWidth, blockWidth);

    if (full)
    {
      float x = grid.cornerX + blockWidth * this.x;
      float y = grid.cornerY + blockWidth * this.y;

      //if left doesnt exist or is not full
      if (adj("Left") == null || !adj("Left").full)
      {
        //draw edge
        helper(3, new Color(0, 0, 0), x, y, x, y + blockWidth);
        helper(1, new Color(246, 255, 66), x, y, x, y + blockWidth);
      }
      //if right doesnt exist or is not full
      if (adj("Right") == null || !adj("Right").full)
      {
        helper(3, new Color(0, 0, 0), x + blockWidth, y, x + blockWidth, y + blockWidth);
        helper(1, new Color(246, 255, 66), x + blockWidth, y, x + blockWidth, y + blockWidth);
      }
      //if down doesnt exist or is not full
      if (adj("Down") == null || !adj("Down").full)
      {
        helper(3, new Color(0, 0, 0), x, y + blockWidth, x + blockWidth, y + blockWidth);
        helper(1, new Color(246, 255, 66), x, y + blockWidth, x + blockWidth, y + blockWidth);
      }
      //if up doesnt exist or is not full
      if (adj("Up") == null || !adj("Up").full)
      {
        helper(3, new Color(0, 0, 0), x, y, x + blockWidth, y);
        helper(1, new Color(246, 255, 66), x, y, x + blockWidth, y);
      }
    }
  }

  private void helper(int s, Color c, float x1, float y1, float x2, float y2)
  {
    strokeWeight(s);
    stroke(c);
    line(x1, y1, x2, y2);
  }

  //get a block neighboring this one
  private Block adj(String dir)
  {
    if (dir.equals("Right"))
    {
      if (x + 1 < gridWidth)
      {
        return grid.blocks[x + 1][y];
      }
    }
    else if (dir.equals("Left"))
    {
      if (x > 0)
      {
        return grid.blocks[x - 1][y];
      }
    }
    else if (dir.equals("Up"))
    {
      if (y > 0)
      {
        return grid.blocks[x][y - 1];
      }
    }
    else if (dir.equals("Down"))
    {
      if (y + 1 < gridHeight)
      {
        return grid.blocks[x][y + 1];
      }
    }

    return null;
  }
}
public class CharcoalParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(43, 43, 43);
  private int fuel = -1;

  public CharcoalParticle(Point indices, String prevType)
  {
    super(indices, "Charcoal", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {

  }

  public void interact()
  {
    
  }
}
/*
 Final Project 5611
 Color Class
 
 Represents a color, because there's no color class and I want it to work how I want it to work.
 It's possible that this class already exists within processing in some format. Maybe it would work the same way.
 However, I was unable to find that exact preexisting functionality. So here it is.
 
 Written by Jasper Rutherford
 */

public class Color {
  //red, green, blue, alpha
  public int r;
  public int g;
  public int b;
  public int a;

  //constructor with no alpha specified. Alpha defaults to 255.
  public Color(int r, int g, int b) {
    this.r = r;
    this.g = g;
    this.b = b;
    a = 255;
  }

  //constructor that specifies alpha
  public Color(int r, int g, int b, int a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }
  
  //returns a copy of this color
  public Color copy() {
     return new Color(r, g, b, a); 
  }
  
  //makes printing nicer
  public String toString() {
   return r + ", " + g + ", " + b + ", " + a; 
  }
  
  //checks if the two rgba's are the same
  public boolean equals(Color colour) {
     return this.r == colour.r 
         && this.g == colour.g 
         && this.b == colour.b 
         && this.a == colour.a; 
  }
}
public class FireParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(235, 64, 52);

  private int baseFuel = 500;
  private int fuel;

  public FireParticle(Point indices, String prevType)
  {
    super(indices, "Fire", prevType);
    this.colour = uniqueColor;

    //charcoal starts with more fuel (burns longer)
    if (prevType.equals("Charcoal"))
    {
        fuel = baseFuel * 3;
    }
    //everything else [aka, plants] starts with the standard amount of fuel
    else
    {
      fuel = baseFuel;
    }
  }

  public void move()
  {
    //spend some fuel
    fuel -= PApplet.parseInt(random(0, baseFuel / 2));

    //if no more fuel
    if (fuel <= 0) 
    {
      //replace this particle with a new air particle
      grid.replaceParticle(this, "Air");

      //gain points from burning stuff
      //burning plant
      if (prevType.equals("Plant"))
      {
        addPoints(plantBurnPoints);
      }
      //burning charcoal
      if (prevType.equals("Charcoal"))
      {
        addPoints(plantBurnPoints);
      }
    }

    //mark that this particle moved
    moved = true;
  }

  public void interact()
  {
    //get half adjacents
    ArrayList<Particle> halfAdjacents = halfAdjacents();

    //track if this fire has access to air (assume not)
    boolean hasAir = false;

    //loop through half adjacents 
    for (Particle adj : halfAdjacents) 
    {
      //if the adjacent particle is air
      if (adj.type.equals("Air")) 
      {
        //mark that this fire has access to air 
        hasAir = true;

        //exit loop
        break;
      }
    }

    //if this fire has no air access
    if (!hasAir) 
    {
      println("extinguishing - " + prevType);
      //replace this particle with a particle of prevType
      grid.replaceParticle(this, prevType);

      //mark that this particle interacted
      interacted = true;
    }
    //if the fire has access to air 
    else 
    {
      //loop through the fire's half adjacents      
      for (Particle adj : halfAdjacents) 
      {
        //interact fire with plant
        if (adj.type.equals("Plant")) 
        {
          //get the plant's adjacents
          ArrayList<Particle> plantAdjacents = adj.halfAdjacents();

          //track whether the plant has access to air
          boolean airAccess = false;

          //loop through the plant's half adjacents
          for (Particle plantAdj : plantAdjacents) 
          {
            //if the plant has any air fully adjacent
            if (plantAdj.type.equals("Air")) 
            {
              //mark that the plant has access to air
              airAccess = true;

              //exit loop
              break;
            }
          }

          //if the plant has access to air
          if (airAccess)
          {
            //replace the plant with a new fire particle
            grid.replaceParticle(adj, "Fire");

            //mark that this fire interacted with something
            interacted = true;
          }
        }
        //interact fire with char
        if (adj.type.equals("Charcoal")) 
        {
          //get the char's adjacents
          ArrayList<Particle> charAdjacents = adj.halfAdjacents();

          //track whether the char has access to air
          boolean airAccess = false;

          //loop through the char's half adjacents
          for (Particle charAdj : charAdjacents) 
          {
            //if the char has any air fully adjacent
            if (charAdj.type.equals("Air")) 
            {
              //mark that the char has access to air
              airAccess = true;

              //exit loop
              break;
            }
          }

          //if the char has access to air
          if (airAccess)
          {
            //replace the char with a new fire particle
            grid.replaceParticle(adj, "Fire");

            //mark that this fire interacted with something
            interacted = true;
          }
        }
        //interact fire with ice
        else if (adj.type.equals("Ice")) 
        {
          //replace ice particle with new water particle
          grid.replaceParticle(adj, "Water");

          //mark that this fire interacted with something
          interacted = true;
        }
      }
    }
  }
}


public class GoopParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(173, 23, 207);
  private boolean didStuff = false;

  public GoopParticle(Point indices, String prevType)
  {
    super(indices, "Goop", prevType);
    this.colour = uniqueColor;
  }

  //move goop
  // check each space around this one
  // the air space that has the most goop around it is the space to move to
  // ties are chosen by random
  public void move()
  {
    //list of spaces to move into
    ArrayList<Particle> spaces = new ArrayList<Particle>();

    //number of goop nearby each best available space
    int bestGoopCount = 0;

    //get all the neighbor particles
    ArrayList<Particle> neighbors = halfAdjacents();

    //check all neighbors
    for (Particle neighbor : neighbors)
    {
      //if the neighbor is air
      if (neighbor.type.equals("Air"))
      {
        //get the neighbor's goop count
        int goopCount = calcGoopCount(neighbor);
        

        if (goopCount > bestGoopCount)
        {
          bestGoopCount = goopCount;
          spaces = new ArrayList<Particle>();
          spaces.add(neighbor);
        }
        else if (goopCount == bestGoopCount)
        {
          spaces.add(neighbor);
        }
      }
    }

    //only move if there is one best space
    if (spaces.size() != 0)
    {
      //choose a random particle from the list of spaces
      Particle chosen = spaces.get(PApplet.parseInt(random(0, spaces.size())));

      //switch this particle with that particle
      grid.swapParticles(chosen, this);

      //you dont have to mark this particle as moved - grid.swapparticles does that automatically. all the other move methods were written before this functionality existed/before I realized it was redundant
    }

    doStuff();
  }

  //calculate how many of the particles surrounding this particle are goop or were goop 
  private int calcGoopCount(Particle p)
  {
    int goopCount = 0;
    for (Particle adj : p.wideNeighbors((particlesPerEdge + 1) / 2))
    {
      if (adj.type.equals("Goop"))
      {
        goopCount++;
      }
      else if (!adj.type.equals("Air"))
      {
        goopCount += 5;
      }
    }

    return goopCount;
  }

  //loop through full adjacents in random order
  //if the adj is not air or goop, replace this goop with another particle of adj
  public void interact()
  {
    // if (!didStuff)
    // {
      // doStuff();
    // }
  }

  private void doStuff()
  {
    //get neighbors    
    ArrayList<Particle> neighbors = fullAdjacents();

    //randomize order
    Collections.shuffle(neighbors);

    //loop through neighbors
    for (Particle adj : neighbors)
    {
      //if adj isnt air or goop
      if (!adj.type.equals("Air") && !adj.type.equals("Goop"))
      {
        //replace this particle with a new particle of adj.type
        grid.replaceParticle(this, adj.type);

        //stop looping through neighbors
        break;
      }
    }
  }
}
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
  public void shuffleParticleList() 
  {
    println("particle list size before shuffle: " + particleList.size());
    ArrayList<Particle> temp = new ArrayList<Particle>();

    //need to save size because the original list will shrink as the loop progresses
    int size = particleList.size();

    //loop through all particles in the particle list
    for (int lcv = 0; lcv < size; lcv++) 
    {
      //get random particle from the old list
      int index = PApplet.parseInt(random(0, particleList.size()));
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

    int index = PApplet.parseInt(random(0, shapes.size()));
    int shape = shapes.get(index);
    shapes.remove(index);
    return shape;
  }

  //fill the particle types
  public void fillTypes() {
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

    int index = PApplet.parseInt(random(0, types.size()));
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
  public void setupTetrominos() {
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
  public void checkRows() {
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
  public void clearRow(int row) {
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
  public void hold() {
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
  public void newTetromino() {
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

  public void render() 
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
        ghost.colour.a = PApplet.parseInt(255 * .75f);
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
      float xGoal = (tWidth - 1) / -2.0f;
      float yGoal = (tHeight - 1) / -2.0f;

      //offset is how far the block needs to move to get there (and accordingly how far all blocks need to move to be on target)
      float xOffset = xGoal - xMin;
      float yOffset = yGoal - yMin;

      //calculate the center coords of the box
      float centerX = (cornerX - 7 * blockWidth) + (3 * blockWidth);
      float centerY = cornerY + (3 * blockWidth);

      //draw the blocks around the center of the held box
      for (int lcv = 0; lcv < 4; lcv++) {
        fill(held.colour);
        rect(centerX + (xOffset + held.blocks[lcv].x) * blockWidth - blockWidth / 2.0f, centerY + (yOffset + held.blocks[lcv].y) * blockWidth - blockWidth / 2.0f, blockWidth, blockWidth);
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
      float xGoal = (tWidth - 1) / -2.0f;
      float yGoal = (tHeight - 1) / -2.0f;

      //offset is how far the block needs to move to get there (and accordingly how far all blocks need to move to be on target)
      float xOffset = xGoal - xMin;
      float yOffset = yGoal - yMin;

      //calculate the center coords of the box
      float centerX = (cornerX - 7 * blockWidth) + (3 * blockWidth) + 18 * blockWidth;
      float centerY = cornerY + (3 * blockWidth) + 6 * blockWidth * loop;

      //draw the blocks around the center of the held box
      for (int lcv = 0; lcv < 4; lcv++) {
        fill(queued.blocks[lcv].colour);
        rect(centerX + (xOffset + queued.blocks[lcv].x) * blockWidth - blockWidth / 2.0f, centerY + (yOffset + queued.blocks[lcv].y) * blockWidth - blockWidth / 2.0f, blockWidth, blockWidth);
      }
    }

    //render the score
    drawTextWithBorder("Score: " + score, grid.cornerX - blockWidth * 7, grid.cornerY - blockWidth / 4, 25, new Color(255, 255, 255), new Color(0, 0, 0));

    //render the level counter
    drawTextWithBorder("Level: " + level, grid.cornerX + blockWidth * 11, grid.cornerY - blockWidth * 1.25f, 25, new Color(255, 255, 255), new Color(0, 0, 0));

    //render level points bar
    fill(new Color(199, 199, 199));
    rect(cornerX + 11.5f * blockWidth, cornerY - blockWidth * 5 / 6, 5 * blockWidth, 2.0f / 3 * blockWidth);
    fill(new Color(61, 132, 245));
    rect(cornerX + 11.5f * blockWidth, cornerY - blockWidth * 5 / 6, 5 * blockWidth * levelPoints / levelPointsPerLevel, 2.0f / 3 * blockWidth);

    //render the level points
    drawTextWithBorder(levelPoints + "/" + levelPointsPerLevel, grid.cornerX + blockWidth * 12, grid.cornerY - blockWidth / 3.7f, 15, new Color(255, 255, 255), new Color(0, 0, 0));  
    drawTextWithBorder("(" + PApplet.parseInt(levelPoints * 100 / levelPointsPerLevel) + "%)", grid.cornerX + blockWidth * (12 + 4.75f), grid.cornerY - blockWidth / 3.7f, 15, new Color(255, 255, 255), new Color(0, 0, 0));  
  }
}
public class IceParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(150, 183, 235);

  public IceParticle(Point indices, String prevType)
  {
    super(indices, "Ice", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {

  }

  public void interact()
  {
    //get half adjacents
    ArrayList<Particle> halfAdjacents = halfAdjacents();

    //check half adjacents 
    for (Particle adj : halfAdjacents) 
    {
      //interact ice with water
      if (adj.type.equals("Water")) 
      {
        //replace that water with a new ice particle
        grid.replaceParticle(adj, "Ice");

        //mark that this particle interacted
        interacted = true;
      }
    }
  }
}
public class LavaParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(214, 111, 32);

  public LavaParticle(Point indices, String prevType)
  {
    super(indices, "Lava", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {
    //it checks 1. directly below, 2. the diagonals below, 3. the neighbors directly left and right. 
    //if 1 exists, move there
    //if 2 exists, move to one of those
    //if 3 exists, move to one of those

    //create a list of particles which this particle can move into
    ArrayList<Particle> openSpaces = new ArrayList<Particle>();

    //get particle which is below this particle
    Particle down = adjacentDown();

    //if that particle is not null
    if (down != null) 
    {
      //if the particle below this one is air
      if (down.type.equals("Air") || down.type.equals("Acid")) 
      {
        //add that particle to the list of spaces to move into
        openSpaces.add(down);
      } 

      //if no space has been found
      if (openSpaces.size() == 0)
      {
        //check the diagonals for air

        //get the diagonal particles
        Particle downLeft = down.adjacentLeft();
        Particle downRight = down.adjacentRight();

        //check the left diagonal for air
        if (downLeft != null && (downLeft.type.equals("Air") || downLeft.type.equals("Acid"))) 
        {
          //add that particle to the list of spaces to move into
          openSpaces.add(downLeft);
        }

        //check the right diagonal for air
        if (downRight != null && (downRight.type.equals("Air") || downRight.type.equals("Acid"))) 
        {
          //add that particle to the list of spaces to move into
          openSpaces.add(downRight);
        }
      }
    }

    //if no spaces have been found
    if (openSpaces.size() == 0)
    {
      //check the left/right neighbors for air

      //get left/right neighbor particles
      Particle left = adjacentLeft();
      Particle right = adjacentRight();

      //check the left neighbor for air
      if (left != null && (left.type.equals("Air") || left.type.equals("Acid"))) 
      {
        //add to the list of spaces to move into
        openSpaces.add(left);
      }

      //check the right neighbor for air
      if (right != null && (right.type.equals("Air") || right.type.equals("Acid"))) 
      {
        //add to the list of spaces to move into
        openSpaces.add(right);
      }
    }

    //if any potential particles have been found to swap with 
    if (openSpaces.size() != 0) 
    {
      //choose a random available particle
      Particle rand = openSpaces.get(PApplet.parseInt(random(0, openSpaces.size())));

      //swap this particle with the particle in the chosen space
      grid.swapParticles(this, rand);

      //mark that this particle moved
      moved = true;
    }
  }

  public void interact()
  {
    //get half adjacents
    ArrayList<Particle> halfAdjacents = halfAdjacents();

    //check half adjacents
    for (Particle adj : halfAdjacents) 
    {
      //interact lava with water
      if (adj.type.equals("Water")) 
      {
        //place a new stone particle where the water was
        grid.replaceParticle(adj, "Stone");
        
        //place a new stone particle where this lava was
        grid.replaceParticle(this, "Stone");

        //marks this particle as interacted
        interacted = true;
      }
      //interact lava with plant 
      else if (adj.type.equals("Plant")) 
      {
        //check plant's half adjacents for air

        //get plant's half adjacents
        ArrayList<Particle> plantAdjacents = adj.halfAdjacents();

        //track whether the plant has access to air
        boolean hasAir = false;

        //loop through all plant half adjacents
        for (Particle plantAdj : plantAdjacents)  
        {
          //if the plant's neighbor is air
          if (plantAdj.type.equals("Air")) 
          {
            //mark that the plant has access to air
            hasAir = true;

            //stop looping - air has been found
            break;
          }
        }

        //if the plant has air fully adjacent
        if (hasAir) 
        {
          //replace the plant with a fire particle
          grid.replaceParticle(adj, "Fire");
        }
        //if the plant has no air
        else 
        {
          //replace the plant with a charcoal particle
          grid.replaceParticle(adj, "Charcoal");
        }

        //mark that this particle interacted with something
        interacted = true;
      }
      //interact lava with charcoal
      else if (adj.type.equals("Charcoal")) 
      {
        //get charcoal's half adjacents
        ArrayList<Particle> charAdjacents = adj.halfAdjacents();

        //track whether the charcoal has access to air
        boolean hasAir = false;
       
        //loop through all plant half adjacents
        for (Particle charAdj : charAdjacents) 
        {
          //if the charcoal's neighbor is air
          if (charAdj.type.equals("Air")) 
          {
            //mark that the charcoal has access to air
            hasAir = true;
            
            //stop looping - air has been found
            break;
          }
        }

        //if the char has air fully adjacent
        if (hasAir) 
        {
          //replace the plant with a fire particle
          grid.replaceParticle(adj, "Fire");
        }

        //mark that this particle interacted with something
        interacted = true;
      }
      //interact lava with ice
      else if (adj.type.equals("Ice")) 
      {
        //replace the ice with a water particle
        grid.replaceParticle(adj, "Water");

        //mark that this particle interacted with something
        interacted = true;
      }
      // //interact lava with stone
      // else if (adj.type.equals("Stone"))
      // {
      //   //replace the stone with a lava particle
      //   grid.replaceParticle(adj, "Lava");

      //   //mark that this particle interacted with something
      //   interacted = true;
      // }
    }
  }
}
/**
 Final Project 5611
 Partcle Class
 Represents one Particle
 
 Written by Jasper Rutherford
 */

public class Particle {
  //coordinates of top left corner of particle (as seen on screen)
  private float x;
  private float y;

  //indices in the particleGrid 
  private Point indices;

  public String type;
  public String prevType;

  //color defaults to null - should be overwritten by subparticle
  public Color colour = null;

  //whether or not this particle was created this tick (either from moving or from interacting)
  public boolean fresh;

  //if a particle is awake, it will try to move and interact. 
  public boolean awake;

  //whether or not the particle moves when it runs move() (used to determine if the particle should go to sleep)
  public boolean moved = false;

  //whether or not the particle interacted when it runs interact() (used to determine if the particle should go to sleep)
  public boolean interacted = false;

  //this particle's index in the grid.particleList
  public int listIndex;

  //new constructor for scalability with adding new particle types
  public Particle(Point indices, String type, String prevType)
  {
    //set x/y indices
    setIndices(indices);

    //set types
    this.type = type;
    this.prevType = prevType;

    //set colour
    this.colour = new Color(100, 100, 100);

    //default to fresh and awake
    fresh = true;
    awake = true;
  }

  //calculate and set x/y positions (on the screen - used for rendering)
  public void updateCoordsFromIndices() 
  {
    x = grid.cornerX + indices.x * particleWidth;
    y = grid.cornerY + indices.y * particleWidth;
  }

  //updates this particle's indices in the particlegrid (also updates screen coords)
  public void setIndices(Point indices)
  {
    this.indices = indices;
    updateCoordsFromIndices();
  }

  public void sleep() {
    awake = false;
  }

  public void wake() {
    awake = true;

    //if a particle is woken after it has already failed to move, and then it fails to interact, it will go right to sleep even though it should remain awake.
    //setting moved to true here prevents this.
    //setting interacted to true is likely redundant.
    moved = true;
    // interacted = true;
  }

  // public void setType(String type) {
  //   //set the type
  //   prevType = this.type;
  //   this.type = type;
  //   this.colour = grid.colorMap.get(type).copy();

  //   //wake self
  //   wake();

  //   //wake neighbors
  //   ArrayList<Particle> neighbors = halfAdjacents();
  //   for (int lcv = 0; lcv < neighbors.size(); lcv++) {
  //     neighbors.get(lcv).wake();
  //   }

  //   //fuel any new fire
  //   if ((prevType.equals("Air") || prevType.equals("Plant")) && type.equals("Fire")) {
  //     fuel = baseFuel;
  //   } else if (prevType.equals("Charcoal") && type.equals("Fire")) {
  //     fuel = baseFuel * 3;
  //   } 

  //   //if this is a fire that burned out then gain a point
  //   if (!lost && prevType.equals("Fire") && type.equals("Air")) {
  //     score++;
  //   }

  //   fresh = true;
  // }

  //wakes up this particle's neighbors
  public void wakeNeighbors()
  {
    //get the neighbors
    ArrayList<Particle> neighbors = halfAdjacents();

    //loop through the neighbors
    for (Particle neighbor : neighbors) 
    {
      //wake them up!
      neighbor.wake();
    }
  }

  public void render() {
    if (!type.equals("Air")) {
      //Color test = colour.copy();
      //if (!awake && alphaSleep) {
      //  test.a /= 2;
      //}
      fill(colour);
      if (awake && alphaSleep) {
        strokeWeight(1);
      } else {
        strokeWeight(0);
      }
      rect(x, y, particleWidth, particleWidth);

      if (awake && alphaSleep) 
      {
        line(x, y, x + particleWidth, y + particleWidth);
      }
      strokeWeight(1);
    }

    //render number of wide neighbors
    if (renderParticleWideNeighbors)
    {
      int numWideNeighbors = wideNeighbors(3).size();

      fill(new Color(255, 255, 255));
      textSize(10);
      text(numWideNeighbors, x + particleWidth / 2, y + particleWidth * .75f);
    }
  }

  //tries to move this particle
  public void move() 
  {
    println("a " + type + " particle had no move method?");  
  }

  //get all particles that are adjacent to this particle, including diagonals
  public ArrayList<Particle> halfAdjacents() {
    //get all adjacent particles
    ArrayList<Particle> adjacents = new ArrayList<Particle>();

    //get left adjacent
    Particle adj = adjacentLeft();
    if (adj != null) {
      adjacents.add(adj);

      //try to get bottom left corner if left exists
      adj = adj.adjacentDown();
      if (adj != null) {
        adjacents.add(adj);
      }
    }

    //get right adjacent
    adj = adjacentRight();
    if (adj != null) {
      adjacents.add(adj);

      //try to get top right corner if right exists
      adj = adj.adjacentUp();
      if (adj != null) {
        adjacents.add(adj);
      }
    }

    //get up adjacent
    adj = adjacentUp();
    if (adj != null) {
      adjacents.add(adj);

      //try to get top left corner if up exists
      adj = adj.adjacentLeft();
      if (adj != null) {
        adjacents.add(adj);
      }
    }

    //get down adjacent
    adj = adjacentDown();
    if (adj != null) {
      adjacents.add(adj);

      //try to get bottom right corner if bottom exists
      adj = adj.adjacentRight();
      if (adj != null) {
        adjacents.add(adj);
      }
    } 
    return adjacents;
  }

  //returns particles that are adjacent to this particle, not including diagonals
  public ArrayList<Particle> fullAdjacents() {
    //get all adjacent particles
    ArrayList<Particle> adjacents = new ArrayList<Particle>();

    //get left adjacent
    Particle adj = adjacentLeft();
    if (adj != null) {
      adjacents.add(adj);
    }

    //get right adjacent
    adj = adjacentRight();
    if (adj != null) {
      adjacents.add(adj);
    }

    //get up adjacent
    adj = adjacentUp();
    if (adj != null) {
      adjacents.add(adj);
    }

    //get down adjacent
    adj = adjacentDown();
    if (adj != null) {
      adjacents.add(adj);
    } 

    return adjacents;
  }

  //returns all particles within range of this particle
  //like, a square with a side length of range * 2 + 1
  //so range 1 -> 3x3 square centered around this particle
  // range 2 -> 5x5 square etc
  public ArrayList<Particle> wideNeighbors(int range)
  {
    ArrayList<Particle> line = new ArrayList<Particle>();

    line.add(this);
    Particle left = adjacentLeft();
    Particle right = adjacentRight();

    for (int lcv = 0; lcv < range; lcv++)
    {
      if (left != null)
      {
        line.add(left);
        left = left.adjacentLeft();
      }

      if (right != null)
      {
        line.add(right);
        right = right.adjacentRight();
      }
    }

    ArrayList<Particle> out = new ArrayList<Particle>(line);
    for (Particle p : line)
    {
      Particle up = p.adjacentUp();
      Particle down = p.adjacentDown();
      for (int lcv = 0; lcv < range; lcv++)
      {
        if (up != null)
        {
          out.add(up);
          up = up.adjacentUp();
        }

        if (down != null)
        {
          out.add(down);
          down = down.adjacentDown();
        }
      }
    }

    out.remove(this);
    return out;
  }

  public void interact() 
  {
    println("a " + type + " particle had no interact method?");  
  }

  //gets the particle from the grid that is to the left of this one
  public Particle adjacentLeft() {
    int xIndex = indices.x;
    int yIndex = indices.y;
    
    if (xIndex != 0) {
      return grid.particleGrid[xIndex - 1][yIndex];
    }
    return null;
  }

  //gets the particle from the grid that is to the right of this one
  public Particle adjacentRight() {
    int xIndex = indices.x;
    int yIndex = indices.y;
    
    if (xIndex != grid.particleGrid.length - 1) {
      return grid.particleGrid[xIndex + 1][yIndex];
    }
    return null;
  }

  //gets the particle from the grid that is above this one
  public Particle adjacentUp() {
    int xIndex = indices.x;
    int yIndex = indices.y;
    
    if (yIndex != 0) {
      return grid.particleGrid[xIndex][yIndex - 1];
    }
    return null;
  }

  //gets the particle from the grid that is below this one  
  public Particle adjacentDown() {
    int xIndex = indices.x;
    int yIndex = indices.y;
    
    if (yIndex != grid.particleGrid[0].length - 1) {
      return grid.particleGrid[xIndex][yIndex + 1];
    }
    return null;
  }

  //makes printing particles nicer
  public String toString() {
    int xIndex = indices.x;
    int yIndex = indices.y;
    
    return ("[" + xIndex + ", " + yIndex + ", " + type + ", " + awake + "]");
  }

  public Point getIndices()
  {
    return indices;
  }
}

//factory that generates particles based on passed in type
public class ParticleFactory
{
    public ParticleFactory()
    {

    }

    public Particle generateParticle(String type, Particle prevParticle)
    {
        //generate an air particle
        if (type.equals("Air"))
        {
            return new AirParticle(prevParticle.indices, prevParticle.type);
        }
        //generate an acid particle
        else if (type.equals("Acid"))
        {
            return new AcidParticle(prevParticle.indices, prevParticle.type);
        }
        //generate a charcoal particle
        else if (type.equals("Charcoal"))
        {
            CharcoalParticle charcoal = new CharcoalParticle(prevParticle.indices, prevParticle.type);
            
            //conserve fuel
            if (prevParticle instanceof FireParticle)
            {
                FireParticle fire = (FireParticle)prevParticle;
                charcoal.fuel = fire.fuel; 
            }

            return charcoal;
        }
        //generate a fire particle
        else if (type.equals("Fire"))
        {
            FireParticle fire = new FireParticle(prevParticle.indices, prevParticle.type);

            //conserve fuel
            if (prevParticle instanceof PlantParticle && ((PlantParticle)prevParticle).fuel != -1)
            {
                fire.fuel = ((PlantParticle)prevParticle).fuel;
            }
            else if (prevParticle instanceof CharcoalParticle && ((CharcoalParticle)prevParticle).fuel != -1)
            {
                fire.fuel = ((CharcoalParticle)prevParticle).fuel;
            }

            return fire;
        }
        //generate a goop particle
        else if (type.equals("Goop"))
        {
            return new GoopParticle(prevParticle.indices, prevParticle.type);
        }
        //generate a ice particle
        else if (type.equals("Ice"))
        {
            return new IceParticle(prevParticle.indices, prevParticle.type);
        }
        //generate a lava particle
        else if (type.equals("Lava"))
        {
            return new LavaParticle(prevParticle.indices, prevParticle.type);
        }
        //generate a plant particle
        else if (type.equals("Plant"))
        {
            PlantParticle plant = new PlantParticle(prevParticle.indices, prevParticle.type);

            //conserve fuel
            if (prevParticle instanceof FireParticle)
            {
                FireParticle fire = (FireParticle)prevParticle;
                plant.fuel = fire.fuel; 
            }

            return plant;
        }
        //generate a stone particle
        else if (type.equals("Stone"))
        {
            return new StoneParticle(prevParticle.indices, prevParticle.type);
        }
        //generate a water particle
        else if (type.equals("Water"))
        {
            return new WaterParticle(prevParticle.indices, prevParticle.type);
        }

        //invalid type, returns null
        else
        {
            println("tried to create particle with unknown type: " + type);
            return null;
        }
    }

    //generates an air particle with a previous type which is also air
    public Particle generateAirParticle(Point indices)
    {
        return new AirParticle(indices, "Air");
    }

    //gets the color of a particle by type 
    //yes this is dumb
    public Color getColour(String type)
    {
        println("getting color of " + type);
        return generateParticle(type, generateAirParticle(new Point(0,0))).colour;
    }
}
public class PlantParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(50, 168, 82);
  private int fuel = -1;

  public PlantParticle(Point indices, String prevType)
  {
    super(indices, "Plant", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {

  }

  public void interact()
  {
    //get full adjacents
    ArrayList<Particle> fullAdjacents = fullAdjacents();

    //check all directly adjacent particles
    for (Particle adj : fullAdjacents) 
    {
      //interact plant with air
      if (adj.type.equals("Air")) 
      {
        //if the adjacent particle is air, plant could grow there. 

        //get all the particles adjacent to the air particles
        ArrayList<Particle> airAdjacents = adj.halfAdjacents();

        //track whether the air is growable
        boolean growable = false;

        //loop through the air's neighbors
        for (Particle airAdj : airAdjacents) 
        {
          //if the air's neighbor isn't air or plant
          if (!airAdj.type.equals("Air") && !airAdj.type.equals("Plant")) 
          {
            //then it is a viable spot to grow, mark it as such
            growable = true;

            //exit loop
            break;
          }
        }   

        //if the air is growable
        if (growable) 
        {
          //place a new plant particle where the air was
          grid.replaceParticle(adj, "Plant");

          //marks this particle as interacted
          interacted = true;
        }
      }
    }
  }
}
public class Point
{
  private int x; 
  private int y;

  public Point(int x, int y)
  {
      this.x = x;
      this.y = y;
  }

  public int getX()
  {
    return this.x;
  }

  public int getY()
  {
    return this.y;
  }
}
public class SampleParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(0, 0, 0, 50);

  public SampleParticle(Point indices, String prevType)
  {
    super(indices, "Sample", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {

  }

  public void interact()
  {
    
  }
}
public class StoneParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(112, 112, 112);

  public StoneParticle(Point indices, String prevType)
  {
    super(indices, "Stone", prevType);
    this.colour = uniqueColor;
  }

  //gets the spaces below this one
  //checks for air directly below, then air on the diagonals, then water/lava directly below, then water/lava on the diagonals
  public void move()
  {
    //create an empty list of available spaces to move into
    ArrayList<Particle> openSpaces = new ArrayList<Particle>();

    //get particle below this one
    Particle down = adjacentDown();

    //if the below particle is not null
    if (down != null) 
    {
      //get the diagonal particles
      Particle downLeft = down.adjacentLeft();
      Particle downRight = down.adjacentRight();

      //if the particle below this stone is air
      if (down.type.equals("Air")) 
      {
        //add that particle to the list of available spaces
        openSpaces.add(down);
      } 

      //if no space has been found
      if (openSpaces.size() == 0)
      {
        //check the diagonals for air

        //check the left diagonal for air
        if (downLeft != null && (downLeft.type.equals("Air"))) 
        {
          //add to list of open spaces
          openSpaces.add(downLeft);
        }
        //check right diagonal for air
        if (downRight != null && (downRight.type.equals("Air"))) 
        {
          //add to list of open spaces
          openSpaces.add(downRight);
        }
      }

      //if no spaces have been found
      if (openSpaces.size() == 0)
      {
        //check down square for water or lava
        if (down.type.equals("Water") || down.type.equals("Lava") || down.type.equals("Acid")) 
        {
          //add to list of open spaces
          openSpaces.add(down);
        }
      }

      //if no spaces have been found
      if (openSpaces.size() == 0)
      {
        //check diagonals for water or lava

        //check left diagonal for water or lava
        if (downLeft != null && (downLeft.type.equals("Water") || downLeft.type.equals("Lava") || downLeft.type.equals("Acid"))) 
        {
          //add to list of open spaces
          openSpaces.add(downLeft);
        }
        //check right diagonal for water or lava
        if (downRight != null && (downRight.type.equals("Water") || downRight.type.equals("Lava") || downRight.type.equals("Acid"))) 
        {
          //add to list of open spaces
          openSpaces.add(downRight);
        }
      }
    }

    //if any spaces have been found which could be moved to
    if (openSpaces.size() != 0) 
    {
      //pick a random particle from the available spaces
      Particle move = openSpaces.get(PApplet.parseInt(random(0, openSpaces.size())));

      //swap this stone with that random particle
      grid.swapParticles(this, move);
      
      //mark that this stone moved (possibly redundant)
      moved = true;
    }
  }

  public void interact()
  {
    
  }
}
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
  public Color stroke = new Color(0, 0, 0);
  public Block blocks[];
  public Particle particles[][];

  //the x,y coords of the central block of the piece
  public int offsetX;
  public int offsetY;

  //vertical offset (number of particle rows)
  public int particleOffset;

  //creates a tetromino with a random shape and rotation
  public Tetromino() {
    //get random shape and rotation
    shape = grid.pickShape();         //pickShape uses 7 bag
    rotation = PApplet.parseInt(random(0, 4));

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

    particleOffset = tetromino.particleOffset;

    // if (collision(0, 0)) {
    //   println("2");
    //   lose();
    // }
  }  

  public void setupTetromino(int shape, int rotation, String type) {

    //instantiate blocks
    blocks = new Block[4];

    //set shape and rotation
    this.shape = shape;
    this.rotation = rotation;
    this.type = type;

    //set colour according to particle type
    colour = particleFactory.getColour(type);

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

    particleOffset = 0;

    // if (collision(0, 0)) {
    //   lose();
    // }
  }

  public void up() {
    //if an upward movement wouldn't collide with anything
    if (!grid.tetromino.collision(0, -1))
    {
      //move the block up one row
      grid.tetromino.offsetY--;
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
      //particleSlam the tetromino
      particleSlam();

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
      particleSlam();
      //put the block on the grid and create a new tetromino
      place();
      grid.checkRows();

      //get the next tetromino
      grid.newTetromino();
    }
  }

  //move the tetromino down as many particle rows as it can without hitting any particles
  public void particleSlam() {
    //drop tetromino as many particle rows as it will go
    boolean slamming = true;
    while (slamming) {
      //if a downward movement wouldn't collide with anything
      if (!particleCollision(1))
      {
        //move the block down one row
        particleOffset++;
      }
      //otherwise the block has descended as far as it will go
      else {
        slamming = false;
      }
    }
  }

  public void rotateLeft() {
  }

  //one 90 degree clockwise rotation
  public void rotateRight() {

    ////create new tetromino with rotation from current location
    //Tetromino rotated = new Tetromino(shape, (rotation + 1) % 4);
    //rotated.type = type;
    //rotated.colour = colour;
    //rotated.offsetX = offsetX;
    //rotated.offsetY = offsetY;

    Tetromino rotated = new Tetromino(this);
    rotated.rotation = (rotation + 1) % 4;
    rotated.copyTemplate();

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
  public void place() {
    //loop through each block in the tetromino
    for (int lcv = 0; lcv < 4; lcv++) {
      Block block = blocks[lcv];
      //generate particles
      for (int particleX = 0; particleX < particlesPerEdge; particleX++) {
        for (int particleY = 0; particleY < particlesPerEdge; particleY++) {
          int xIndex = PApplet.parseInt((block.x + offsetX) * particlesPerEdge + particleX);
          int yIndex = PApplet.parseInt((block.y + offsetY) * particlesPerEdge + particleY + particleOffset);

          //get the particle currently in this position
          Particle oldParticle = grid.particleGrid[xIndex][yIndex];

          //replace the old particle with a new particle
          grid.replaceParticle(oldParticle, type);
        }
      }
    }

    grid.updateBlockStats();
  }

  public void setType(String type) {
    this.type = type;
    this.colour = particleFactory.getColour(type);
  }

  //checks if the tetromino would collide with anything if it moved according to the given x, y
  public boolean collision(int x, int y) {
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

  //checks if the tetromino would collide with anything if it moved according to the given y
  public boolean particleCollision(int y) {
    boolean out = false;

    //check all blocks in the tetromino
    for (int lcv = 0; lcv < 4; lcv++) {
      Block block = blocks[lcv];
      int blockCornerX = (block.x + offsetX) * particlesPerEdge;
      int blockCornerY = (block.y + offsetY) * particlesPerEdge + particleOffset + y;

      for (int relX = 0; relX < particlesPerEdge; relX++) {
        for (int relY = 0; relY < particlesPerEdge; relY++) {
          //checks for if the given block is outside the particle grid
          int netX = blockCornerX + relX;
          int netY = blockCornerY + relY;
          if (netX < 0 || netY < 0 || netX >= gridWidth * particlesPerEdge || netY >= gridHeight * particlesPerEdge) {
            out = true;
          }
          // or inside the grid but overlapping another block
          else if (netX >= 0 && netY >= 0 && netX < gridWidth * particlesPerEdge && netY < gridHeight * particlesPerEdge && !grid.particleGrid[netX][netY].type.equals("Air")) {
            out = true;
          }
        }
      }
    }

    return out;
  }

  //copies relevant blocks from template
  public void copyTemplate() {
    for (int lcv = 0; lcv < 4; lcv++) {
      Block block = templates[shape][rotation][lcv];
      blocks[lcv] = b(block.x, block.y);
      blocks[lcv].colour = colour;
    }
  }

  public void render(boolean drawText) 
  {
    if (drawText) 
    {
      //get topmost block
      Block top = blocks[0];

      //get leftmost block
      Block left = blocks[0];

      //get rightmost block
      Block right = blocks[0];

      for (Block block : blocks) //grid.cornerX + (block.x + offsetX) * blockWidth
      {
        if (top.y > block.y)
        {
          top = block;
        }
        if (left.x > block.x)
        {
          left = block;
        }
        if (right.x < block.x)
        {
          right = block;
        }
      }

      // //render the text
      // textSize(50);
      // fill(255, 255, 255);
      float textX = grid.cornerX + ((left.x + right.x) / 2 + offsetX) * blockWidth;
      float textY = grid.cornerY + (top.y + offsetY) * blockWidth - blockWidth * .25f;

      float textX2 = grid.cornerX + blockWidth * 3;
      float textY2 = grid.cornerY - blockWidth / 3;
      float textY3 = grid.cornerY + (blockWidth * 21) + blockWidth / 1.5f;

      if (type.equals("Charcoal")) {
        textX -= blockWidth * 1.5f;
        textX2 -= blockWidth * 1.5f;
      } else if (type.equals("Ice")) {
        textX += blockWidth * 1;
        textX2 += blockWidth * 1;
      } else if (type.equals("Fire")) {
        textX += blockWidth * .5f;
        textX2 += blockWidth * .5f;
      }
      // for (int x = -1; x < 2; x++) {
      //   //  for(int y = -1; y < 2; y++){
      //   //    text("LIKE THIS!", 20+x,20+y);
      //   //  }
      //   text(type, textX + x, textY);
      //   text(type, textX, textY + x);
      // }
      // fill(colour);
      // text(type, textX, textY);
      if (type.equals("Charcoal"))
      {
        drawTextWithBorder(type, textX, textY, 25, new Color(25, 25, 25), new Color(100, 100, 100));
        drawTextWithBorder(type, textX2, textY2, 50, new Color(25, 25, 25), new Color(100, 100, 100));
        drawTextWithBorder(type, textX2, textY3, 50, new Color(25, 25, 25), new Color(100, 100, 100));
      }
      else
      {
        drawTextWithBorder(type, textX, textY, 25, grid.tetromino.colour, new Color(0, 0, 0));
        drawTextWithBorder(type, textX2, textY2, 50, grid.tetromino.colour, new Color(0, 0, 0));
        drawTextWithBorder(type, textX2, textY3, 50, grid.tetromino.colour, new Color(0, 0, 0));
      }
    }
    
    fill(colour);
    stroke(stroke);
    
    //render the blocks
    for (int lcv = 0; lcv < 4; lcv++) 
    {
      Block block = blocks[lcv];
      rect(grid.cornerX + (block.x + offsetX) * blockWidth, grid.cornerY + (block.y + offsetY) * blockWidth + particleOffset * particleWidth, blockWidth, blockWidth);
    }
    stroke(0, 0, 0);
  }

  public String toString() {
    return "[Tetromino]\n\tShape: " + shape + "\n\tRotation: " + rotation + "\n\tRotation: " + rotation + "\n\tX/Y Offset: (" + offsetX + ", " + offsetY + ")";
  }
}

//represents a particle of water
public class WaterParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(66, 135, 245);

  public WaterParticle(Point indices, String prevType)
  {
      super(indices, "Water", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {
    //it checks 1. directly below, 2. the diagonals below, 3. the neighbors directly left and right. 
    //if 1 exists, move there
    //if 2 exists, move to one of those
    //if 3 exists, move to one of those

    //create a list of particles which this particle can move into
    ArrayList<Particle> openSpaces = new ArrayList<Particle>();

    //get particle which is below this particle
    Particle down = adjacentDown();

    //if that particle is not null
    if (down != null) 
    {
      //if the particle below this one is air
      if (down.type.equals("Air")) 
      {
        //add that particle to the list of spaces to move into
        openSpaces.add(down);
      } 

      //if no space has been found
      if (openSpaces.size() == 0)
      {
        //check the diagonals for air

        //get the diagonal particles
        Particle downLeft = down.adjacentLeft();
        Particle downRight = down.adjacentRight();

        //check the left diagonal for air
        if (downLeft != null && downLeft.type.equals("Air")) 
        {
          //add that particle to the list of spaces to move into
          openSpaces.add(downLeft);
        }

        //check the right diagonal for air
        if (downRight != null && downRight.type.equals("Air")) 
        {
          //add that particle to the list of spaces to move into
          openSpaces.add(downRight);
        }
      }
    }

    //if no spaces have been found
    if (openSpaces.size() == 0)
    {
      //check the left/right neighbors for air

      //get left/right neighbor particles
      Particle left = adjacentLeft();
      Particle right = adjacentRight();

      //check the left neighbor for air
      if (left != null && left.type.equals("Air")) 
      {
        //add to the list of spaces to move into
        openSpaces.add(left);
      }

      //check the right neighbor for air
      if (right != null && right.type.equals("Air")) 
      {
        //add to the list of spaces to move into
        openSpaces.add(right);
      }
    }

    //if any potential particles have been found to swap with 
    if (openSpaces.size() != 0) 
    {
      //choose a random available particle
      Particle rand = openSpaces.get(PApplet.parseInt(random(0, openSpaces.size())));

      //swap this particle with the particle in the chosen space
      grid.swapParticles(this, rand);

      //mark that this particle moved
      moved = true;
    }
  }

  public void interact()
  {
    //get half adjacents
    ArrayList<Particle> halfAdjacents = halfAdjacents();

    //check half adjacents
    for (Particle adj : halfAdjacents) 
    {
      //interact water with lava
      if (adj.type.equals("Lava")) 
      {
        //place a new stone particle where the lava was
        grid.replaceParticle(adj, "Stone");
        
        //place a new stone particle where this water was
        grid.replaceParticle(this, "Stone");

        //marks this particle as interacted
        interacted = true;
      }
    }
  }
}
  public void settings() {  size(900, 800); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "PROJ4" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
