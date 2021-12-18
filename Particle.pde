/**
 Final Project 5611
 Partcle Class
 Represents one Particle
 
 Written by Jasper Rutherford
 */

public class Particle {
  //coordinates of top left corner of particle
  public float x;
  public float y;

  //coordinates in grid.particleGrid for this particle
  public int xIndex;
  public int yIndex;

  public String type;
  public Color colour;

  public int fuel;

  public boolean fresh;
  public Particle(String type, int xIndex, int yIndex) {
    this.xIndex = xIndex;
    this.yIndex = yIndex;

    x = grid.cornerX + xIndex * particleWidth;
    y = grid.cornerY + yIndex * particleWidth;

    this.type = type;
    this.colour = grid.colorMap.get(type).copy();

    resetVariables();
    this.fresh = false;
  }

  public void setType(String type) {
    if (!this.type.equals("Air")) {
      grid.particleList.remove(this);
    }
    if (this.type.equals("Air") && type.equals("Fire")) {
        fuel = baseFuel * 2;
    }  
    else if (this.type.equals("Plant") && type.equals("Fire")) {
       fuel = baseFuel; 
    }
    this.type = type;
    this.colour = grid.colorMap.get(type).copy();
    //resetVariables();
    if (!this.type.equals("Air")) {
      grid.particleList.add(this);
    }
  }

  public void resetVariables() {
    //reset fire
    if (type.equals("Fire")) {
      if (fuel <= 0) {
       fuel = baseFuel / 2; 
      }
    } 
    //reset plant
    else if (type.equals("Plant")) {
      fuel = baseFuel;
    }
  }

  public void render() {
    if (!type.equals("Air")) {
      fill(colour);
      rect(x, y, particleWidth, particleWidth);
    }
  }

  public void move() {
    //move water
    if (type.equals("Water")) {
      //check the spots (including diagonals) below this particle for openness. 
      //if any of those spots are open, move to a random one of the spots
      //otherwise, check if the spots directly left or right are open. 
      //then choose a random open spot to move to. if none are found then there is no movement.
      ArrayList<Particle> openSpaces = new ArrayList<Particle>();

      //check lower neighbor particles
      Particle down = adjacentDown();
      if (down != null) {
        Particle downLeft = down.adjacentLeft();
        Particle downRight = down.adjacentRight();
        if (down.type.equals("Air")) {
          openSpaces.add(down);
        }
        if (downLeft != null && downLeft.type.equals("Air")) {
          openSpaces.add(downLeft);
        }
        if (downRight != null && downRight.type.equals("Air")) {
          openSpaces.add(downRight);
        }
      }

      //only consider direct left and right neighbors if no lower neighbors are available
      if (openSpaces.size() == 0) {
        //check left neighbor particle
        Particle left = adjacentLeft();
        if (left != null && left.type.equals("Air")) {
          openSpaces.add(left);
        }

        //check right neighbor particle
        Particle right = adjacentRight();
        if (right != null && right.type.equals("Air")) {
          openSpaces.add(right);
        }
      }

      //pick a random spot from the openSpaces to move to
      if (openSpaces.size() != 0) {
        Particle move = openSpaces.get(int(random(0, openSpaces.size())));
        move.setType("Water");
        setType("Air");
      }
    } 
    //move fire
    else if (type.equals("Fire")) {
      fuel -= int(random(0, baseFuel / 2));
      //colour.a = 255 * fuel / baseFuel;
      if (fuel <= 0) {
        setType("Air");
      }
    }

    //println(type, colour);
    if ((type.equals("Fire") && !colour.equals(grid.colorMap.get("Fire"))) || (type.equals("Fire") && !colour.equals(grid.colorMap.get("Fire")))) {
      println(type.equals("Fire"), colour.equals(grid.colorMap.get("Fire")));
    }
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

  public void interact() {
    ArrayList<Particle> halfAdjacents = halfAdjacents();
    ArrayList<Particle> fullAdjacents = fullAdjacents();
    Particle adj;

    //if the particle was created this step by another particle (probably in an interaction), then this particle does nothing this step
    if (fresh) {
      fresh = !fresh;
    } 
    //if it's not fresh though then it's good to go
    else {
      //interact fire
      if (type.equals("Fire")) {
        //check half adjacents for air. if there is no air then the fire goes out.
        boolean hasAir = false;
        for (int lcv = 0; lcv < halfAdjacents.size(); lcv++) {
          if (halfAdjacents.get(lcv).type.equals("Air")) {
            hasAir = true;
            break;
          }
        }

        //extinguish this fire if it has no air access (set to plant if it has fuel) (which it should? it should never have zero fuel in this section.)
        if (!hasAir && fuel > 0) {
          setType("Plant");
        }
        //if the fire has no access to air and it has no fuel
        else if (!hasAir) {
          println("this really shouldnt be possible :/");
        }
        //if the fire has access to air then try to spread fire
        else {
          //loop through the fire's half adjacents
          for (int lcv = 0; lcv < halfAdjacents.size(); lcv++) {
            adj = halfAdjacents.get(lcv);

            //if the adjacent particle is plant
            if (adj.type.equals("Plant")) {
              //loop through the plant's half adjacents
              ArrayList<Particle> plantAdjacents = adj.halfAdjacents();
              for (int plantLcv = 0; plantLcv < plantAdjacents.size(); plantLcv++) {
                //if the plant has any air fully adjacent
                if (plantAdjacents.get(plantLcv).type.equals("Air")) {
                  //set the plant to be fire
                  adj.setType("Fire");

                  //set to fresh to prevent fire from chaining into a million burns in one step
                  adj.fresh = true;
                  ;
                }
              }
            }
          }
        }
      }
      //interact plant
      else if (type.equals("Plant")) {
        //check all adjacent particles
        for (int lcv = 0; lcv < fullAdjacents.size(); lcv++) {
          adj = fullAdjacents.get(lcv);

          //if the adjacent particle is air, plant could grow there. 
          if (adj.type.equals("Air")) {
            //get all the particles adjacent to the air particles
            ArrayList<Particle> adjAdjacents = adj.halfAdjacents();

            //loop through these adjacent particles
            for (int lcv2 = 0; lcv2 < adjAdjacents.size(); lcv2++) {
              Particle adjAdj = adjAdjacents.get(lcv2);

              //if the particle isn't air or plant, then it is a viable spot to grow. 
              if (!adjAdj.type.equals("Air") && !adjAdj.type.equals("Plant")) {
                //grow the plant
                adj.setType("Plant");

                //set to fresh so that a plant can't chain into a million particles in one step
                adj.fresh = true;

                //plant cannot grow multiple times into one particle, so end the loop as soon as plant grows
                break;
              }
            }
          }
        }
      }
    }
  }

  //gets the particle from the grid that is to the left of this one
  public Particle adjacentLeft() {
    if (xIndex != 0) {
      return grid.particleGrid[xIndex - 1][yIndex];
    }
    return null;
  }

  //gets the particle from the grid that is to the right of this one
  public Particle adjacentRight() {
    if (xIndex != grid.particleGrid.length - 1) {
      return grid.particleGrid[xIndex + 1][yIndex];
    }
    return null;
  }

  //gets the particle from the grid that is above this one
  public Particle adjacentUp() {
    if (yIndex != 0) {
      return grid.particleGrid[xIndex][yIndex - 1];
    }
    return null;
  }

  //gets the particle from the grid that is below this one  
  public Particle adjacentDown() {
    if (yIndex != grid.particleGrid[0].length - 1) {
      return grid.particleGrid[xIndex][yIndex + 1];
    }
    return null;
  }
}
