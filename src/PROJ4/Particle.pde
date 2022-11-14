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
    this.prevType = type;

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
    interacted = true;
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
