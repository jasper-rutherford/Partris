import processing.core.*;

import java.util.Random;

public class Tetromino
{
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
    public Tetromino()
    {
        //get random shape and rotation
        shape = Grid.getGrid().pickShape();         //pickShape uses 7 bag
        rotation = (new Random().nextInt(4));

        //sets up the tetromino with the random shape and rotation, plus random type
        type = Grid.getGrid().pickType();    //pickType uses the same kind of randomness as pickShape

        setupTetromino(shape, rotation, type);
    }

    //creates a tetromino with defined shape and rotation
    public Tetromino(int shape, int rotation)
    {

        //sets up the tetromino with the given shape and rotation, plus random type
        type = Grid.getGrid().pickType();    //pickType uses the same kind of randomness as pickShape

        setupTetromino(shape, rotation, type);
    }

    public Tetromino(Tetromino tetromino)
    {
        shape = tetromino.shape;
        rotation = tetromino.rotation;
        type = tetromino.type;
        colour = tetromino.colour.copy();

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

    public void setupTetromino(int shape, int rotation, String type)
    {

        //instantiate blocks
        blocks = new Block[4];

        //set shape and rotation
        this.shape = shape;
        this.rotation = rotation;
        this.type = type;

        //set colour according to particle type
        colour = ParticleFactory.getColour(type);

        //copy in relevant blocks from the template
        copyTemplate();

        //x offset is at about the center of the screen
        offsetX = (Grid.getGrid().gridWidth - 1) / 2;

        //y offset is the negative of the lowest y value in the piece's blocks
        offsetY = blocks[0].y;
        for (int lcv = 1; lcv < 4; lcv++)
        {
            int y = blocks[lcv].y;
            if (y < offsetY)
            {
                offsetY = y;
            }
        }
        offsetY *= -1;

        particleOffset = 0;

        // if (collision(0, 0)) {
        //   lose();
        // }
    }

    public void up()
    {
        //if an upward movement wouldn't collide with anything
        if (!Grid.getGrid().tetromino.collision(0, -1))
        {
            //move the block up one row
            Grid.getGrid().tetromino.offsetY--;
        }
    }

    public void left()
    {
        //if a downward movement wouldn't collide with anything
        if (!Grid.getGrid().tetromino.collision(-1, 0))
        {
            //move the block down one row
            Grid.getGrid().tetromino.offsetX--;
        }
    }

    public void right()
    {
        //if a downward movement wouldn't collide with anything
        if (!collision(1, 0))
        {
            //move the block down one row
            offsetX++;
        }
    }

    public void down()
    {
        //if a downward movement wouldn't collide with anything
        if (!collision(0, 1))
        {
            //move the block down one row
            offsetY++;
        }
        //if there would be a collision
        else
        {
            //particleSlam the tetromino
            particleSlam();

            //put the block on the Grid.getGrid() and create a new tetromino
            place();
            Grid.getGrid().checkRows();

            //get the next tetromino
            Grid.getGrid().newTetromino();
        }
    }

    public void slam(boolean place)
    {

        //drop tetromino as far as it will go
        boolean slamming = true;
        while (slamming)
        {
            //if a downward movement wouldn't collide with anything
            if (!collision(0, 1))
            {
                //move the block down one row
                offsetY++;
            }
            //otherwise the block has descended as far as it will go
            else
            {
                slamming = false;
            }
        }

        //if place is true
        if (place)
        {
            particleSlam();
            //put the block on the Grid.getGrid() and create a new tetromino
            place();
            Grid.getGrid().checkRows();

            //get the next tetromino
            Grid.getGrid().newTetromino();
        }
    }

    //move the tetromino down as many particle rows as it can without hitting any particles
    public void particleSlam()
    {
        //drop tetromino as many particle rows as it will go
        boolean slamming = true;
        while (slamming)
        {
            //if a downward movement wouldn't collide with anything
            if (!particleCollision(1))
            {
                //move the block down one row
                particleOffset++;
            }
            //otherwise the block has descended as far as it will go
            else
            {
                slamming = false;
            }
        }
    }

    public void rotateLeft()
    {
    }

    //one 90 degree clockwise rotation
    public void rotateRight()
    {

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
        if (!rotated.collision(0, 0))
        {
            //set it to be the current piece
            Grid.getGrid().tetromino = rotated;
        }
        //otherwise check for wall kicks
        else
        {
            boolean leftCollide = rotated.collision(-1, 0);
            boolean rightCollide = rotated.collision(1, 0);
            boolean upCollide = rotated.collision(0, -1);
            boolean downCollide = rotated.collision(0, 1);

            //if only left kick works, then do a left kick and set the rotated kicked piece to be the current piece
            if (!leftCollide && rightCollide)
            {
                rotated.offsetX--;
                Grid.getGrid().tetromino = rotated;
            }
            //if only right kick works then do a right kick and set the rotated kicked piece to be the current piece
            else if (leftCollide && !rightCollide)
            {
                rotated.offsetX++;
                Grid.getGrid().tetromino = rotated;
            }
            //if only up kick works and down doesnt yada ya you get it
            else if (!upCollide && downCollide)
            {
                rotated.offsetY--;
                Grid.getGrid().tetromino = rotated;
            }
            //down kick
            else if (upCollide && !downCollide)
            {
                rotated.offsetY++;
                Grid.getGrid().tetromino = rotated;
            }
        }

        copyTemplate();
    }

    //fills the block's space on the Grid.getGrid() with particles
    public void place()
    {
        int particlesPerEdge = Grid.getGrid().particlesPerEdge;
        //loop through each block in the tetromino
        for (int lcv = 0; lcv < 4; lcv++)
        {
            Block block = blocks[lcv];
            //generate particles
            for (int particleX = 0; particleX < particlesPerEdge; particleX++)
            {
                for (int particleY = 0; particleY < particlesPerEdge; particleY++)
                {
                    int xIndex = PApplet.parseInt((block.x + offsetX) * particlesPerEdge + particleX);
                    int yIndex = PApplet.parseInt((block.y + offsetY) * particlesPerEdge + particleY + particleOffset);

                    //get the particle currently in this position
                    Particle oldParticle = Grid.getGrid().particleGrid[xIndex][yIndex];

                    //replace the old particle with a new particle
                    Grid.getGrid().replaceParticle(oldParticle, type);
                }
            }
        }

        Grid.getGrid().updateBlockStats();
    }

    public void setType(String type)
    {
        this.type = type;
        this.colour = ParticleFactory.getColour(type);
    }

    //checks if the tetromino would collide with anything if it moved according to the given x, y
    public boolean collision(int x, int y)
    {
        boolean out = false;

        //check all blocks in the tetromino
        for (int lcv = 0; lcv < 4; lcv++)
        {
            Block block = blocks[lcv];
            int netX = block.x + x + offsetX;
            int netY = block.y + y + offsetY;

            //checks for if the given block is outside the Grid.getGrid()
            if (netX < 0 || netY < 0 || netX >= Grid.getGrid().gridWidth || netY >= Grid.getGrid().gridHeight)
            {
                out = true;
            }
            // or inside the Grid.getGrid() but overlapping another block
            else if (netX >= 0 && netY >= 0 && netX < Grid.getGrid().gridWidth && netY < Grid.getGrid().gridHeight && Grid.getGrid().blocks[netX][netY].active)
            {
                out = true;
            }
        }

        return out;
    }

    //checks if the tetromino would collide with anything if it moved according to the given y
    public boolean particleCollision(int y)
    {
        int particlesPerEdge = Grid.getGrid().particlesPerEdge;
        boolean out = false;

        //check all blocks in the tetromino
        for (int lcv = 0; lcv < 4; lcv++)
        {
            Block block = blocks[lcv];
            int blockCornerX = (block.x + offsetX) * particlesPerEdge;
            int blockCornerY = (block.y + offsetY) * particlesPerEdge + particleOffset + y;

            for (int relX = 0; relX < particlesPerEdge; relX++)
            {
                for (int relY = 0; relY < particlesPerEdge; relY++)
                {
                    //checks for if the given block is outside the particle Grid.getGrid()
                    int netX = blockCornerX + relX;
                    int netY = blockCornerY + relY;
                    if (netX < 0 || netY < 0 || netX >= Grid.getGrid().gridWidth * particlesPerEdge || netY >= Grid.getGrid().gridHeight * particlesPerEdge)
                    {
                        out = true;
                    }
                    // or inside the Grid.getGrid() but overlapping another block
                    else if (netX >= 0 && netY >= 0 && netX < Grid.getGrid().gridWidth * particlesPerEdge && netY < Grid.getGrid().gridHeight * particlesPerEdge && !Grid.getGrid().particleGrid[netX][netY].type.equals("Air"))
                    {
                        out = true;
                    }
                }
            }
        }

        return out;
    }

    //copies relevant blocks from template
    public void copyTemplate()
    {
        for (int lcv = 0; lcv < 4; lcv++)
        {
            Block block = Main.templates[shape][rotation][lcv];
            blocks[lcv] = Main.b(block.x, block.y);
            blocks[lcv].colour = colour;
        }
    }

    public void render(boolean drawText, Main drawer)
    {
        if (drawText)
        {
            //get topmost block
            Block top = blocks[0];

            //get leftmost block
            Block left = blocks[0];

            //get rightmost block
            Block right = blocks[0];

            for (Block block : blocks) //Grid.getGrid().cornerX + (block.x + offsetX) * blockWidth
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
            int blockWidth = Grid.getGrid().blockWidth;
            float textX = Grid.getGrid().cornerX + ((left.x + right.x) / 2 + offsetX) * blockWidth;
            float textY = Grid.getGrid().cornerY + (top.y + offsetY) * blockWidth - blockWidth * .25f;

            float textX2 = Grid.getGrid().cornerX + blockWidth * 3;
            float textY2 = Grid.getGrid().cornerY - blockWidth / 3;
            float textY3 = Grid.getGrid().cornerY + (blockWidth * 21) + blockWidth / 1.5f;

            if (type.equals("Charcoal"))
            {
                textX -= blockWidth * 1.5f;
                textX2 -= blockWidth * 1.5f;
            }
            else if (type.equals("Ice"))
            {
                textX += blockWidth * 1;
                textX2 += blockWidth * 1;
            }
            else if (type.equals("Fire"))
            {
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
                drawer.drawTextWithBorder(type, textX, textY, 25, new Color(25, 25, 25), new Color(100, 100, 100));
                drawer.drawTextWithBorder(type, textX2, textY2, 50, new Color(25, 25, 25), new Color(100, 100, 100));
                drawer.drawTextWithBorder(type, textX2, textY3, 50, new Color(25, 25, 25), new Color(100, 100, 100));
            }
            else
            {
                drawer.drawTextWithBorder(type, textX, textY, 25, Grid.getGrid().tetromino.colour, new Color(0, 0, 0));
                drawer.drawTextWithBorder(type, textX2, textY2, 50, Grid.getGrid().tetromino.colour, new Color(0, 0, 0));
                drawer.drawTextWithBorder(type, textX2, textY3, 50, Grid.getGrid().tetromino.colour, new Color(0, 0, 0));
            }
        }

        drawer.fill(colour);
        drawer.stroke(stroke);

        int blockWidth = Grid.blockWidth;
        int particleWidth = Grid.blockWidth / Grid.getGrid().particlesPerEdge;
        //render the blocks
        for (int lcv = 0; lcv < 4; lcv++)
        {
            Block block = blocks[lcv];
            drawer.rect(Grid.getGrid().cornerX + (block.x + offsetX) * blockWidth, Grid.getGrid().cornerY + (block.y + offsetY) * blockWidth + particleOffset * particleWidth, blockWidth, blockWidth);
        }
        drawer.stroke(0, 0, 0);
    }

    public String toString()
    {
        return "[Tetromino]\n\tShape: " + shape + "\n\tRotation: " + rotation + "\n\tRotation: " + rotation + "\n\tX/Y Offset: (" + offsetX + ", " + offsetY + ")";
    }
}
