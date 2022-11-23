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
