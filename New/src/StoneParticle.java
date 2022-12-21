import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Random;

public class StoneParticle extends Particle
{
    //this particle's color
    private Color uniqueColor = new Color(112, 112, 112);

    public StoneParticle(Point indices, String prevType)
    {
        super(indices, "Stone", prevType);
        this.colour = uniqueColor;
    }

    //gets the spaces below this one
    //checks for air directly below, then air on the diagonals, then water/lava directly below, then water/lava on the diagonals
    public void move()
    {
        //create an empty list of available spaces to move into
        ArrayList<Particle> openSpaces = new ArrayList<Particle>();

        //get particle below this one
        Particle down = adjacentDown();

        //if the below particle is not null
        if (down != null)
        {
            //get the diagonal particles
            Particle downLeft = down.adjacentLeft();
            Particle downRight = down.adjacentRight();

            //if the particle below this stone is air
            if (down.type.equals("Air"))
            {
                //add that particle to the list of available spaces
                openSpaces.add(down);
            }

            //if no space has been found
            if (openSpaces.size() == 0)
            {
                //check the diagonals for air

                //check the left diagonal for air
                if (downLeft != null && (downLeft.type.equals("Air")))
                {
                    //add to list of open spaces
                    openSpaces.add(downLeft);
                }
                //check right diagonal for air
                if (downRight != null && (downRight.type.equals("Air")))
                {
                    //add to list of open spaces
                    openSpaces.add(downRight);
                }
            }

            //if no spaces have been found
            if (openSpaces.size() == 0)
            {
                //check down square for water or lava or acid
                if (down.type.equals("Water") || down.type.equals("Lava") || down.type.equals("Acid"))
                {
                    //add to list of open spaces
                    openSpaces.add(down);
                }
            }

            //if no spaces have been found
            if (openSpaces.size() == 0)
            {
                //check diagonals for water or lava

                //check left diagonal for water or lava
                if (downLeft != null && (downLeft.type.equals("Water") || downLeft.type.equals("Lava") || downLeft.type.equals("Acid")))
                {
                    //add to list of open spaces
                    openSpaces.add(downLeft);
                }
                //check right diagonal for water or lava
                if (downRight != null && (downRight.type.equals("Water") || downRight.type.equals("Lava") || downRight.type.equals("Acid")))
                {
                    //add to list of open spaces
                    openSpaces.add(downRight);
                }
            }
        }

        //if any spaces have been found which could be moved to
        if (openSpaces.size() != 0)
        {
            //pick a random particle from the available spaces
            Particle move = openSpaces.get((new Random()).nextInt(openSpaces.size()));

            //swap this stone with that random particle
            Grid.getGrid().swapParticles(this, move);

            //mark that this stone moved (possibly redundant)
            moved = true;
        }
    }

    public void interact()
    {

    }
}
