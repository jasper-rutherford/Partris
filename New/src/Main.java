import processing.core.*;

import java.util.ArrayList;

public class Main extends PApplet
{
    public static int screenWidth = 900;
    public static int screenHeight = 800;

    public final static float version = 1.4f;
    Grid grid;

    public static Block templates[][][] = new Block[7][4][4];

    ArrayList<Integer> inputs = new ArrayList<>();
    ArrayList<Integer> konami = new ArrayList<>();
    public static boolean debug = false;
    private boolean numRowFunc = false;
    public static ArrayList<String> allTypes;

    public void setup()
    {
        //set title to game name and version
        surface.setTitle("Partris v" + version);

        //fill the template array with all piece shapes and rotations
        fillTemplates();

        //not including air because this is used to decide what type a tetromino can be
        allTypes = new ArrayList<>();
        allTypes.add("Acid");
        allTypes.add("Charcoal");
        allTypes.add("Fire");
        allTypes.add("Goop");
        allTypes.add("Ice");
        allTypes.add("Lava");
        allTypes.add("Plant");
        allTypes.add("Stone");
        allTypes.add("Water");

        //reset the grid
        Grid.reset();

        //build the grid
        grid = Grid.getGrid();
        grid.setupParticleStuff();
        println("setup particle stuff");

        grid.setupTetrominos();

        println("setup tetrominos");

        if (konami.size() == 0)
        {
            //setup konami
            inputs.add(38);
            konami.add(38);
            inputs.add(38);
            konami.add(38);
            inputs.add(40);
            konami.add(40);
            inputs.add(40);
            konami.add(40);
            inputs.add(37);
            konami.add(37);
            inputs.add(39);
            konami.add(39);
            inputs.add(37);
            konami.add(37);
            inputs.add(39);
            konami.add(39);
            inputs.add(66);
            konami.add(66);
            inputs.add(65);
            konami.add(65);
        }
    }

    public void draw()
    {
        grid.update();
        background(60, 60, 60);
        grid.render(this);
    }

    //key controls
    public void keyPressed()
    {
        //enter advances to next shape (if debug mode is enabled)
        if (debug && key == '\n')
        {
            grid.tetromino.shape = (grid.tetromino.shape + 1) % 7;
            grid.tetromino.copyTemplate();
        }
        //\ advances to next type (if debug mode is enabled)
        if (debug && key == '\\')
        {
            int index = allTypes.indexOf(grid.tetromino.type);
            grid.tetromino.setType(allTypes.get((index + 1) % allTypes.size()));
            grid.tetromino.copyTemplate();
        }
        //a toggles whether sleeping particles have a border around them (if debug mode is enabled)
        if (debug && (key == 'a' || key == 'A'))
        {
            Particle.alphaSleep = !Particle.alphaSleep;
            println("toggled alphaSleep", Particle.alphaSleep);
        }
        if (debug && (key == 'b' || key == 'B'))
        {
            Particle.renderParticleWideNeighbors = !Particle.renderParticleWideNeighbors;
            println("toggled render wide particles", Particle.renderParticleWideNeighbors);
        }
        //c holds the current piece
        if (key == 'c' || key == 'C')
        {
            grid.hold();
        }
        //d toggles whether rows clear (if debug mode is enabled)
        if (debug && (key == 'd' || key == 'D'))
        {
            Grid.getGrid().toggleCheckingRows();
        }
        //f toggles whether blocks fall automatically (if debug mode is enabled)
        if (debug && (key == 'f' || key == 'F'))
        {
            Grid.getGrid().toggleAutoFall();
        }
        // toggles ghostBlock (if debug mode is enabled)
        if (debug && (key == 'g' || key == 'G'))
        {
            grid.ghostBlock = !grid.ghostBlock;
        }
        // L moves the tetromino up (only in debug mode)
        if (debug && (key == 'l' || key == 'L'))
        {
            grid.tetromino.up();
        }
        // p pauses everything
        if (key == 'p' || key == 'P')
        {
            Grid.getGrid().togglePaused();
        }
        //r restarts
        if (key == 'r' || key == 'R')
        {
            println("restarting");
            setup();
        }
        //s reshuffles the particlelist (if debug mode is enabled)
        if (debug && (key == 's' || key == 'S'))
        {
            println("shuffling particlelist");
            grid.shuffleParticleList();
        }
        //x toggles whether particles update automatically (if debug mode is enabled)
        if (debug && (key == 'x' || key == 'X'))
        {
            Grid.getGrid().toggleAutoParticle();
        }
        //z manually updates all the particles one tick, only if autoParticle is off (if debug mode is enabled)
        if (debug && (key == 'z' || key == 'Z') && !Grid.getGrid().getAutoFall())
        {
            grid.updateParticles();
            println("forced a particle update");
        }

        //, toggles whether the game is lost (if debug mode is enabled)
        if (debug && (key == ','))
        {
            Grid.getGrid().togglePaused();
        }

        //` toggles the functionality of the number row (if debug mode is enabled)
        if (debug && key == '`')
        {
            numRowFunc = !numRowFunc;
            if (numRowFunc)
            {
                println("numRowFunc toggled: shapes");
            }
            else
            {
                println("numRowFunc toggled: types");
            }
        }

        // disables debug mode (if debug mode is enabled)
        if (debug && key == '/')
        {
            debug = false;
            println("debug mode disabled");
        }

        //sets the type to the (n - 1)th type/shape (except 0 is 9) (type/shape decided by numRowFunc) (if debug mode is enabled)
        if (debug && ((key == '1') || (key == '2') || (key == '3') || (key == '4') || (key == '5') || (key == '6') || (key == '7') || (key == '8') || (key == '9') || (key == '0')))
        {
            int num = 9;
            if (key == '1')
            {
                num = 0;
            }
            else if (key == '2')
            {
                num = 1;
            }
            else if (key == '3')
            {
                num = 2;
            }
            else if (key == '4')
            {
                num = 3;
            }
            else if (key == '5')
            {
                num = 4;
            }
            else if (key == '6')
            {
                num = 5;
            }
            else if (key == '7')
            {
                num = 6;
            }
            else if (key == '8')
            {
                num = 7;
            }
            else if (key == '9')
            {
                num = 8;
            }
            //set type
            if (numRowFunc)
            {
                grid.tetromino.setType(allTypes.get(num % allTypes.size()));
            }
            //set shape
            else
            {
                grid.tetromino.shape = num % 7;
                grid.tetromino.copyTemplate();
            }
        }

        //space slams (doesn't work if paused (unless in debug mode))
        if (((!Grid.lost && !Grid.paused) || debug) && key == ' ')
        {
            grid.tetromino.slam(true);
            //set oldTime to the current time
            Grid.getGrid().prevTime = System.nanoTime();
        }

        boolean lost = Grid.lost;
        boolean paused = Grid.paused;

        //rotates the tetromino 90 degrees clockwise (doesn't work if paused or lost (unless in debug mode))
        if (((!lost && !paused) || debug) && keyCode == UP)
        {
            grid.tetromino.rotateRight();
        }
        //moves the tetromino left one block (doesn't work if paused or lost (unless in debug mode))
        if (((!lost && !paused) || debug) && keyCode == LEFT)
        {
            grid.tetromino.left();
        }
        //moves the tetromino right one block (doesn't work if paused or lost (unless in debug mode))
        if (((!lost && !paused) || debug) && keyCode == RIGHT)
        {
            grid.tetromino.right();
        }
        //moves the tetromino down one block (doesn't work if paused or lost (unless in debug mode))
        if (((!lost && !paused) || debug) && keyCode == DOWN)
        {
            grid.tetromino.down();
            //set oldTime to the current time
            Grid.getGrid().prevTime = System.nanoTime();
        }

        konamiCheck(keyCode);
    }

    //draws the provided text with a border around it
    public void drawTextWithBorder(String text, float topLeftCornerX, float topLeftCornerY, int textSize, Color fillColor, Color borderColor)
    {
        //render the score
        textSize(textSize);
        fill(borderColor);

        for (int x = -1; x < 2; x++)
        {
            text(text, topLeftCornerX + x, topLeftCornerY);
            text(text, topLeftCornerX, topLeftCornerY + x);
        }

        fill(fillColor);
        text(text, topLeftCornerX, topLeftCornerY);
    }

    public void konamiCheck(int code)
    {
        inputs.remove(0);
        inputs.add(code);

        boolean match = true;
        for (int lcv = 0; lcv < konami.size(); lcv++)
        {
            if (inputs.get(lcv) != konami.get(lcv))
            {
                match = false;
            }
        }
        if (!debug && match)
        {
            debug = true;
            println("Activating debug mode");
        }
    }

    //lets me send in a Color to fill without any messing around
    public void fill(Color colour)
    {
        fill(colour.r, colour.g, colour.b, colour.a);
    }

    //lets me send in a Color to stroke without any messing around
    public void stroke(Color colour)
    {
        stroke(colour.r, colour.g, colour.b, colour.a);
    }

    /*
      Sets up the tetromino templates
     */

    //shortcut to create a block
    public static Block b(int x, int y)
    {
        return new Block(x, y, Grid.blockWidth);
    }

    //sends in the four given blocks to the template array at once
    public void pack(int shape, int rotation, Block a, Block b, Block c, Block d)
    {
        Block[] packed = {a, b, c, d};
        templates[shape][rotation] = packed;
        //templates[shape][rotation][0] = a;
        //templates[shape][rotation][1] = b;
        //templates[shape][rotation][2] = c;
        //templates[shape][rotation][3] = d;
    }

    //all the templates for all the tetrominos and all their rotations. Each increment of the rotation index is a 90 degree clockwise rotation with arbitrary starting positions.
    //tetrominos are in alphabetical order (IJLOSTZ -> 0123456)
    public void fillTemplates()
    {
        //I
        pack(0, 0, b(0, 0), b(0, -1), b(0, -2), b(0, 1));
        pack(0, 1, b(-1, -1), b(0, -1), b(1, -1), b(2, -1));
        pack(0, 2, b(1, -2), b(1, -1), b(1, 0), b(1, 1));
        pack(0, 3, b(0, 0), b(-1, 0), b(1, 0), b(2, 0));

        //J
        pack(1, 0, b(0, 0), b(0, 1), b(-1, 1), b(0, -1));
        pack(1, 1, b(0, 0), b(-1, 0), b(-1, -1), b(1, 0));
        pack(1, 2, b(0, 0), b(0, -1), b(1, -1), b(0, 1));
        pack(1, 3, b(0, 0), b(1, 0), b(1, 1), b(-1, 0));

        //L
        pack(2, 0, b(0, 0), b(0, 1), b(1, 1), b(0, -1));
        pack(2, 1, b(0, 0), b(-1, 0), b(1, 0), b(-1, 1));
        pack(2, 2, b(0, 0), b(0, -1), b(-1, -1), b(0, 1));
        pack(2, 3, b(0, 0), b(1, 0), b(-1, 0), b(1, -1));

        //O
        pack(3, 0, b(0, 0), b(0, -1), b(1, -1), b(1, 0));
        pack(3, 1, b(0, 0), b(0, -1), b(1, -1), b(1, 0));
        pack(3, 2, b(0, 0), b(0, -1), b(1, -1), b(1, 0));
        pack(3, 3, b(0, 0), b(0, -1), b(1, -1), b(1, 0));

        //S
        pack(4, 0, b(0, 0), b(1, 0), b(0, 1), b(-1, 1));
        pack(4, 1, b(0, 0), b(0, 1), b(-1, 0), b(-1, -1));
        pack(4, 2, b(0, 0), b(-1, 0), b(0, -1), b(1, -1));
        pack(4, 3, b(0, 0), b(0, -1), b(1, 0), b(1, 1));

        //T
        pack(5, 0, b(0, 0), b(-1, 0), b(1, 0), b(0, 1));
        pack(5, 1, b(0, 0), b(0, -1), b(0, 1), b(-1, 0));
        pack(5, 2, b(0, 0), b(1, 0), b(-1, 0), b(0, -1));
        pack(5, 3, b(0, 0), b(0, 1), b(0, -1), b(1, 0));

        //Z
        pack(6, 0, b(0, 0), b(-1, 0), b(0, 1), b(1, 1));
        pack(6, 1, b(0, 0), b(0, -1), b(-1, 0), b(-1, 1));
        pack(6, 2, b(0, 0), b(1, 0), b(0, -1), b(-1, -1));
        pack(6, 3, b(0, 0), b(0, 1), b(1, 0), b(1, -1));
    }

    public void settings()
    {
        size(screenWidth, screenHeight);
    }

    //launch the game
    static public void main(String[] passedArgs)
    {
        String[] appletArgs = new String[]{"Main"};
        if (passedArgs != null)
        {
            PApplet.main(concat(appletArgs, passedArgs));
        }
        else
        {
            PApplet.main(appletArgs);
        }
    }
}
