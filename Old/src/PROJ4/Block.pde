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
    
  }

  public void renderEdgeOutlines()
  {
    if (full)
    {
      float x = grid.cornerX + blockWidth * this.x;
      float y = grid.cornerY + blockWidth * this.y; boolean leftFull = adj("Left") == null || !adj("Left").full;
      boolean left = (adj("Left") == null || !adj("Left").full);
      boolean right = (adj("Right") == null || !adj("Right").full);
      boolean up = (adj("Up") == null || !adj("Up").full);
      boolean down = (adj("Down") == null || !adj("Down").full);
      
      if (left)
      {
        //draw edge
        helper(3, new Color(0, 0, 0), x, y, x, y + blockWidth);
      }
      if (right)
      {
        //draw edge
        helper(3, new Color(0, 0, 0), x + blockWidth, y, x + blockWidth, y + blockWidth);
      }
      if (up)
      {
        //draw edge
        helper(3, new Color(0, 0, 0), x, y, x + blockWidth, y);
      }
      if (down)
      {
        //draw edge
        helper(3, new Color(0, 0, 0), x, y + blockWidth, x + blockWidth, y + blockWidth);
      }
    }
  }

  public void renderEdges()
  {
    if (full)
    {
      float x = grid.cornerX + blockWidth * this.x;
      float y = grid.cornerY + blockWidth * this.y; boolean leftFull = adj("Left") == null || !adj("Left").full;
      boolean left = (adj("Left") == null || !adj("Left").full);
      boolean right = (adj("Right") == null || !adj("Right").full);
      boolean up = (adj("Up") == null || !adj("Up").full);
      boolean down = (adj("Down") == null || !adj("Down").full);
    
      if (left)
      {
        //draw edge
        helper(1, new Color(246, 255, 66), x, y, x, y + blockWidth);
      }
      if (right)
      {
        //draw edge
        helper(1, new Color(246, 255, 66), x + blockWidth, y, x + blockWidth, y + blockWidth);
      }
      if (up)
      {
        //draw edge
        helper(1, new Color(246, 255, 66), x, y, x + blockWidth, y);
      }
      if (down)
      {
        //draw edge
        helper(1, new Color(246, 255, 66), x, y + blockWidth, x + blockWidth, y + blockWidth);
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
