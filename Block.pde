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

  public Block(int x, int y) {
    this.x = x;
    this.y = y;
    active = false;
    full = false;
  }

  public void render()
  {
    if (active) {
      fill(252, 186, 3);
      rect(grid.cornerX + blockWidth * x, grid.cornerY + blockWidth * y, blockWidth, blockWidth);
    }
    noFill();
    rect(grid.cornerX + blockWidth * x, grid.cornerY + blockWidth * y, blockWidth, blockWidth);
  }
}
