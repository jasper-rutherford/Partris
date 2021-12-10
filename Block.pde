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
  public int colour[];

  public Block(int x, int y) {
    this.x = x;
    this.y = y;
    active = false;
    full = false;
    colour = new int[3];
    colour[0] = 1;
    colour[1] = 1;
    colour[2] = 1;
  }

  void setColour(int colour[]) {
    this.colour = new int[3];
    this.colour[0] = colour[0];
    this.colour[1] = colour[1];
    this.colour[2] = colour[2];
  }

  public void render()
  {
    if (active) {
      fill(colour[0], colour[1], colour[2]);
      rect(grid.cornerX + blockWidth * x, grid.cornerY + blockWidth * y, blockWidth, blockWidth);
    }
    if (false) {
      noFill();
      rect(grid.cornerX + blockWidth * x, grid.cornerY + blockWidth * y, blockWidth, blockWidth);
    }
  }
}
