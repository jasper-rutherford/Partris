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

  public Particle(String type, int xIndex, int yIndex) {
    this.xIndex = xIndex;
    this.yIndex = yIndex;

    x = grid.cornerX + xIndex * particleWidth;
    y = grid.cornerY + yIndex * particleWidth;

    this.type = type;
    this.colour = grid.typeColors.get(type).copy();
    
    this.fuel = fuel;
  }

  public void setType(String type) {
    if (!this.type.equals("Air")) {
      grid.particleList.remove(this);
    }
    this.type = type;
    this.colour = grid.typeColors.get(type).copy();
    if (!this.type.equals("Air")) {
      grid.particleList.add(this);
    }
  }

  public void render() {
    fill(colour);
    rect(x, y, particleWidth, particleWidth);
  }

  public void move() {
  }
  
  public ArrayList<Particle> adjacents() {
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

  public void interact() {
    ArrayList<Particle> adjacents = adjacents();
    Particle adj;

    if (type.equals("Fire")) {
      for (int lcv = 0; lcv < adjacents.size(); lcv++) {
        adj = adjacents.get(lcv);
        if (adj.type.equals("Plant")) {
          adj.setType("Fire");
        }
      }
    }
    else if (type.equals("Plant")) {
      //check all adjacent particles
      for (int lcv = 0; lcv < adjacents.size(); lcv++) {
        adj = adjacents.get(lcv);
        
        //if the adjacent particle is air, plant could grow there. 
        if (adj.type.equals("Air")) {
          //get all the particles adjacent to the air particles
          ArrayList<Particle> adjAdjacents = adj.adjacents();
          
          //loop through these adjacent particles
          for (int lcv2 = 0; lcv2 < adjAdjacents.size(); lcv2++) {
            Particle adjAdj = adjAdjacents.get(lcv2);
            
            //if the particle isn't air or plant, then it is a viable spot to grow. 
            if (!adjAdj.type.equals("Air") && !adjAdj.type.equals("Plant")) {
              //grow the plant
              adj.setType("Plant");
              
              //plant cannot grow multiple times into one particle, so end the loop as soon as plant grows
              break;
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
