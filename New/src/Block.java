public class Block
{
    public int x;
    public int y;
    private int blockWidth;
    public boolean active;
    public boolean full;
    public int type;
    public Color colour;

    public Block(int x, int y, int blockWidth)
    {
        this.x = x;
        this.y = y;
        this.blockWidth = blockWidth;
        active = false;
        full = false;
        colour = new Color(1, 1, 1);
    }

    public void render(Main drawer)
    {

    }

    public void renderEdgeOutlines(Main drawer)
    {
        if (full)
        {
            float x = Grid.getGrid().cornerX + blockWidth * this.x;
            float y = Grid.getGrid().cornerY + blockWidth * this.y;
            boolean leftFull = adj("Left") == null || !adj("Left").full;
            boolean left = (adj("Left") == null || !adj("Left").full);
            boolean right = (adj("Right") == null || !adj("Right").full);
            boolean up = (adj("Up") == null || !adj("Up").full);
            boolean down = (adj("Down") == null || !adj("Down").full);

            if (left)
            {
                //draw edge
                helper(3, new Color(0, 0, 0), x, y, x, y + blockWidth, drawer);
            }
            if (right)
            {
                //draw edge
                helper(3, new Color(0, 0, 0), x + blockWidth, y, x + blockWidth, y + blockWidth, drawer);
            }
            if (up)
            {
                //draw edge
                helper(3, new Color(0, 0, 0), x, y, x + blockWidth, y, drawer);
            }
            if (down)
            {
                //draw edge
                helper(3, new Color(0, 0, 0), x, y + blockWidth, x + blockWidth, y + blockWidth, drawer);
            }
        }
    }

    public void renderEdges(Main drawer)
    {
        if (full)
        {
            float x = Grid.getGrid().cornerX + blockWidth * this.x;
            float y = Grid.getGrid().cornerY + blockWidth * this.y;
            boolean leftFull = adj("Left") == null || !adj("Left").full;
            boolean left = (adj("Left") == null || !adj("Left").full);
            boolean right = (adj("Right") == null || !adj("Right").full);
            boolean up = (adj("Up") == null || !adj("Up").full);
            boolean down = (adj("Down") == null || !adj("Down").full);

            if (left)
            {
                //draw edge
                helper(1, new Color(246, 255, 66), x, y, x, y + blockWidth, drawer);
            }
            if (right)
            {
                //draw edge
                helper(1, new Color(246, 255, 66), x + blockWidth, y, x + blockWidth, y + blockWidth, drawer);
            }
            if (up)
            {
                //draw edge
                helper(1, new Color(246, 255, 66), x, y, x + blockWidth, y, drawer);
            }
            if (down)
            {
                //draw edge
                helper(1, new Color(246, 255, 66), x, y + blockWidth, x + blockWidth, y + blockWidth, drawer);
            }
        }
    }

    private void helper(int s, Color c, float x1, float y1, float x2, float y2, Main drawer)
    {
        drawer.strokeWeight(s);
        drawer.stroke(c);
        drawer.line(x1, y1, x2, y2);
    }

    //get a block neighboring this one
    private Block adj(String dir)
    {
        if (dir.equals("Right"))
        {
            if (x + 1 < Grid.getGrid().gridWidth)
            {
                return Grid.getGrid().blocks[x + 1][y];
            }
        }
        else if (dir.equals("Left"))
        {
            if (x > 0)
            {
                return Grid.getGrid().blocks[x - 1][y];
            }
        }
        else if (dir.equals("Up"))
        {
            if (y > 0)
            {
                return Grid.getGrid().blocks[x][y - 1];
            }
        }
        else if (dir.equals("Down"))
        {
            if (y + 1 < Grid.getGrid().gridHeight)
            {
                return Grid.getGrid().blocks[x][y + 1];
            }
        }

        return null;
    }
}
