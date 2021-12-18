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
    //if (active) {
    //  noFill();
    //strokeWeight(2);
    //  rect(grid.cornerX + blockWidth * x, grid.cornerY + blockWidth * y, blockWidth, blockWidth);
    //strokeWeight(1);
    //}
    if (full) {
      fill(255, 255, 255, 127);
      rect(grid.cornerX + blockWidth * x, grid.cornerY + blockWidth * y, blockWidth, blockWidth);
    }
    //if (false) {
    //  noFill();
    //  rect(grid.cornerX + blockWidth * x, grid.cornerY + blockWidth * y, blockWidth, blockWidth);
    //}
  }
}
